# Infraestrutura como codigo (Terraform, Google Cloud)

Cria: APIs necessarias, Artifact Registry, cluster GKE Autopilot, Cloud SQL
(PostgreSQL 16), service accounts e Workload Identity Federation para o GitHub
Actions. O estado fica em um bucket do Cloud Storage.

```bash
./bootstrap.sh SEU_PROJECT_ID                    # APIs minimas + bucket do estado
cp terraform.tfvars.example terraform.tfvars     # ajuste project_id e github_repository
export TF_VAR_db_password='uma-senha-forte'
terraform init -backend-config="bucket=SEU_PROJECT_ID-tfstate"
terraform plan
terraform apply
```

Os `outputs` entregam os valores que o pipeline e o ConfigMap precisam (veja
`docs/deploy-gcp.md`). Para evitar custo depois da entrega: `terraform destroy`.
