# PostgreSQL gerenciado. A API acessa pelo Cloud SQL Auth Proxy (sidecar), que
# autentica por IAM: nao ha rede autorizada nem senha trafegando em claro.
resource "google_sql_database_instance" "oficina" {
  name             = "oficina-db"
  region           = var.region
  database_version = "POSTGRES_16"

  # Facilita o teardown no fim do tech challenge (evita custo residual).
  deletion_protection = false

  settings {
    tier              = var.db_tier
    edition           = "ENTERPRISE"
    availability_type = "ZONAL"
    disk_type         = "PD_SSD"
    disk_size         = 10

    backup_configuration {
      enabled = true
    }

    ip_configuration {
      ipv4_enabled = true
    }
  }

  depends_on = [google_project_service.apis]
}

resource "google_sql_database" "oficina" {
  name     = "oficina_mecanica"
  instance = google_sql_database_instance.oficina.name
}

resource "google_sql_user" "oficina" {
  name     = "oficina"
  instance = google_sql_database_instance.oficina.name
  password = var.db_password
}
