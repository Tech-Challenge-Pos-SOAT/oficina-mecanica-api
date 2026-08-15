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

GREEN='\033[0;32m'; BLUE='\033[0;34m'; YELLOW='\033[1;33m'; RED='\033[0;31m'; NC='\033[0m'
log_section() { printf "\n${BLUE}== %s ==${NC}\n\n" "$1"; }
log_ok()      { printf "${GREEN}OK   %s${NC}\n" "$1"; }
log_warn()    { printf "${YELLOW}...  %s${NC}\n" "$1"; }
log_err()     { printf "${RED}ERRO %s${NC}\n" "$1" >&2; }

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$REPO_ROOT"

AI=""; ASSUME_YES="no"
for arg in "$@"; do
  case "$arg" in
    --ai=*)  AI="${arg#*=}" ;;
    --yes|-y) ASSUME_YES="yes" ;;
    -h|--help) sed -n '3,12p' "$0"; exit 0 ;;
    *) log_err "flag desconhecida: $arg"; exit 1 ;;
  esac
done

# ------------------------------------------------------------------
# 1. Pre-requisitos (verifica)
# ------------------------------------------------------------------
log_section "1. Pre-requisitos"

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
# 2. Qual IA
# ------------------------------------------------------------------
log_section "2. Assistente de IA"

if [ -z "$AI" ]; then
  if [ "$ASSUME_YES" = "yes" ]; then
    AI="claude"
  else
    echo "Qual IA voce vai usar?"
    echo "  1) Claude Code"
    echo "  2) Cursor"
    read -p "Escolha (1 ou 2): " choice
    case "$choice" in
      1) AI="claude" ;;
      2) AI="cursor" ;;
      *) log_err "opcao invalida"; exit 1 ;;
    esac
  fi
fi

case "$AI" in
  claude|cursor) log_ok "IA escolhida: $AI" ;;
  *) log_err "valor invalido para --ai: $AI (use claude ou cursor)"; exit 1 ;;
esac

# ------------------------------------------------------------------
# 3. Instalacao de skills e plugins
# ------------------------------------------------------------------
instalar_claude() {
  log_section "3. Claude Code: skills e plugins"

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

  claude plugin marketplace add JuliusBrussee/caveman   >/dev/null 2>&1 || true
  claude plugin install caveman@caveman                 >/dev/null 2>&1 || true
  claude plugin marketplace add DietrichGebert/ponytail >/dev/null 2>&1 || true
  claude plugin install ponytail@ponytail               >/dev/null 2>&1 || true
  claude plugin install superpowers@claude-plugins-official >/dev/null 2>&1 || true
  log_ok "plugins instalados (caveman, ponytail, superpowers)"

  if command -v npx >/dev/null 2>&1; then
    npx ctx7 setup --claude --cli >/dev/null 2>&1 && log_ok "Context7 configurado (find-docs)" \
      || log_warn "Context7 nao configurado (rode 'npx ctx7 setup --claude --cli' a mano)"
  fi
}

instalar_cursor() {
  log_section "3. Cursor: rules e configuration"

  if command -v npx >/dev/null 2>&1; then
    npx ctx7 setup --cursor --cli >/dev/null 2>&1 && log_ok "Context7 configurado (find-docs)" \
      || log_warn "Context7 nao configurado (rode 'npx ctx7 setup --cursor --cli' a mano)"
  fi
}

case "$AI" in
  claude) instalar_claude ;;
  cursor) instalar_cursor ;;
esac

# ------------------------------------------------------------------
# 4. Fim
# ------------------------------------------------------------------
log_section "Pronto"
echo
echo "Configurado:"
case "$AI" in
  claude)
    echo "  Claude Code (caveman, ponytail, superpowers, Context7)"
    ;;
  cursor)
    echo "  Cursor (.cursor/rules/, Context7)"
    ;;
esac
echo "  CLAUDE.md e docs/contexts/ (leia antes de codificar)"
echo
echo "Proximo passo:"
echo "  mvn test"
