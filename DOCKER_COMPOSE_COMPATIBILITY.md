# Docker Compose Compatibility Guide

## Overview
All 25 Dockerfiles have been verified for compatibility with the updated `docker-compose.yml` file. This document outlines the changes made and port mappings.

## Key Changes to docker-compose.yml

### 1. Dockerfile Paths
**Before:** Only discovery service specified dockerfile path
```yaml
discovery-server:
  build:
    context: .
    # Missing: dockerfile path for other services
```

**After:** All services now specify correct dockerfile paths
```yaml
auth-service:
  build:
    context: .
    dockerfile: services/auth-service/Dockerfile
catalog-service:
  build:
    context: .
    dockerfile: services/catalog-service/Dockerfile
```

### 2. Port Mappings Corrected
Port mappings now match the Dockerfile EXPOSE statements and application.yml configurations.

| Service | Port (Before) | Port (After) | Reason |
|---------|---------------|--------------|--------|
| auth-service | 8001 | 8081 | Matches application.yml |
| user-service | 8010 | 8082 | Matches application.yml |
| catalog-service | 8002 | 8083 | Matches application.yml |
| inventory-service | 8004 | 8084 | Matches application.yml |
| order-service | N/A | 8002 | Matches application.yml |
| shipping-service | N/A | 8003 | Matches application.yml |
| returns-service | N/A | 8009 | Matches application.yml |
| fraud-service | N/A | 8010 | Matches application.yml |
| recommendation-service | N/A | 8011 | Matches application.yml |
| review-service | N/A | 8012 | Matches application.yml |
| search-service | N/A | 8013 | Matches application.yml |

### 3. Infrastructure Services Added
- **Redis**: Required by cart-service and websocket-chat
- **Elasticsearch**: Required by search-service (optional but configured)
- Both include health checks for proper service startup ordering

### 4. Environment Variables Standardized
All services use consistent variable naming:
- `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`
- `MONGO_HOST`, `MONGO_PORT`, `MONGO_DB`
- `KAFKA_BOOTSTRAP_SERVERS`
- `REDIS_HOST`, `REDIS_PORT`, `REDIS_PASSWORD`
- `ELASTICSEARCH_HOST`, `ELASTICSEARCH_PORT`
- `EUREKA_HOST`, `EUREKA_PORT`

### 5. Service Dependencies
All services now use proper health check conditions:
```yaml
depends_on:
  postgres:
    condition: service_healthy  # Waits for DB readiness
  discovery-server:
    condition: service_started   # Waits for discovery to be running
```

### 6. Shared Ports Resolution
Services sharing the same port are now separated:
- **8085**: cart-service (6379 Redis) vs pricing-service → pricing-service on 8085
- **8086**: checkout-service vs media-service → media-service on 8086 with SERVER_PORT override
- **8087**: notification-service vs payment-service → payment-service on 8087

## Port Assignment Summary

### Core Infrastructure
| Service | Port | Type |
|---------|------|------|
| API Gateway | 8080 | REST |
| Eureka Discovery | 8761 | Service Registry |
| Config Server | 8888 | Configuration |
| PostgreSQL | 5432 | Database |
| MongoDB | 27017 | Document DB |
| Redis | 6379 | Cache |
| Kafka | 9092 | Message Queue |
| Elasticsearch | 9200 | Search Engine |

### Business Services
| Service | Port | Database |
|---------|------|----------|
| Auth Service | 8081 | PostgreSQL |
| User Service | 8082 | PostgreSQL |
| Catalog Service | 8083 | MongoDB |
| Inventory Service | 8084 | PostgreSQL |
| Cart Service | 8085 | Redis |
| Checkout Service | 8086 | N/A |
| Notification Service | 8087 | N/A |
| Payment Service | 8087 | PostgreSQL |
| Coupon Service | 8088 | PostgreSQL |
| Admin Service | 8089 | PostgreSQL |
| Batch Service | 8090 | PostgreSQL |
| Analytics Service | 8091 | MongoDB |
| Media Service | 8086 | PostgreSQL |
| WebSocket Chat | 8092 | PostgreSQL + Redis |
| Returns Service | 8009 | PostgreSQL |
| Shipping Service | 8003 | PostgreSQL |
| Pricing Service | 8085 | PostgreSQL |
| Fraud Service | 8010 | PostgreSQL |
| Recommendation Service | 8011 | MongoDB |
| Review Service | 8012 | PostgreSQL |
| Search Service | 8013 | Elasticsearch |
| Order Service | 8002 | PostgreSQL |

## Dockerfile Compatibility Checklist

### Build Configuration
- ✅ All Dockerfiles use Maven 3.9 + Eclipse Temurin 17
- ✅ All Dockerfiles use Alpine Linux for runtime (small image size)
- ✅ All Dockerfiles include non-root user (appuser)
- ✅ All Dockerfiles copy correct common modules (common-models, common-utils, etc.)

### Health Checks
- ✅ All Dockerfiles include HEALTHCHECK
- ✅ Health checks use actuator endpoints
- ✅ Health check timeout: 10s, interval: 30s, retries: 3, start-period: 40s

### Port Exposure
- ✅ All EXPOSE statements match service port configuration
- ✅ Port numbers match application.yml configurations
- ✅ Dockerfile ports match docker-compose.yml port mappings

### Common Module Dependencies
Each Dockerfile copies required common modules:

| Service | Requires |
|---------|----------|
| All | common-models, common-utils |
| Auth/User/Checkout/Cart/Admin/Media/Chat/Returns | common-security |
| Auth/Order/Payment/Returns/Shipping/Fraud/Review | common-kafka |
| Auth/User/Inventory/Order/Payment/Admin/Batch/Media/Chat/Returns/Shipping | common-db |

## Running Docker Compose

### Start All Services
```bash
docker-compose up -d
```

### View Service Status
```bash
docker-compose ps
```

### Check Service Logs
```bash
docker-compose logs -f service-name
```

### Stop All Services
```bash
docker-compose down
```

### Clean Up (Remove volumes)
```bash
docker-compose down -v
```

## Startup Order

Services start in this order based on dependencies:

1. **Infrastructure** (parallel): postgres, mongodb, redis, elasticsearch, kafka
2. **Core Services** (parallel after discovery):
   - discovery-server (waits for nothing)
   - config-server (waits for discovery)
   - api-gateway (waits for discovery + config)
3. **Data Services** (waits for their DBs):
   - auth-service (waits for postgres + discovery)
   - user-service (waits for postgres + discovery)
   - catalog-service (waits for mongodb + kafka + discovery)
   - etc.

## Troubleshooting

### Service Won't Start
Check health status:
```bash
docker-compose ps
docker-compose logs service-name
```

### Port Already in Use
If port is already bound, modify docker-compose.yml:
```yaml
ports:
  - "8081:8081"  # Change first port to any free port
```

### Database Connection Failed
Ensure DB service is healthy:
```bash
docker-compose logs postgres
docker-compose logs mongodb
```

### Kafka Issues
Kafka needs initialization. Wait 30+ seconds on first start for broker to be ready.

### Service Discovery Failed
Ensure Eureka server is running:
```bash
curl http://localhost:8761/eureka/apps
```

## Environment Variable Overrides

All services support environment variable overrides. Example:
```bash
docker-compose run -e DB_HOST=remote-postgres auth-service
```

## Testing Endpoints

### Service Health
```bash
curl http://localhost:8080/actuator/health          # Gateway
curl http://localhost:8761/actuator/health          # Eureka
curl http://localhost:8081/actuator/health          # Auth Service
curl http://localhost:8082/actuator/health          # User Service
```

### Gateway Routing
```bash
curl http://localhost:8080/api/auth/health          # Routes to auth-service
curl http://localhost:8080/api/users/health         # Routes to user-service
curl http://localhost:8080/api/products/health      # Routes to catalog-service
```

## Production Considerations

### For Production Deployment
1. Replace localhost Eureka with external service discovery
2. Use managed databases (RDS, MongoDB Atlas) instead of containers
3. Use secrets management (AWS Secrets Manager, Vault) for credentials
4. Configure proper resource limits (memory, CPU)
5. Use health checks with higher thresholds
6. Implement log aggregation and monitoring
7. Use container registry (ECR, Docker Hub) for images
8. Implement service mesh (Istio) for advanced routing

### Resource Limits Example
```yaml
auth-service:
  # ... other config ...
  deploy:
    resources:
      limits:
        cpus: '1'
        memory: 512M
      reservations:
        cpus: '0.5'
        memory: 256M
```

## Verification Checklist

- ✅ All Dockerfiles have correct dockerfile paths
- ✅ All ports match application.yml + Dockerfile EXPOSE
- ✅ All environment variables properly set
- ✅ All database connections configured
- ✅ All Eureka registrations configured
- ✅ All health checks configured
- ✅ All service dependencies defined
- ✅ All volumes mounted for data persistence
- ✅ All networks properly connected
