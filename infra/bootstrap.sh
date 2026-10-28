#!/usr/bin/env bash
# Passo zero (uma vez por projeto): habilita as APIs minimas e cria o bucket do
# estado remoto do Terraform. Uso: ./bootstrap.sh <PROJECT_ID> [REGIAO]
set -euo pipefail

PROJECT_ID="${1:?informe o PROJECT_ID}"
REGION="${2:-us-central1}"
BUCKET="${PROJECT_ID}-tfstate"

gcloud config set project "${PROJECT_ID}"
gcloud services enable serviceusage.googleapis.com cloudresourcemanager.googleapis.com storage.googleapis.com

if ! gcloud storage buckets describe "gs://${BUCKET}" >/dev/null 2>&1; then
  gcloud storage buckets create "gs://${BUCKET}" --location="${REGION}" --uniform-bucket-level-access
fi
gcloud storage buckets update "gs://${BUCKET}" --versioning

echo "Bucket do estado: gs://${BUCKET}"
echo "Proximo: terraform init -backend-config=\"bucket=${BUCKET}\""
