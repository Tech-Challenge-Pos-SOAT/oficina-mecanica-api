variable "project_id" {
  description = "ID do projeto no Google Cloud"
  type        = string
}

variable "region" {
  description = "Regiao do cluster, do banco e do registry"
  type        = string
  default     = "us-central1"
}

variable "github_repository" {
  description = "Repositorio do GitHub no formato dono/nome, autorizado a fazer deploy via Workload Identity Federation"
  type        = string
}

variable "db_password" {
  description = "Senha do usuario do banco (passe por TF_VAR_db_password; nunca commite)"
  type        = string
  sensitive   = true
}

variable "db_tier" {
  description = "Tamanho da instancia do Cloud SQL"
  type        = string
  default     = "db-f1-micro"
}

variable "namespace" {
  description = "Namespace do Kubernetes onde a API roda"
  type        = string
  default     = "oficina"
}
