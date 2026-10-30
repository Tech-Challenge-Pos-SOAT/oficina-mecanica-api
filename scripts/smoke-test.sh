#!/usr/bin/env bash
# Teste de fumaca ponta a ponta da API da oficina (serve de ensaio para o video).
#
# Percorre o fluxo completo da OS contra um ambiente rodando (GKE, Dokploy ou
# local) e FALHA se algum passo devolver codigo HTTP ou status inesperado.
# Tambem dispara os 4 e-mails de status (orcamento enviado, aprovado, finalizado
# e, opcionalmente, recusado) para o e-mail do cliente informado.
#
# Uso:
#   scripts/smoke-test.sh http://IP_EXTERNO [email-do-cliente]
#   PAUSE=1 scripts/smoke-test.sh http://IP_EXTERNO seu@email.com   # pausa a cada passo (video)
#   REJECT=1 scripts/smoke-test.sh ...                              # tambem testa a recusa (2a OS)
#   QUICK=1  scripts/smoke-test.sh ...                              # pula a prova de ordenacao (menos OS e e-mails)
#
# O que cobre: saude, login/JWT, 401, cliente, abertura da OS (servico + peca),
# diagnostico, orcamento, consulta publica, listagem ordenada por prioridade
# (prova com 4 OS em estados diferentes), filtro por status, baixa e devolucao
# de estoque, aprovacao, finalizacao, entrega, historico e os erros esperados
# (404, 409, 400). Dispara os e-mails de status para o cliente informado.
#
# Requisitos: bash, curl e python3 (para ler o JSON). Nao usa jq.
# Efeito colateral: troca o e-mail do cliente do seed (529.982.247-25, Maria)
# para o e-mail informado, cria um veiculo de placa aleatoria e um servico.

set -euo pipefail

BASE_URL="${1:?Informe a URL base, ex.: http://34.16.18.102}"
BASE_URL="${BASE_URL%/}"
CLIENT_EMAIL="${2:-kawanmelofernandes@gmail.com}"
DOCUMENT="${DOCUMENT:-529.982.247-25}"
EMPLOYEE_EMAIL="${EMPLOYEE_EMAIL:-carlos.souza@oficina.com}"
EMPLOYEE_PASSWORD="${EMPLOYEE_PASSWORD:-senha123}"
MATERIAL_ID="${MATERIAL_ID:-1}"
PAUSE="${PAUSE:-0}"
REJECT="${REJECT:-0}"
QUICK="${QUICK:-0}"

BODY_FILE="$(mktemp)"
trap 'rm -f "$BODY_FILE"' EXIT
TOKEN=""
PASS=0

step() { echo; echo "==> $*"; if [ "$PAUSE" = "1" ]; then read -r -p "    [Enter para executar] " _; fi; }
ok()   { echo "    OK: $*"; PASS=$((PASS + 1)); }
fail() { echo "    FALHOU: $*" >&2; echo "    Resposta: $(cat "$BODY_FILE")" >&2; exit 1; }

# json CAMPO  -> le CAMPO (ex.: id, status, token) do ultimo corpo de resposta
json() {
  python3 -c '
import json, sys
d = json.load(open(sys.argv[1]))
for k in sys.argv[2].split("."):
    d = d[k]
print(d)
' "$BODY_FILE" "$1"
}

# call METODO CAMINHO CODIGO_ESPERADO [JSON]
call() {
  local method="$1" path="$2" expected="$3" data="${4:-}"
  local args=(-s -o "$BODY_FILE" -w '%{http_code}' -X "$method" "$BASE_URL$path" -H 'Content-Type: application/json')
  if [ -n "$TOKEN" ] && [[ "$path" == /api/* ]]; then args+=(-H "Authorization: Bearer $TOKEN"); fi
  if [ -n "$data" ]; then args+=(-d "$data"); fi
  local code
  code="$(curl "${args[@]}")"
  [ "$code" = "$expected" ] || fail "$method $path devolveu HTTP $code (esperado $expected)"
}

# try METODO CAMINHO [JSON] -> melhor esforco (limpeza), nunca derruba o teste
try() {
  local method="$1" path="$2" data="${3:-}"
  local args=(-s -o /dev/null -X "$method" "$BASE_URL$path" -H 'Content-Type: application/json' -H "Authorization: Bearer $TOKEN")
  if [ -n "$data" ]; then args+=(-d "$data"); fi
  curl "${args[@]}" || true
}

stock() {
  call GET "/api/materials/$MATERIAL_ID" 200
  json stockQuantity
}

expect_field() {
  local value
  value="$(json "$1")"
  [ "$value" = "$2" ] || fail "campo '$1' = '$value' (esperado '$2')"
  ok "$1 = $value"
}

step "1. Health check (Actuator)"
call GET /actuator/health 200
expect_field status UP

step "2. Login do funcionario (JWT)"
call POST /auth/login 200 "{\"email\":\"$EMPLOYEE_EMAIL\",\"password\":\"$EMPLOYEE_PASSWORD\"}"
TOKEN="$(json token)"
EMPLOYEE_ID="$(json employeeId)"
ok "token recebido, employeeId=$EMPLOYEE_ID"

step "3. Sem token a API protegida responde 401"
SAVED="$TOKEN"; TOKEN=""
call GET /api/service-orders 401
TOKEN="$SAVED"
ok "401 sem token"

step "4. Cliente: aponta o e-mail do seed para $CLIENT_EMAIL"
call GET "/api/customers/document?document=$DOCUMENT" 200
CUSTOMER_ID="$(json id)"
CUSTOMER_NAME="$(json name)"
CUSTOMER_PHONE="$(json phone)"
call PUT "/api/customers/$CUSTOMER_ID" 200 \
  "{\"name\":\"$CUSTOMER_NAME\",\"phone\":\"$CUSTOMER_PHONE\",\"email\":\"$CLIENT_EMAIL\"}"
expect_field email "$CLIENT_EMAIL"

new_order() {
  # Cria veiculo (placa aleatoria), servico e abre a OS ja com servico e peca.
  local plate="TST$(printf '%04d' $((RANDOM % 10000)))"
  call POST /api/vehicles 201 \
    "{\"customerId\":$CUSTOMER_ID,\"brand\":\"Fiat\",\"model\":\"Uno\",\"plate\":\"$plate\",\"year\":2019}"
  ok "veiculo criado, placa $plate"
  call POST /api/services 201 \
    "{\"name\":\"Troca de oleo $plate\",\"description\":\"Teste de fumaca\",\"price\":150.0}"
  local service_id
  service_id="$(json id)"
  call POST /api/service-orders 201 \
    "{\"customerDocument\":\"$DOCUMENT\",\"plate\":\"$plate\",\"employeeId\":$EMPLOYEE_ID,\"serviceIds\":[$service_id],\"materials\":[{\"materialId\":$MATERIAL_ID,\"quantity\":1}]}"
  ORDER_ID="$(json id)"
  expect_field status RECEIVED
  ok "OS $ORDER_ID aberta com 1 servico e 1 peca"
}

step "5. Abertura da OS (cliente, veiculo, servico e peca) devolve o id"
new_order
ORDER_A="$ORDER_ID"

step "6. Diagnostico"
call POST "/api/service-orders/$ORDER_A/diagnosis" 200 "{\"employeeId\":$EMPLOYEE_ID}"
expect_field status IN_DIAGNOSIS

step "7. Envio do orcamento ao cliente (E-MAIL 1: orcamento enviado)"
call POST "/api/service-orders/$ORDER_A/budget" 200 "{\"employeeId\":$EMPLOYEE_ID}"
expect_field status AWAITING_APPROVAL

step "8. Consulta publica de status, sem token"
call GET "/public/service-orders/$ORDER_A?document=$DOCUMENT" 200
expect_field status AWAITING_APPROVAL

step "9. Listagem so com OS ativas, mais prioritarias primeiro"
call GET /api/service-orders 200
python3 - "$BODY_FILE" "$ORDER_A" <<'PY'
import json, sys
orders = json.load(open(sys.argv[1]))
mine = int(sys.argv[2])
prio = {"IN_EXECUTION": 1, "AWAITING_APPROVAL": 2, "IN_DIAGNOSIS": 3, "RECEIVED": 4}
bad = [o["status"] for o in orders if o["status"] in ("FINISHED", "DELIVERED")]
if bad:
    sys.exit("listagem trouxe OS encerradas: %s" % bad)
ranks = [prio.get(o["status"], 99) for o in orders]
if ranks != sorted(ranks):
    sys.exit("listagem fora de ordem de prioridade: %s" % [o["status"] for o in orders])
if mine not in [o["id"] for o in orders]:
    sys.exit("a OS %d nao aparece na listagem" % mine)
print("    ordem dos status:", [o["status"] for o in orders])
PY
ok "listagem ordenada e sem FINISHED/DELIVERED"

if [ "$QUICK" != "1" ]; then
  step "9b. Prova de ordenacao e estoque: mais 3 OS, uma em cada estado"
  STOCK_BEFORE="$(stock)"
  new_order; ORDER_RECEIVED="$ORDER_ID"
  new_order; ORDER_DIAG="$ORDER_ID"
  call POST "/api/service-orders/$ORDER_DIAG/diagnosis" 200 "{\"employeeId\":$EMPLOYEE_ID}"
  new_order; ORDER_EXEC="$ORDER_ID"
  call POST "/api/service-orders/$ORDER_EXEC/diagnosis" 200 "{\"employeeId\":$EMPLOYEE_ID}"
  call POST "/api/service-orders/$ORDER_EXEC/budget" 200 "{\"employeeId\":$EMPLOYEE_ID}"
  call POST "/public/service-orders/$ORDER_EXEC/approval" 200 "{\"customerDocument\":\"$DOCUMENT\",\"approved\":true}"
  expect_field status IN_EXECUTION
  STOCK_AFTER="$(stock)"
  [ "$STOCK_AFTER" = "$((STOCK_BEFORE - 1))" ] || fail "estoque deveria cair 1 na aprovacao (antes $STOCK_BEFORE, depois $STOCK_AFTER)"
  ok "aprovacao baixou 1 un. do estoque ($STOCK_BEFORE -> $STOCK_AFTER)"

  call GET /api/service-orders 200
  python3 - "$BODY_FILE" "$ORDER_EXEC" "$ORDER_A" "$ORDER_DIAG" "$ORDER_RECEIVED" <<'PY'
import json, sys
orders = [o["id"] for o in json.load(open(sys.argv[1]))]
expected = [int(x) for x in sys.argv[2:]]   # EXECUCAO, AGUARDANDO, DIAGNOSTICO, RECEBIDA
missing = [i for i in expected if i not in orders]
if missing:
    sys.exit("OS ausentes da listagem: %s" % missing)
got = [i for i in orders if i in expected]
if got != expected:
    sys.exit("ordem errada: esperado %s, veio %s" % (expected, got))
print("    ordem confirmada (execucao > aguardando > diagnostico > recebida):", got)
PY
  ok "4 OS em estados diferentes saem na ordem de prioridade"

  call GET "/api/service-orders?status=AWAITING_APPROVAL" 200
  python3 - "$BODY_FILE" "$ORDER_A" <<'PY'
import json, sys
orders = json.load(open(sys.argv[1]))
if any(o["status"] != "AWAITING_APPROVAL" for o in orders):
    sys.exit("filtro por status trouxe outro status")
if int(sys.argv[2]) not in [o["id"] for o in orders]:
    sys.exit("OS esperada nao veio no filtro")
PY
  ok "filtro ?status=AWAITING_APPROVAL devolve so esse status"

  call GET "/api/service-orders?status=NAO_EXISTE" 400
  ok "status invalido devolve 400"

  # Limpeza: cancela as 3 OS de apoio (a em execucao devolve a peca ao estoque).
  for id in "$ORDER_EXEC" "$ORDER_DIAG" "$ORDER_RECEIVED"; do
    try POST "/api/service-orders/$id/cancellation" "{\"employeeId\":$EMPLOYEE_ID,\"reason\":\"Limpeza do teste de fumaca\"}"
  done
  STOCK_RESTORED="$(stock)"
  [ "$STOCK_RESTORED" = "$STOCK_BEFORE" ] || fail "cancelamento deveria devolver a peca (esperado $STOCK_BEFORE, veio $STOCK_RESTORED)"
  ok "cancelamento devolveu a peca ao estoque ($STOCK_RESTORED)"
fi

step "10. Aprovacao do orcamento pelo cliente (E-MAIL 2: orcamento aprovado)"
call POST "/public/service-orders/$ORDER_A/approval" 200 "{\"customerDocument\":\"$DOCUMENT\",\"approved\":true}"
expect_field status IN_EXECUTION

step "11. Finalizacao do servico (E-MAIL 3: servico finalizado)"
call POST "/api/service-orders/$ORDER_A/completion" 200 \
  "{\"employeeId\":$EMPLOYEE_ID,\"observation\":\"Servicos concluidos e testados\"}"
expect_field status FINISHED

step "12. Entrega ao cliente"
call POST "/api/service-orders/$ORDER_A/delivery" 200 "{\"employeeId\":$EMPLOYEE_ID}"
expect_field status DELIVERED

step "13. OS entregue sai da listagem (exclusao logica)"
call GET /api/service-orders 200
python3 - "$BODY_FILE" "$ORDER_A" <<'PY'
import json, sys
ids = [o["id"] for o in json.load(open(sys.argv[1]))]
if int(sys.argv[2]) in ids:
    sys.exit("a OS entregue ainda aparece na listagem")
PY
ok "OS $ORDER_A fora da listagem"

step "13b. Historico da OS registra cada transicao"
call GET "/api/service-orders/$ORDER_A" 200
python3 - "$BODY_FILE" <<'PY'
import json, sys
h = [x["status"] for x in json.load(open(sys.argv[1]))["history"]]
need = ["IN_DIAGNOSIS", "AWAITING_APPROVAL", "IN_EXECUTION", "FINISHED", "DELIVERED"]
missing = [n for n in need if n not in h]
if missing:
    sys.exit("historico sem as transicoes %s (veio %s)" % (missing, h))
print("    historico:", h)
PY
ok "historico completo"

step "13c. Erros esperados da API"
call GET /api/service-orders/999999999 404
ok "OS inexistente devolve 404"
call POST "/api/service-orders/$ORDER_A/delivery" 409 "{\"employeeId\":$EMPLOYEE_ID}"
ok "segunda entrega da mesma OS devolve 409"
call POST "/api/service-orders/$ORDER_A/services" 409 "{\"serviceId\":1,\"employeeId\":$EMPLOYEE_ID}"
ok "adicionar servico a OS entregue devolve 409"

if [ "$REJECT" = "1" ]; then
  step "14. Segunda OS: orcamento recusado (E-MAIL 4: orcamento recusado)"
  new_order
  ORDER_B="$ORDER_ID"
  call POST "/api/service-orders/$ORDER_B/diagnosis" 200 "{\"employeeId\":$EMPLOYEE_ID}"
  call POST "/api/service-orders/$ORDER_B/budget" 200 "{\"employeeId\":$EMPLOYEE_ID}"
  call POST "/public/service-orders/$ORDER_B/approval" 200 \
    "{\"customerDocument\":\"$DOCUMENT\",\"approved\":false,\"reason\":\"Valor acima do esperado\"}"
  ok "orcamento da OS $ORDER_B recusado, status $(json status)"
fi

echo
echo "=== Fumaca OK: $PASS verificacoes passaram. ==="
echo "Confira a caixa de entrada (e o spam) de $CLIENT_EMAIL: devem chegar os e-mails"
echo "'Oficina Mecanica | OS n $ORDER_A: ...' (orcamento pronto, aprovado, concluido"
echo "e, com REJECT=1, recusado). Sem QUICK=1 chegam tambem os e-mails das OS de apoio."
