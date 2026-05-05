variable "db_password" {
  description = "PostgreSQL password"
  type        = string
  sensitive   = true
}

variable "db_name" {
  description = "Database name"
  type        = string
  default     = "pingwatch"
}

variable "db_user" {
  description = "Database user"
  type        = string
  default     = "postgres"
}

variable "app_port" {
  description = "Application exposed port"
  type        = number
  default     = 8080
}

variable "app_version" {
  description = "Application image version"
  type        = string
  default     = "latest"
}

