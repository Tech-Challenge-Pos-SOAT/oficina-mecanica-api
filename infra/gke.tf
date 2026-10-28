# GKE Autopilot: o Google gerencia os nos, ja vem com metrics-server (necessario
# para o HPA) e Workload Identity habilitado.
resource "google_container_cluster" "oficina" {
  name     = "oficina-cluster"
  location = var.region

  enable_autopilot    = true
  deletion_protection = false

  release_channel {
    channel = "REGULAR"
  }

  ip_allocation_policy {}

  depends_on = [google_project_service.apis]
}
