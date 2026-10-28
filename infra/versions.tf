terraform {
  required_version = ">= 1.6.0"

  required_providers {
    google = {
      source  = "hashicorp/google"
      version = "~> 6.0"
    }
  }

  # Estado remoto no Cloud Storage. O bucket e' criado por bootstrap.sh e o nome
  # entra na inicializacao, para nao ficar fixo no codigo:
  #   terraform init -backend-config="bucket=<PROJECT_ID>-tfstate"
  backend "gcs" {
    prefix = "oficina-mecanica/state"
  }
}

provider "google" {
  project = var.project_id
  region  = var.region
}
