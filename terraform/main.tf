terraform {
  required_providers {
    docker = {
      source  = "kreuzwerker/docker"
      version = "~> 3.0"
    }
  }
}

provider "docker" {}

# ============================================
# NETWORK
# Isolated network for containers
# ============================================
resource "docker_network" "pingwatch_network" {
  name   = "pingwatch-network-tf"
  driver = "bridge"
}

# ============================================
# VOLUME
# Persistent storage for PostgreSQL
# ============================================
resource "docker_volume" "postgres_data" {
  name = "pingwatch-postgres-data-tf"
}

# ============================================
# POSTGRESQL
# ============================================
resource "docker_image" "postgres" {
  name         = "postgres:15-alpine"
  keep_locally = true
}

resource "docker_container" "postgres" {
  name  = "pingwatch-db-tf"
  image = docker_image.postgres.image_id

  env = [
    "POSTGRES_DB=${var.db_name}",
    "POSTGRES_USER=${var.db_user}",
    "POSTGRES_PASSWORD=${var.db_password}"
  ]

  volumes {
    volume_name    = docker_volume.postgres_data.name
    container_path = "/var/lib/postgresql/data"
  }

  networks_advanced {
    name = docker_network.pingwatch_network.name
  }

  healthcheck {
    test     = ["CMD-SHELL", "pg_isready -U ${var.db_user}"]
    interval = "10s"
    timeout  = "5s"
    retries  = 5
  }

  restart = "unless-stopped"
}

# ============================================
# PINGWATCH APPLICATION
# ============================================
resource "docker_image" "pingwatch" {
  name         = "pingwatch:1.0"
  keep_locally = true
}

resource "docker_container" "pingwatch" {
  name  = "pingwatch-app-tf"
  image = docker_image.pingwatch.image_id

  ports {
    internal = 8080
    external = var.app_port
  }

  env = [
    "SPRING_DATASOURCE_URL=jdbc:postgresql://${docker_container.postgres.name}:5432/${var.db_name}",
    "SPRING_DATASOURCE_USERNAME=${var.db_user}",
    "SPRING_DATASOURCE_PASSWORD=${var.db_password}",
    "SPRING_JPA_HIBERNATE_DDL_AUTO=update"
  ]

  networks_advanced {
    name = docker_network.pingwatch_network.name
  }

  depends_on = [docker_container.postgres]

  restart = "unless-stopped"
}