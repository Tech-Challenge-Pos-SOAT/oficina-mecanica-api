# Infraestrutura como codigo (Terraform, Google Cloud)

Provisiona todo o ambiente do Tech Challenge Fase 2. O estado fica em um
bucket do Cloud Storage (criado pelo `bootstrap.sh`).

## Recursos criados

| Arquivo | Recursos |
|---|---|
| `apis.tf` | `google_project_service.apis` — habilita as APIs do projeto (GKE, Cloud SQL, Artifact Registry, IAM) |
| `artifact_registry.tf` | `google_artifact_registry_repository.oficina` — repositorio Docker |
| `gke.tf` | `google_container_cluster.oficina` — cluster GKE Autopilot `oficina-cluster` |
| `cloudsql.tf` | `google_sql_database_instance.oficina` (PostgreSQL 16), `google_sql_database.oficina`, `google_sql_user.oficina` |
| `iam.tf` | `google_service_account.api` (Workload Identity da API), `google_project_iam_member.api_cloudsql`, `google_service_account_iam_member.api_workload_identity`, `google_artifact_registry_repository_iam_member.nodes_pull_images` (leitura do Artifact Registry pelos nos), `google_service_account.deployer` (CD), `google_project_iam_member.deployer_artifact_writer`, `google_project_iam_member.deployer_gke`, `google_iam_workload_identity_pool.github`, `google_iam_workload_identity_pool_provider.github`, `google_service_account_iam_member.deployer_wif` |

## Aplicar

```bash
./bootstrap.sh SEU_PROJECT_ID                    # APIs minimas + bucket do estado
cp terraform.tfvars.example terraform.tfvars     # ajuste project_id e github_repository
export TF_VAR_db_password='uma-senha-forte'
terraform init -backend-config="bucket=SEU_PROJECT_ID-tfstate"
terraform plan
terraform apply
```

Os `outputs` entregam os valores que o pipeline e o ConfigMap precisam (veja
`docs/deploy-gcp.md`).

## Destruir

Depois da avaliacao, para zerar o custo:

```bash
terraform destroy
```

## Problemas conhecidos

### ImagePullBackOff nos pods

Nos do Autopilot usam a service account padrao do Compute Engine, que por
padrao nao tem `roles/artifactregistry.reader`. Diagnostico:

```bash
kubectl -n oficina describe pod POD_COM_ERRO
```

Correcao: `terraform apply` (recurso `nodes_pull_images` acima) ou, em
ambiente ja criado, veja o comando `gcloud artifacts ... add-iam-policy-binding`
em `docs/deploy-gcp.md`.
