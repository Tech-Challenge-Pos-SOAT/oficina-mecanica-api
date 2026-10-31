#!/usr/bin/env bash
# Deploy manual no GKE, pensado para o Cloud Shell (ja tem gcloud, docker,
# kubectl e terraform). Serve para validar a infraestrutura antes do pipeline.
# Rode na raiz do repositorio, depois de "terraform apply" em infra/:
#
#   export DB_PASSWORD='mesma-senha-do-terraform'
#   export JWT_SECRET="$(openssl rand -base64 48)"
#   export BREVO_SMTP_USERNAME='...' BREVO_SMTP_PASSWORD='...' MAIL_FROM='...'
#   ./scripts/deploy-gcp-manual.sh
set -euo pipefail

: "${DB_PASSWORD:?defina DB_PASSWORD (a mesma usada em TF_VAR_db_password)}"
: "${JWT_SECRET:?defina JWT_SECRET}"
BREVO_SMTP_USERNAME="${BREVO_SMTP_USERNAME:-nao-configurado}"
BREVO_SMTP_PASSWORD="${BREVO_SMTP_PASSWORD:-nao-configurado}"
MAIL_FROM="${MAIL_FROM:-oficina@exemplo.com}"

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "${ROOT}/infra"
IMAGE_BASE="$(terraform output -raw artifact_registry_image)"
INSTANCE="$(terraform output -raw instance_connection_name)"
CLUSTER="$(terraform output -raw cluster_name)"
REGION="$(terraform output -raw region)"
cd "${ROOT}"

PROJECT_ID="${INSTANCE%%:*}"
TAG="manual-$(date +%Y%m%d%H%M%S)"

echo ">> Build e push da imagem ${IMAGE_BASE}:${TAG}"
gcloud auth configure-docker "${REGION}-docker.pkg.dev" --quiet
docker build -t "${IMAGE_BASE}:${TAG}" .
docker push "${IMAGE_BASE}:${TAG}"

echo ">> Credenciais do cluster ${CLUSTER}"
gcloud container clusters get-credentials "${CLUSTER}" --region "${REGION}" --project "${PROJECT_ID}"

echo ">> Preparando manifestos (copia temporaria, o repositorio nao e alterado)"
WORK="$(mktemp -d)"
cp -r k8s "${WORK}/k8s"
sed -i "s|__PROJECT_ID__|${PROJECT_ID}|g" "${WORK}/k8s/serviceaccount.yaml"
sed -i "s|__INSTANCE_CONNECTION_NAME__|${INSTANCE}|g; s|seu-email-remetente@exemplo.com|${MAIL_FROM}|g" "${WORK}/k8s/configmap.yaml"
sed -i "s|newName: .*|newName: ${IMAGE_BASE}|; s|newTag: .*|newTag: ${TAG}|" "${WORK}/k8s/kustomization.yaml"

kubectl apply -f "${WORK}/k8s/namespace.yaml"
kubectl -n oficina create secret generic oficina-api-secret \
  --from-literal=DB_PASSWORD="${DB_PASSWORD}" \
  --from-literal=JWT_SECRET="${JWT_SECRET}" \
  --from-literal=BREVO_SMTP_USERNAME="${BREVO_SMTP_USERNAME}" \
  --from-literal=BREVO_SMTP_PASSWORD="${BREVO_SMTP_PASSWORD}" \
  --dry-run=client -o yaml | kubectl apply -f -

echo ">> Aplicando manifestos"
kubectl apply -k "${WORK}/k8s"
kubectl -n oficina rollout status deployment/oficina-api --timeout=420s

echo ">> Aguardando o IP publico do LoadBalancer"
for _ in $(seq 1 40); do
  IP="$(kubectl -n oficina get svc oficina-api -o jsonpath='{.status.loadBalancer.ingress[0].ip}' 2>/dev/null || true)"
  [ -n "${IP}" ] && break
  sleep 10
done

kubectl -n oficina get pods,hpa,svc
echo
echo "API:     http://${IP:-<aguarde e rode: kubectl -n oficina get svc>}"
echo "Health:  http://${IP:-IP}/actuator/health"
echo "Swagger: http://${IP:-IP}/swagger-ui.html"
