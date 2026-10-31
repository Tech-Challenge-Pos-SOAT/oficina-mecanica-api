output "cluster_name" {
  value = google_container_cluster.oficina.name
}

output "region" {
  value = var.region
}

output "artifact_registry_image" {
  description = "Caminho base da imagem (sem a tag)"
  value       = "${var.region}-docker.pkg.dev/${var.project_id}/${google_artifact_registry_repository.oficina.repository_id}/oficina-mecanica-api"
}

output "instance_connection_name" {
  description = "Valor para o ConfigMap (INSTANCE_CONNECTION_NAME)"
  value       = google_sql_database_instance.oficina.connection_name
}

output "api_service_account" {
  value = google_service_account.api.email
}

output "wif_provider" {
  description = "Valor do GitHub variable/secret GCP_WORKLOAD_IDENTITY_PROVIDER"
  value       = google_iam_workload_identity_pool_provider.github.name
}

output "deployer_service_account" {
  description = "Valor do GitHub variable/secret GCP_SERVICE_ACCOUNT"
  value       = google_service_account.deployer.email
}
