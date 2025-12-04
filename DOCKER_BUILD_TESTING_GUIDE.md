# ShopSphere Docker Build & Testing Guide

## Overview

This guide explains how to compile, build, and test individual services and the entire ShopSphere platform using Docker and Docker Compose.

## Directory Structure

```
shopsphere/
├── services/
│   ├── auth-service/Dockerfile
│   ├── user-service/Dockerfile
│   ├── catalog-service/Dockerfile
│   ├── cart-service/Dockerfile
│   ├── inventory-service/Dockerfile
│   ├── order-service/Dockerfile
│   ├── payment-service/Dockerfile
│   ├── checkout-service/Dockerfile
│   ├── notification-service/Dockerfile
│   ├── shipping-service/Dockerfile
│   ├── review-service/Dockerfile
│   ├── fraud-service/Dockerfile
│   ├── pricing-service/Dockerfile
│   ├── media-service/Dockerfile
│   ├── search-service/Dockerfile
│   ├── recommendation-service/Dockerfile
│   ├── admin-service/Dockerfile
│   ├── batch-service/Dockerfile
│   ├── analytics-service/Dockerfile
│   ├── coupon-service/Dockerfile
│   ├── returns-service/Dockerfile
│   ├── api-gateway/Dockerfile
│   ├── discovery/Dockerfile
│   └── config-server/Dockerfile
├── docker-compose-full.yml
├── compose.yaml
└── pom.xml
```

## Prerequisites

- Docker (v20.10+)
- Docker Compose (v2.0+)
- Maven (v3.9+)
- Java 17 JDK

## Building Individual Services

Each service has its own Dockerfile with a two-stage build:

1. **Builder Stage**: Compiles the service using Maven
2. **Runtime Stage**: Lightweight Java runtime with only the JAR

### Build Single Service

```bash
# Build Auth Service
docker build -f services/auth-service/Dockerfile -t shopsphere-auth:latest .

# Build Order Service
docker build -f services/order-service/Dockerfile -t shopsphere-order:latest .

# Build all services
for service in services/*/Dockerfile; do
  service_name=$(basename $(dirname $service))
  docker build -f $service -t shopsphere-$service_name:latest .
done
```

### Build Dependencies Chain

Services have build dependencies on common modules. Each Dockerfile:

1. Copies parent pom.xml
2. Copies required common modules (common-models, common-db, common-kafka, etc.)
3. Copies the specific service
4. Builds with Maven, including dependencies

Example build command:
```bash
mvn -B clean package -pl services/order-service -am -DskipTests
```

Flags:
- `-pl`: Projects to build (specific service)
- `-am`: Also make (compile dependencies)
- `-DskipTests`: Skip tests during build
- `-B`: Batch mode (non-interactive)

## Testing Complete System with Docker Compose

### Full System Startup

```bash
# Start all services (infrastructure + microservices)
docker-compose -f docker-compose-full.yml up -d

# View startup logs
docker-compose -f docker-compose-full.yml logs -f

# Wait for services to be healthy (30-60 seconds)
docker-compose -f docker-compose-full.yml ps
```

### Service Startup Order

The docker-compose-full.yml is configured with proper dependency ordering:

1. **Infrastructure** (parallel): PostgreSQL, MongoDB, Zookeeper, Kafka
2. **Core Discovery**: Eureka Service Discovery
3. **Core Config**: Config Server
4. **API Gateway** (waits for infrastructure + Eureka)
5. **Microservices** (depends on discovery + required infrastructure)

### Health Check Verification

Each service has healthcheck endpoints. View status:

```bash
# Check all services
docker-compose -f docker-compose-full.yml ps

# Check specific service health
curl http://localhost:8001/actuator/health  # Auth Service
curl http://localhost:8005/actuator/health  # Order Service
curl http://localhost:8761/eureka/status    # Discovery Server
```

### Service Port Mappings

```
Discovery Server:  8761
Config Server:     8888
API Gateway:       8080

Auth Service:      8001
User Service:      8010
Catalog Service:   8002
Cart Service:      8003
Inventory Service: 8004
Order Service:     8005
Payment Service:   8006
Checkout Service:  8007
Notification Service: 8008
Shipping Service:  8009

Review Service:    8083
Fraud Service:     8084
Pricing Service:   8085
Media Service:     8086
Search Service:    8087
Recommendation Service: 8088
Admin Service:     8089
Batch Service:     8090
Analytics Service: 8091

Coupon Service:    8081
Returns Service:   8082

PostgreSQL:        5432
MongoDB:           27017
Kafka:             9092
Zookeeper:         2181
```

## Testing Individual Service Endpoints

### 1. Auth Service (8001)

```bash
# Health check
curl http://localhost:8001/actuator/health

# Register user
curl -X POST http://localhost:8001/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "SecurePassword123",
    "firstName": "John",
    "lastName": "Doe"
  }'
```

### 2. Order Service (8005)

```bash
# Health check
curl http://localhost:8005/actuator/health

# Get orders
curl http://localhost:8005/api/orders \
  -H "Authorization: Bearer <JWT_TOKEN>"
```

### 3. Payment Service (8006)

```bash
# Health check
curl http://localhost:8006/actuator/health

# Process payment
curl -X POST http://localhost:8006/api/payments \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": "ORD-001",
    "amount": 99.99,
    "currency": "USD"
  }'
```

### 4. API Gateway (8080)

```bash
# Gateway health
curl http://localhost:8080/actuator/health

# Route through gateway to auth service
curl http://localhost:8080/auth/api/auth/status
```

## Stopping and Cleanup

```bash
# Stop all services
docker-compose -f docker-compose-full.yml stop

# Stop and remove containers
docker-compose -f docker-compose-full.yml down

# Remove volumes (data loss)
docker-compose -f docker-compose-full.yml down -v

# Remove all ShopSphere images
docker rmi $(docker images | grep shopsphere | awk '{print $3}')
```

## Debugging Services

### View Service Logs

```bash
# Follow logs for specific service
docker-compose -f docker-compose-full.yml logs -f auth-service

# View last 100 lines
docker-compose -f docker-compose-full.yml logs --tail=100 order-service

# View all logs
docker-compose -f docker-compose-full.yml logs
```

### Execute Commands in Running Container

```bash
# Shell into Auth Service
docker exec -it shopsphere-auth sh

# View environment variables
docker exec shopsphere-auth env | grep SPRING

# Check database connectivity
docker exec shopsphere-auth curl -f http://localhost:8001/actuator/health
```

### Database Inspection

```bash
# Connect to PostgreSQL
docker exec -it shopsphere-postgres psql -U shopsphere -d shopsphere

# View tables
\dt

# Connect to MongoDB
docker exec -it shopsphere-mongodb mongosh -u shopsphere -p

# View collections
show collections
```

## Performance & Resource Management

### Resource Limits

Each service runs with:
- Memory: ~512MB allocated
- CPU: Unlimited (can be configured)

To add resource limits, modify docker-compose-full.yml:

```yaml
services:
  auth-service:
    # ... other config
    deploy:
      resources:
        limits:
          cpus: '1'
          memory: 512M
        reservations:
          cpus: '0.5'
          memory: 256M
```

### Monitoring

```bash
# View resource usage
docker stats

# Monitor specific service
docker stats shopsphere-auth

# Get detailed service info
docker inspect shopsphere-order
```

## Troubleshooting

### Service Fails to Start

1. Check logs: `docker-compose -f docker-compose-full.yml logs <service_name>`
2. Verify dependencies are healthy: `docker-compose -f docker-compose-full.yml ps`
3. Check environment variables are set correctly
4. Ensure required ports are available: `netstat -an | grep LISTEN`

### Database Connection Issues

```bash
# Test PostgreSQL connectivity
docker exec shopsphere-postgres pg_isready -U shopsphere

# Test MongoDB connectivity
docker exec shopsphere-mongodb mongosh --eval "db.adminCommand('ping')"

# Test Kafka connectivity
docker exec shopsphere-kafka kafka-broker-api-versions.sh --bootstrap-server localhost:9092
```

### Eureka Discovery Issues

```bash
# Check Eureka dashboard
curl http://localhost:8761/eureka/apps

# View registered services
curl http://localhost:8761/eureka/apps/auth-service
```

### Port Conflicts

If port is already in use:

```bash
# Find process using port
netstat -ano | findstr :8001  # Windows
lsof -i :8001                  # Linux/Mac

# Change port in docker-compose-full.yml
# Modify the ports section for conflicting service
```

## CI/CD Integration

The GitHub Actions workflow (`.github/workflows/ci.yml`) automatically:

1. Builds all modules with Maven
2. Runs unit tests
3. Performs security scans
4. Can optionally build Docker images

## Scaling Services

To run multiple instances of a service:

```bash
# Override scaling
docker-compose -f docker-compose-full.yml up -d --scale order-service=3

# Note: Requires load balancing configuration in API Gateway
```

## Network Isolation

All services communicate via the `shopsphere-network` Docker bridge network:

```bash
# View network details
docker network inspect shopsphere_shopsphere-network

# Services resolve by container name (e.g., postgres:5432, kafka:9092)
```

## Best Practices

1. **Always check logs first** when services fail
2. **Use health checks** to verify service readiness
3. **Environment variables** should not contain secrets in production
4. **Volume persistence** is handled for PostgreSQL, MongoDB, and Zookeeper
5. **Resource limits** prevent runaway containers
6. **Proper shutdown sequence** respects dependencies

## Performance Tips

1. **Build images once**: Images are cached, rebuilds only compile changes
2. **Use BuildKit**: Enables parallel layer builds (Docker buildx)
3. **Multi-stage builds**: Reduces final image size significantly
4. **Alpine base image**: Smaller than regular JDK images

## Next Steps

1. ✅ Build individual services
2. ✅ Verify all services start without errors
3. ✅ Test API endpoints through gateway
4. ✅ Monitor logs and health checks
5. ✅ Perform integration tests
6. ✅ Load test with K6 or similar
7. ✅ Deploy to production infrastructure

---

For additional information, see:
- `.github/workflows/ci.yml` - CI/CD pipeline
- `AGENTS.md` - Agent responsibilities and phases
- `DOCKER_COMPOSE.md` - Original compose documentation
