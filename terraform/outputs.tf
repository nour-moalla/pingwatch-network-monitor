output "api_url" {
  value       = "http://localhost:${var.app_port}/api"
  description = "PingWatch API base URL"
}

output "dashboard_url" {
  value       = "http://localhost:${var.app_port}/api/dashboard"
  description = "PingWatch dashboard URL"
}

output "app_container" {
  value       = docker_container.pingwatch.name
  description = "App container name"
}

output "db_container" {
  value       = docker_container.postgres.name
  description = "Database container name"
}