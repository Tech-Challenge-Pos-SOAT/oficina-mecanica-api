#!/usr/bin/env bash
set -euo pipefail

# scripts/sonar-local.sh - sobe o SonarQube local (Docker Compose, perfil
# "sonar"), faz o bootstrap (troca de senha do admin + geracao de token) na
# primeira vez, e dispara a analise do projeto contra essa instancia local.
#
# Funciona com Docker Desktop, OrbStack ou Podman em Mac/Linux (e Windows via
# WSL/Git Bash). Para PowerShell nativo no Windows use scripts/sonar-local.ps1
# - mesma logica, sem depender de bash.
#
# Uso:
#   ./scripts/sonar-local.sh up         # sobe o SonarQube (+ Postgres dele) e faz o bootstrap
#   ./scripts/sonar-local.sh analyze    # roda a analise (mvn ... sonar) contra o Sonar local
#   ./scripts/sonar-local.sh down       # para os containers do Sonar (mantem os dados)
#   ./scripts/sonar-local.sh reset      # para e APAGA os dados do Sonar local (recomeca do zero)
#   ./scripts/sonar-local.sh            # atalho = up + analyze
#
# Documentacao completa (memoria minima, troubleshooting por SO/runtime):
# docs/sonarqube-local.md

GREEN='\033[0;32m'; BLUE='\033[0;34m'; YELLOW='\033[1;33m'; RED='\033[0;31m'; NC='\033[0m'
log()      { printf "${BLUE}==>${NC} %s\n" "$1"; }
log_ok()   { printf "${GREEN}OK${NC}   %s\n" "$1"; }
log_warn() { printf "${YELLOW}...${NC}  %s\n" "$1"; }
log_err()  { printf "${RED}ERRO${NC} %s\n" "$1" >&2; }

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$REPO_ROOT"

SONAR_URL="http://localhost:9000"
SONAR_DEFAULT_USER="admin"
SONAR_DEFAULT_PASS="admin"
# Pode sobrescrever a senha nova via env var antes de rodar o script, se quiser.
SONAR_NEW_PASS="${SONAR_LOCAL_ADMIN_PASSWORD:-oficina-mecanica-local}"
TOKEN_DIR=".sonar"
TOKEN_FILE="$TOKEN_DIR/local-token"
TOKEN_NAME="local-$(hostname 2>/dev/null | tr -c 'a-zA-Z0-9' '-' || echo "dev")"

# ---------------------------------------------------------------------------
# Detecta o comando de compose disponivel. Docker Desktop, OrbStack e Podman
# (via "podman compose"/"podman-compose") tem sintaxes ligeiramente
# diferentes dependendo da versao instalada - tentamos na ordem mais comum
# primeiro.
# ---------------------------------------------------------------------------
detect_compose() {
  if command -v docker >/dev/null 2>&1 && docker compose version >/dev/null 2>&1; then
    echo "docker compose"
  elif command -v podman >/dev/null 2>&1 && podman compose version >/dev/null 2>&1; then
    echo "podman compose"
  elif command -v docker-compose >/dev/null 2>&1; then
    echo "docker-compose"
  elif command -v podman-compose >/dev/null 2>&1; then
    echo "podman-compose"
  else
    log_err "nenhum comando de compose encontrado (docker compose / podman compose / docker-compose / podman-compose)"
    log_err "instale o Docker Desktop, OrbStack ou Podman Desktop e tente de novo."
    exit 1
  fi
}

COMPOSE="$(detect_compose)"
log "Runtime de container detectado: $COMPOSE"

wait_for_sonar() {
  log "Aguardando o SonarQube ficar UP em $SONAR_URL (pode levar 1-2 min no primeiro start)..."
  for i in $(seq 1 60); do
    if curl -fsS "$SONAR_URL/api/system/status" 2>/dev/null | grep -q '"status":"UP"'; then
      log_ok "SonarQube UP"
      return 0
    fi
    sleep 5
  done
  log_err "SonarQube nao ficou UP a tempo."
  log_err "Rode '$COMPOSE --profile sonar logs sonarqube' para ver o que esta acontecendo."
  log_err "Causa comum: pouca memoria alocada ao Docker/Podman - ver docs/sonarqube-local.md."
  exit 1
}

bootstrap_token() {
  mkdir -p "$TOKEN_DIR"

  if [ -s "$TOKEN_FILE" ]; then
    log_ok "Token local ja existe em $TOKEN_FILE (bootstrap pulado)"
    return 0
  fi

  log "Primeiro uso: trocando a senha padrao do admin e gerando um token de analise..."

  local change_status
  change_status="$(curl -s -o /dev/null -w '%{http_code}' -u "$SONAR_DEFAULT_USER:$SONAR_DEFAULT_PASS" \
    -X POST "$SONAR_URL/api/users/change_password" \
    --data-urlencode "login=$SONAR_DEFAULT_USER" \
    --data-urlencode "previousPassword=$SONAR_DEFAULT_PASS" \
    --data-urlencode "password=$SONAR_NEW_PASS" || true)"

  if [ "$change_status" = "200" ] || [ "$change_status" = "204" ]; then
    log_ok "Senha do admin trocada"
  else
    log_warn "Nao troquei a senha agora (HTTP $change_status) - provavelmente ja foi trocada em um start anterior. Seguindo com a senha configurada."
  fi

  local token_response
  token_response="$(curl -s -u "$SONAR_DEFAULT_USER:$SONAR_NEW_PASS" \
    -X POST "$SONAR_URL/api/user_tokens/generate" \
    --data-urlencode "name=$TOKEN_NAME")"

  local token=""
  if command -v python3 >/dev/null 2>&1; then
    token="$(printf '%s' "$token_response" | python3 -c 'import json,sys
try:
    print(json.load(sys.stdin).get("token",""))
except Exception:
    pass' 2>/dev/null || true)"
  fi
  if [ -z "$token" ]; then
    # Fallback sem python3: extrai o valor de "token":"..." na marra.
    token="$(printf '%s' "$token_response" | sed -n 's/.*"token":"\([^"]*\)".*/\1/p')"
  fi

  if [ -z "$token" ]; then
    log_err "Nao consegui gerar o token automaticamente."
    log_err "Resposta do SonarQube: $token_response"
    log_err "Gere manualmente em $SONAR_URL > My Account > Security e salve o valor em $TOKEN_FILE"
    exit 1
  fi

  printf '%s' "$token" > "$TOKEN_FILE"
  chmod 600 "$TOKEN_FILE"
  log_ok "Token gerado e salvo em $TOKEN_FILE (arquivo pessoal, ja esta no .gitignore)"
}

cmd_up() {
  log "Subindo SonarQube local ($COMPOSE --profile sonar up -d)..."
  $COMPOSE --profile sonar up -d sonarqube-db sonarqube
  wait_for_sonar
  bootstrap_token
  echo
  log_ok "Pronto. Acesse $SONAR_URL"
  log_ok "Login: admin / senha: '$SONAR_NEW_PASS' (ou a que voce configurou em SONAR_LOCAL_ADMIN_PASSWORD)"
}

cmd_analyze() {
  if [ ! -s "$TOKEN_FILE" ]; then
    log_err "Token nao encontrado em $TOKEN_FILE. Rode '$0 up' primeiro."
    exit 1
  fi
  local token
  token="$(cat "$TOKEN_FILE")"
  log "Rodando a analise (mvn clean verify + sonar-maven-plugin) contra $SONAR_URL..."
  mvn -B clean verify org.sonarsource.scanner.maven:sonar-maven-plugin:5.7.0.6970:sonar \
    -Dsonar.host.url="$SONAR_URL" \
    -Dsonar.token="$token" \
    -Dsonar.projectKey=oficina-mecanica-api-local
  log_ok "Analise concluida. Relatorio: $SONAR_URL/dashboard?id=oficina-mecanica-api-local"
}

cmd_down() {
  log "Parando os containers do Sonar local (dados preservados nos volumes)..."
  $COMPOSE --profile sonar stop sonarqube sonarqube-db
  log_ok "Parado. '$0 up' religa depois sem precisar bootstrap de novo."
}

cmd_reset() {
  log_warn "Isso apaga TODOS os dados/analises do Sonar local (volumes + token salvo)."
  read -r -p "Confirma? (digite 'sim' para continuar): " confirm
  if [ "$confirm" != "sim" ]; then
    log "Cancelado."
    exit 0
  fi
  $COMPOSE --profile sonar down -v
  rm -f "$TOKEN_FILE"
  log_ok "Resetado. Rode '$0 up' para comecar do zero."
}

case "${1:-}" in
  up)      cmd_up ;;
  analyze) cmd_analyze ;;
  down)    cmd_down ;;
  reset)   cmd_reset ;;
  ""|all)  cmd_up && cmd_analyze ;;
  *) log_err "comando desconhecido: $1 (use: up | analyze | down | reset)"; exit 1 ;;
esac
