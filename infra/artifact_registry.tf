resource "google_artifact_registry_repository" "oficina" {
  location      = var.region
  repository_id = "oficina"
  description   = "Imagens Docker da API da oficina mecanica"
  format        = "DOCKER"

  # Mantem so as 10 imagens mais recentes para nao acumular custo de storage.
  cleanup_policies {
    id     = "keep-recent"
    action = "KEEP"
    most_recent_versions {
      keep_count = 10
    }
  }

  depends_on = [google_project_service.apis]
}
