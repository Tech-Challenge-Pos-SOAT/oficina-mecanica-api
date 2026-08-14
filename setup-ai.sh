#!/usr/bin/env bash
set -euo pipefail

# setup-ai.sh - prepara o ambiente de IA deste repositorio.
#
# NAO gera documentacao: os docs sao commitados em docs/.
# NAO instala pre-requisito: so verifica e avisa (quem usa nvm nao quer um
# "brew install node" por baixo).
#
# Uso:
#   ./setup-ai.sh                      # interativo
#   ./setup-ai.sh --ai=cursor --yes    # sem perguntar
#   ./setup-ai.sh --ai=claude --os=macos

GREEN='\033[0;32m'; BLUE='\033[0;34m'; YELLOW='\033[1;33m'; RED='\033[0;31m'; NC='\033[0m'
log_section() { printf "\n${BLUE}== %s ==${NC}\n\n" "$1"; }
log_ok()      { printf "${GREEN}OK   %s${NC}\n" "$1"; }
log_warn()    { printf "${YELLOW}...  %s${NC}\n" "$1"; }
log_err()     { printf "${RED}ERRO %s${NC}\n" "$1" >&2; }

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$REPO_ROOT"

AI=""; OS_ALVO=""; ASSUME_YES="no"
for arg in "$@"; do
  case "$arg" in
    --ai=*)  AI="${arg#*=}" ;;
    --os=*)  OS_ALVO="${arg#*=}" ;;
    --yes|-y) ASSUME_YES="yes" ;;
    -h|--help) sed -n '3,14p' "$0"; exit 0 ;;
    *) log_err "flag desconhecida: $arg"; exit 1 ;;
  esac
done

# ------------------------------------------------------------------
# 1. Sistema operacional
# ------------------------------------------------------------------
log_section "1. Sistema operacional"

if [ -z "$OS_ALVO" ]; then
  case "$OSTYPE" in
    darwin*) OS_ALVO="macos" ;;
    linux*)  OS_ALVO="linux" ;;
    *)       OS_ALVO="desconhecido" ;;
  esac
fi

case "$OS_ALVO" in
  macos)
    log_ok "macOS detectado"
    ;;
  linux|windows|desconhecido)
    log_err "OS '$OS_ALVO' ainda nao e suportado por este script."
    echo "Instale a mao e rode de novo com --os=macos para so gerar as rules:"
    echo "  - git, npm, mvn, JDK 21, Docker"
    echo "  - Claude Code:  https://claude.com/claude-code"
    echo "  - plugins:      caveman, ponytail, superpowers"
    exit 1
    ;;
  *)
    log_err "valor invalido para --os: $OS_ALVO (use macos)"; exit 1 ;;
esac

# ------------------------------------------------------------------
# 2. Pre-requisitos (verifica, NAO instala)
# ------------------------------------------------------------------
log_section "2. Pre-requisitos"

FALTANDO=()
verificar() {  # $1 = comando, $2 = como instalar
  if command -v "$1" >/dev/null 2>&1; then
    log_ok "$1 ($("$1" --version 2>&1 | head -1))"
  else
    log_err "$1 nao encontrado - $2"
    FALTANDO+=("$1")
  fi
}

verificar git "brew install git"
verificar npm "instale o Node (nvm install --lts, ou brew install node)"
verificar mvn "brew install maven"

if [ ${#FALTANDO[@]} -gt 0 ]; then
  log_err "faltam ${#FALTANDO[@]} pre-requisito(s): ${FALTANDO[*]}"
  echo "Instale e rode de novo."
  exit 1
fi

# ------------------------------------------------------------------
# 3. Qual IA
# ------------------------------------------------------------------
log_section "3. Assistente de IA"

if [ -z "$AI" ]; then
  if [ "$ASSUME_YES" = "yes" ]; then
    AI="claude"
  else
    echo "Qual assistente voce usa neste repositorio?"
    echo "  1) Claude Code"
    echo "  2) Cursor"
    echo "  3) Ambos"
    read -r -p "Escolha [1-3]: " escolha
    case "$escolha" in
      1) AI="claude" ;;
      2) AI="cursor" ;;
      3) AI="ambos" ;;
      *) log_err "escolha invalida"; exit 1 ;;
    esac
  fi
fi

case "$AI" in
  claude|cursor|ambos) log_ok "IA escolhida: $AI" ;;
  *) log_err "valor invalido para --ai: $AI (use claude, cursor ou ambos)"; exit 1 ;;
esac

# ------------------------------------------------------------------
# 4. Claude Code: skills e plugins
# ------------------------------------------------------------------
instalar_claude() {
  log_section "4. Claude Code: skills e plugins"

  if ! command -v claude >/dev/null 2>&1; then
    log_err "Claude Code nao encontrado. Instale em https://claude.com/claude-code e rode de novo."
    return 1
  fi
  log_ok "Claude Code disponivel"

  if command -v rtk >/dev/null 2>&1; then
    log_ok "RTK ja instalado ($(rtk --version 2>&1 | head -1))"
  else
    log_warn "instalando RTK..."
    curl -fsSL https://raw.githubusercontent.com/rtk-ai/rtk/refs/heads/master/install.sh | sh
    case ":$PATH:" in
      *":$HOME/.local/bin:"*) ;;
      *) echo 'export PATH="$HOME/.local/bin:$PATH"' >> "$HOME/.zshrc"
         export PATH="$HOME/.local/bin:$PATH" ;;
    esac
    log_ok "RTK instalado"
  fi
  rtk init -g >/dev/null 2>&1 || log_warn "rtk init falhou (siga sem ele)"

  log_warn "instalando plugins (caveman, ponytail, superpowers)..."
  claude plugin marketplace add JuliusBrussee/caveman   >/dev/null 2>&1 || true
  claude plugin install caveman@caveman                 >/dev/null 2>&1 || true
  claude plugin marketplace add DietrichGebert/ponytail >/dev/null 2>&1 || true
  claude plugin install ponytail@ponytail               >/dev/null 2>&1 || true
  claude plugin install superpowers@claude-plugins-official >/dev/null 2>&1 || true
  log_ok "plugins instalados"

  if command -v npx >/dev/null 2>&1; then
    npx ctx7 setup --claude >/dev/null 2>&1 && log_ok "Context7 configurado" \
      || log_warn "Context7 nao configurado (rode 'npx ctx7 setup --claude' a mao)"
  fi

  log_ok "rule do Claude: CLAUDE.md ja esta versionado na raiz, nada a gerar"
}

# ------------------------------------------------------------------
# 5. Cursor: rule gerada a partir do template commitado
# ------------------------------------------------------------------
instalar_cursor() {
  log_section "5. Cursor: rules"

  local template="docs/ai/cursor-rule.mdc.template"
  local destino=".cursor/rules/00-projeto.mdc"

  if [ ! -f "$template" ]; then
    log_err "template nao encontrado: $template"
    return 1
  fi

  mkdir -p "$(dirname "$destino")"
  cp "$template" "$destino"
  log_ok "gerado $destino"
  log_warn "Cursor nao instala plugin por CLI - RTK/Caveman/Ponytail sao so do Claude Code."
  log_warn "Para Context7 no Cursor, adicione o MCP em Settings > MCP."
}

case "$AI" in
  claude) instalar_claude ;;
  cursor) instalar_cursor ;;
  ambos)  instalar_claude || true; instalar_cursor ;;
esac

# ------------------------------------------------------------------
# 6. Fim
# ------------------------------------------------------------------
log_section "Setup finalizado"
echo "Contexto do projeto (commitado, nao gerado):"
echo "  docs/context/README.md   <- dicionario: comece por aqui"
echo "  CLAUDE.md                <- rule do Claude Code"
echo "  docs/ai/                 <- templates de rule"
echo
echo "Proximos passos:"
echo "  1. Reinicie o assistente para carregar as rules/plugins."
echo "  2. mvn test   # testes + cobertura JaCoCo"
