# ShopSphere Docker Deployment Guide

## Overview

This guide explains how to run ShopSphere services using Docker with proper dependency management.

## Architecture Layers

```
┌─────────────────────────────────────────┐
│         API Gateway (8080)              │
└─────────────────────────────────────────┘
                  ↓
┌─────────────────────────────────────────┐
│    Discovery Server (8761)              │
│    Config Server (8888)                 │
└─────────────────────────────────────────┘
                  ↓
┌─────────────────────────────────────────┐
│  Business Services (Auth, User, etc.)   │
└─────────────────────────────────────────┘
                  ↓
┌─────────────────────────────────────────┐
│  Infrastructure (Postgres, Mongo, etc.) │
└─────────────────────────────────────────┘
```

## Approach 1: Run Individual Services

Each service has its own Dockerfile. To run a specific service with all dependencies:

### Example: Run Checkout Service

```bash
# Build the project first
mvn clean package -DskipTests

# Start infrastructure + dependencies for checkout
docker-compose -f docker-compose.optimized.yml up -d postgres redis kafka mongodb discovery-server

# Wait for discovery to be healthy (30-40 seconds)
docker-compose -f docker-compose.optimized.yml ps

# Start dependent services
docker-compose -f docker-compose.optimized.yml up -d cart-service inventory-service pricing-service coupon-service catalog-service

# Finally start checkout service
docker-compose -f docker-compose.optimized.yml up -d checkout-service
```

### Service Dependencies Reference

| Service | Depends On |
|---------|-----------|
| **discovery-server** | None (starts first) |
| **config-server** | discovery-server |
| **api-gateway** | discovery-server, config-server |
| **auth-service** | postgres, discovery-server |
| **user-service** | postgres, discovery-server |
| **catalog-service** | mongodb, kafka, discovery-server |
| **inventory-service** | postgres, kafka, discovery-server |
| **cart-service** | redis, discovery-server, catalog-service, inventory-service |
| **pricing-service** | postgres, discovery-server |
| **coupon-service** | postgres, discovery-server |
| **checkout-service** | discovery-server, cart-service, inventory-service, pricing-service, coupon-service |
| **order-service** | postgres, kafka, discovery-server |
| **payment-service** | postgres, kafka, discovery-server |
| **fraud-service** | postgres, kafka, discovery-server |
| **shipping-service** | postgres, kafka, discovery-server, order-service |
| **returns-service** | postgres, kafka, discovery-server, order-service |
| **notification-service** | kafka, discovery-server |
| **review-service** | postgres, kafka, discovery-server |
| **recommendation-service** | mongodb, kafka, discovery-server |
| **search-service** | elasticsearch, kafka, discovery-server |
| **media-service** | postgres, discovery-server |
| **websocket-chat** | postgres, redis, discovery-server |
| **admin-service** | postgres, discovery-server |
| **batch-service** | postgres, discovery-server |
| **analytics-service** | mongodb, kafka, discovery-server |

## Approach 2: Run Entire Project (Recommended)

### Prerequisites

1. **Build the project:**
   ```bash
   mvn clean package -DskipTests
   ```

2. **Ensure Docker has enough resources:**
   - Memory: 8GB minimum (16GB recommended)
   - CPU: 4 cores minimum
   - Disk: 20GB free space

### Start All Services

```bash
# Start everything
docker-compose -f docker-compose.optimized.yml up -d

# View logs
docker-compose -f docker-compose.optimized.yml logs -f

# Check service health
docker-compose -f docker-compose.optimized.yml ps
```

### Startup Order (Automatic)

1. **Infrastructure (30-40s)**
   - PostgreSQL
   - MongoDB
   - Redis
   - Kafka
   - Elasticsearch

2. **Core Services (40-50s)**
   - Discovery Server (Eureka)
   - Config Server
   - API Gateway

3. **Business Services (60-90s)**
   - All microservices start in parallel with proper dependency checks

### Access Points

| Service | URL | Description |
|---------|-----|-------------|
| API Gateway | http://localhost:8080 | Main entry point |
| Eureka Dashboard | http://localhost:8761 | Service registry |
| Config Server | http://localhost:8888 | Configuration management |
| PostgreSQL | localhost:5432 | Database |
| MongoDB | localhost:27017 | Document store |
| Redis | localhost:6379 | Cache |
| Kafka | localhost:9092 | Message broker |
| Elasticsearch | http://localhost:9200 | Search engine |

### Service Ports

```
8080 - API Gateway
8761 - Discovery Server
8888 - Config Server
8081 - Auth Service
8082 - User Service
8083 - Catalog Service
8084 - Inventory Service
8085 - Cart Service
8086 - Checkout Service
8087 - Payment Service
8088 - Coupon Service
8089 - Admin Service
8090 - Batch Service
8091 - Analytics Service
8092 - WebSocket Chat
8093 - Pricing Service
8094 - Notification Service
8095 - Media Service
8002 - Order Service
8003 - Shipping Service
8009 - Returns Service
8010 - Fraud Service
8011 - Recommendation Service
8012 - Review Service
8013 - Search Service
```

## Common Commands

### Stop All Services
```bash
docker-compose -f docker-compose.optimized.yml down
```

### Stop and Remove Volumes (Clean Slate)
```bash
docker-compose -f docker-compose.optimized.yml down -v
```

### Rebuild Specific Service
```bash
# Rebuild the JAR
mvn clean package -DskipTests -pl services/cart-service -am

# Rebuild and restart the container
docker-compose -f docker-compose.optimized.yml up -d --build cart-service
```

### View Logs for Specific Service
```bash
docker-compose -f docker-compose.optimized.yml logs -f cart-service
```

### Scale Services (if needed)
```bash
docker-compose -f docker-compose.optimized.yml up -d --scale cart-service=3
```

## Troubleshooting

### Service Won't Start

1. **Check logs:**
   ```bash
   docker-compose -f docker-compose.optimized.yml logs service-name
   ```

2. **Check if dependencies are healthy:**
   ```bash
   docker-compose -f docker-compose.optimized.yml ps
   ```

3. **Restart specific service:**
   ```bash
   docker-compose -f docker-compose.optimized.yml restart service-name
   ```

### Out of Memory

Reduce services or increase Docker memory:
```bash
# Run only essential services
docker-compose -f docker-compose.optimized.yml up -d postgres mongodb redis kafka discovery-server api-gateway auth-service user-service catalog-service
```

### Port Conflicts

Check if ports are already in use:
```bash
# Windows
netstat -ano | findstr :8080

# Kill process if needed
taskkill /PID <PID> /F
```

### Database Connection Issues

Wait for health checks to pass:
```bash
# Check postgres health
docker exec shopsphere-postgres pg_isready -U postgres

# Check mongodb health
docker exec shopsphere-mongodb mongosh --eval "db.adminCommand('ping')"
```

## Performance Optimization

### Build Optimization

Use Maven parallel builds:
```bash
mvn clean package -DskipTests -T 4
```

### Docker Build Cache

Keep Dockerfiles unchanged to leverage layer caching.

### Resource Limits

Add resource limits in docker-compose if needed:
```yaml
services:
  service-name:
    deploy:
      resources:
        limits:
          cpus: '0.5'
          memory: 512M
```

## Production Considerations

1. **Use environment-specific configs**
2. **Enable health checks on all services**
3. **Set up proper logging (ELK stack)**
4. **Use Docker secrets for sensitive data**
5. **Implement service mesh (Istio/Linkerd) for production**
6. **Use Kubernetes for orchestration at scale**

## Quick Start Commands

```bash
# Full stack startup
mvn clean package -DskipTests && docker-compose -f docker-compose.optimized.yml up -d

# Check everything is running
docker-compose -f docker-compose.optimized.yml ps

# View all logs
docker-compose -f docker-compose.optimized.yml logs -f

# Shutdown
docker-compose -f docker-compose.optimized.yml down
```
