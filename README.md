# 🔍 PingWatch — Network Monitoring Platform

> A production-grade network monitoring API built with Java/Spring Boot, containerized with Docker, deployed on Kubernetes, and secured with a full DevSecOps CI/CD pipeline.

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.5-green)
![Docker](https://img.shields.io/badge/Docker-29.1.5-blue)
![Kubernetes](https://img.shields.io/badge/Kubernetes-minikube-blue)
![Terraform](https://img.shields.io/badge/Terraform-1.15.1-purple)
![CI/CD](https://img.shields.io/badge/CI%2FCD-GitHub%20Actions-black)

---

## 📋 Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Tech Stack](#tech-stack)
- [API Endpoints](#api-endpoints)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
- [Docker](#docker)
- [Kubernetes](#kubernetes)
- [Terraform](#terraform)
- [CI/CD Pipeline](#cicd-pipeline)
- [Blue-Green Deployment](#blue-green-deployment)

---

## Overview

PingWatch monitors network hosts in real-time by performing HTTP and ICMP checks every 60 seconds. It tracks uptime percentage, latency history, and status changes — exposing everything through a clean REST API.

**Key features:**
- Auto-monitoring every 60 seconds for all active hosts
- HTTP check with ICMP fallback
- Uptime percentage and average latency statistics
- Per-host ping history (last 20 results)
- Dashboard summary across all monitored hosts

---

## Architecture

```
┌─────────────────────────────────────────────────┐
│                  GitHub Actions                  │
│  Gitleaks → Maven → SonarCloud → OWASP → Trivy  │
└──────────────────────┬──────────────────────────┘
                       │ push
                       ▼
                  Docker Hub
                       │
          ┌────────────┴────────────┐
          │      Kubernetes         │
          │  ┌──────────────────┐   │
          │  │  pingwatch-app   │   │
          │  │  (2 replicas)    │   │
          │  └────────┬─────────┘   │
          │           │             │
          │  ┌────────▼─────────┐   │
          │  │   postgres-db    │   │
          │  └──────────────────┘   │
          └─────────────────────────┘
```

---

## Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java 17, Spring Boot 3.3.5 |
| Database | PostgreSQL 15 |
| ORM | Spring Data JPA / Hibernate |
| Containerization | Docker (multi-stage build) |
| Orchestration | Kubernetes (Minikube) |
| IaC | Terraform |
| CI/CD | GitHub Actions |
| SAST | SonarCloud |
| Dependency Scan | OWASP Dependency Check |
| Container Scan | Trivy |
| Secret Scanning | Gitleaks |

---

## API Endpoints

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/hosts` | Get all monitored hosts |
| `POST` | `/api/hosts` | Register a new host |
| `GET` | `/api/hosts/{id}` | Get host by ID |
| `PUT` | `/api/hosts/{id}` | Update host |
| `DELETE` | `/api/hosts/{id}` | Delete host |
| `POST` | `/api/hosts/{id}/ping` | Manually trigger ping |
| `GET` | `/api/hosts/{id}/history` | Get ping history |
| `GET` | `/api/hosts/{id}/stats` | Get host statistics |
| `GET` | `/api/dashboard` | Get dashboard summary |

### Example — Register a host

```bash
curl -X POST http://localhost:8080/api/hosts \
  -H "Content-Type: application/json" \
  -d '{
    "hostname": "google-dns",
    "ipAddress": "8.8.8.8",
    "port": 80,
    "description": "Google DNS server"
  }'
```

### Example — Dashboard response

```json
{
  "totalHosts": 3,
  "onlineHosts": 2,
  "offlineHosts": 1,
  "unknownHosts": 0,
  "uptimePercent": 66.67
}
```

---

## Project Structure

```
pingwatch/
├── src/main/java/com/pingwatch/
│   ├── model/              # JPA entities
│   ├── repository/         # Spring Data repositories
│   ├── service/            # Business logic + auto-monitor
│   ├── controller/         # REST controllers
│   └── PingwatchApplication.java
├── terraform/              # IaC — Docker infrastructure
├── kubernetes/             # K8s manifests
│   ├── namespace.yaml
│   ├── secret.yaml
│   ├── configmap.yaml
│   ├── postgres-deployment.yaml
│   ├── blue-deployment.yaml
│   ├── green-deployment.yaml
│   └── blue-green-service.yaml
├── scripts/
│   └── switch.sh           # Blue-green traffic switch
├── .github/workflows/
│   └── devsecops-pipeline.yml
├── Dockerfile
├── docker-compose.yml
└── .gitignore
```

---

## Getting Started

### Prerequisites

```
Java 17+
Maven 3.9+
Docker
PostgreSQL
```

### Run locally

```bash
# Clone the repo
git clone https://github.com/nour-moalla/pingwatch-network-monitor.git
cd pingwatch-network-monitor

# Configure database in application.properties
# Then run
./mvnw spring-boot:run
```

---

## Docker

### Build and run with Docker Compose

```bash
# Start both app and PostgreSQL
docker-compose --env-file .env up --build

# Stop
docker-compose down
```

### Build image manually

```bash
docker build -t pingwatch:1.0 .
docker run -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/pingwatch \
  -e SPRING_DATASOURCE_USERNAME=postgres \
  -e SPRING_DATASOURCE_PASSWORD=postgres \
  pingwatch:1.0
```

**Multi-stage build reduces image size by 73%** — from ~300MB (full JDK) to ~80MB (Alpine JRE).

---

## Kubernetes

```bash
# Start minikube
minikube start

# Deploy everything
kubectl apply -f kubernetes/namespace.yaml
kubectl apply -f kubernetes/secret.yaml
kubectl apply -f kubernetes/configmap.yaml
kubectl apply -f kubernetes/postgres-deployment.yaml
kubectl apply -f kubernetes/app-deployment.yaml

# Check status
kubectl get pods -n pingwatch

# Access the app
minikube service pingwatch-service -n pingwatch --url
```

---

## Terraform

```bash
cd terraform

terraform init
terraform plan -var-file="terraform.tfvars"
terraform apply -var-file="terraform.tfvars"

# Destroy
terraform destroy -var-file="terraform.tfvars"
```

---

## CI/CD Pipeline

Every push to `main` or `develop` triggers a 5-gate DevSecOps pipeline:

```
┌─────────────────┐
│ 1. Secret Scan  │  Gitleaks — detects leaked credentials
└────────┬────────┘
         │
┌────────▼────────┐
│ 2. Build & Test │  Maven compile + test
└────────┬────────┘
         │
    ┌────┴────┐
    │         │
┌───▼───┐ ┌──▼──────────┐
│ SAST  │ │ Dependency  │
│Sonar  │ │ Audit OWASP │
└───┬───┘ └──┬──────────┘
    └────┬───┘
         │
┌────────▼────────┐
│ 5. Trivy Scan   │  Container CVE scan
└────────┬────────┘
         │
┌────────▼────────┐
│  Docker Hub     │  Push image (main branch only)
└─────────────────┘
```

---

## Blue-Green Deployment

Zero-downtime deployment strategy on Kubernetes:

```bash
# Switch traffic to green (v2.0)
kubectl patch service pingwatch-service -n pingwatch \
  --type=merge \
  -p '{"spec":{"selector":{"version":"green"}}}'

# Switch back to blue (v1.0)
kubectl patch service pingwatch-service -n pingwatch \
  --type=merge \
  -p '{"spec":{"selector":{"version":"blue"}}}'
```

---

## Author

**Nour Moalla**
GitHub: [@nour-moalla](https://github.com/nour-moalla)
