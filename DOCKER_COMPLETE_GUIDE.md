# ShopSphere Docker Complete Guide

## Table of Contents
1. [Quick Start](#quick-start)
2. [Architecture](#architecture)
3. [Deployment Strategies](#deployment-strategies)
4. [Service Configuration](#service-configuration)
5. [MACHINE_IP Configuration](#machine-ip-configuration)
6. [Essential Commands](#essential-commands)
7. [Troubleshooting](#troubleshooting)
8. [Advanced Topics](#advanced-topics)

---

## Quick Start

### One-Command Deployment

```bash
# Build and run everything
mvn clean package -DskipTests && docker-compose up -d
```

### Access Points

| Service | URL | Port |
|---------|-----|------|
| API Gateway | http://localhost:8080 | 8080 |
| Eureka Dashboard | http://localhost:8761 | 8761 |
| Config Server | http://localhost:8888 | 8888 |

### Verification Steps

```bash
# Check all services
docker-compose ps

# View Eureka
curl http://localhost:8761

# Test API Gateway
curl http://localhost:8080/actuator/health
```

---

## Architecture

### Layered Architecture

```
┌─────────────────────────────────────────┐
│         API Gateway (8080)              │
│      Request Entry Point                │
└─────────────────────────────────────────┘
                   ↓
┌─────────────────────────────────────────┐
│    Discovery Server (8761) - Eureka     │
│    Config Server (8888)                 │
│    Service Registry & Configuration     │
└─────────────────────────────────────────┘
                   ↓
┌─────────────────────────────────────────┐
│  25 Business Microservices              │
│  Auth, Catalog, Orders, Payments, etc.  │
└─────────────────────────────────────────┘
                   ↓
┌─────────────────────────────────────────┐
│  Infrastructure Services                │
│  Postgres, MongoDB, Redis, Kafka, ES    │
└─────────────────────────────────────────┘
```

### Service Categories

**Core Services (3)**
- discovery-server (8761) - Eureka registry
- config-server (8888) - Configuration management
- api-gateway (8080) - Main entry point

**User & Account Services (3)**
- auth-service (8081)
- user-service (8082)
- admin-service (8089)

**Catalog & Inventory (5)**
- catalog-service (8083)
- inventory-service (8084)
- search-service (8013)
- review-service (8012)
- recommendation-service (8011)

**Shopping & Cart (4)**
- cart-service (8085)
- checkout-service (8086)
- coupon-service (8088)
- pricing-service (8093)

**Order & Payment (4)**
- order-service (8002)
- payment-service (8087)
- fraud-service (8010)
- returns-service (8009)

**Logistics & Notifications (3)**
- shipping-service (8003)
- notification-service (8094)
- media-service (8095)

**Analytics & Real-Time (3)**
- batch-service (8090)
- analytics-service (8091)
- websocket-chat (8092)

---

## Deployment Strategies

### Strategy 1: Full Stack (Testing/Demo)

Best for: Complete system testing, demos, CI/CD pipelines

```bash
# Build the project
mvn clean package -DskipTests

# Start everything
docker-compose up -d

# Wait 2-3 minutes for all services to become healthy
docker-compose ps

# Verify health
curl http://localhost:8080/actuator/health
```

**Resources Required**: 16GB RAM, 8+ CPU cores
**Startup Time**: 2-3 minutes

### Strategy 2: Layered Development (Recommended)

Best for: Active development, faster iteration

```bash
# Layer 1: Start infrastructure (30-40s)
docker-compose -f docker-compose.infrastructure.yml up -d

# Layer 2: Start core services (40-50s)
docker-compose -f docker-compose.core.yml up -d

# Layer 3: Start specific services as needed
docker-compose up -d cart-service catalog-service inventory-service
```

**Benefits**:
- Faster startup
- Lower memory usage
- Only run services you need
- Easier debugging

### Strategy 3: Individual Service

Best for: Testing specific services

```bash
# Start infrastructure dependencies
docker-compose -f docker-compose.infrastructure.yml up -d postgres redis

# Start core services
docker-compose -f docker-compose.core.yml up -d

# Start dependent services
docker-compose up -d catalog-service inventory-service

# Start your service
docker-compose up -d cart-service
```

---

## Service Configuration

### Service Dependency Map

| Service | Database | Cache | Message Broker | Depends On |
|---------|----------|-------|-----------------|-----------|
| discovery-server | — | — | — | None |
| config-server | — | — | — | discovery-server |
| api-gateway | — | — | — | discovery-server, config-server |
| auth-service | PostgreSQL | — | — | postgres, discovery-server |
| user-service | PostgreSQL | — | — | postgres, discovery-server |
| catalog-service | MongoDB | — | Kafka | kafka, discovery-server |
| inventory-service | PostgreSQL | — | Kafka | postgres, kafka, discovery-server |
| cart-service | — | Redis | — | redis, discovery-server, catalog-service, inventory-service |
| pricing-service | PostgreSQL | — | — | postgres, discovery-server |
| coupon-service | PostgreSQL | — | — | postgres, discovery-server |
| checkout-service | — | — | — | cart, inventory, pricing, coupon services |
| order-service | PostgreSQL | — | Kafka | postgres, kafka, discovery-server |
| payment-service | PostgreSQL | — | Kafka | postgres, kafka, discovery-server |
| fraud-service | PostgreSQL | — | Kafka | postgres, kafka, discovery-server |
| shipping-service | PostgreSQL | — | Kafka | postgres, kafka, discovery-server, order-service |
| returns-service | PostgreSQL | — | Kafka | postgres, kafka, discovery-server, order-service |
| notification-service | — | — | Kafka | kafka, discovery-server |
| review-service | PostgreSQL | — | Kafka | postgres, kafka, discovery-server |
| recommendation-service | MongoDB | — | Kafka | mongodb, kafka, discovery-server |
| search-service | — | — | Kafka, Elasticsearch | elasticsearch, kafka, discovery-server |
| media-service | PostgreSQL | — | — | postgres, discovery-server |
| websocket-chat | PostgreSQL | Redis | — | postgres, redis, discovery-server |
| admin-service | PostgreSQL | — | — | postgres, discovery-server |
| batch-service | PostgreSQL | — | — | postgres, discovery-server |
| analytics-service | MongoDB | — | Kafka | mongodb, kafka, discovery-server |

### Container Ports Reference

**Core Services**
```
8080  - API Gateway
8761  - Discovery Server
8888  - Config Server
```

**Core Business Services**
```
8081  - Auth Service
8082  - User Service
8083  - Catalog Service
8084  - Inventory Service
8085  - Cart Service
8086  - Checkout Service
8087  - Payment Service
8088  - Coupon Service
8089  - Admin Service
```

**Advanced Business Services**
```
8090  - Batch Service
8091  - Analytics Service
8092  - WebSocket Chat
8093  - Pricing Service
8094  - Notification Service
8095  - Media Service
```

**Order Management Services**
```
8002  - Order Service
8003  - Shipping Service
8009  - Returns Service
8010  - Fraud Service
8011  - Recommendation Service
8012  - Review Service
8013  - Search Service
```

**Infrastructure Services**
```
5432  - PostgreSQL
27017 - MongoDB
6379  - Redis
9092  - Kafka
9200  - Elasticsearch
```

---

## MACHINE_IP Configuration

### Overview

All 25 services are configured with dynamic MACHINE_IP support via Docker build arguments for flexible networking.

### IPs Available

| Type | IP | Usage |
|------|---|-------|
| Public IP | `157.38.3.74` | External/internet access |
| Local IP | `10.198.135.96` | LAN/internal access |
| Localhost | `127.0.0.1` | Single machine (default) |

### Set Environment Variable

```cmd
setx MACHINE_IP "157.38.3.74"
```

Or use current local IP:
```cmd
setx MACHINE_IP "10.198.135.96"
```

### Build with Different IPs

**Using Public IP (External Access)**
```bash
docker build --build-arg MACHINE_IP=157.38.3.74 \
  -t api-gateway:latest services/api-gateway/
```

**Using Local IP (Network Access)**
```bash
docker build --build-arg MACHINE_IP=10.198.135.96 \
  -t api-gateway:latest services/api-gateway/
```

**Using Localhost (Default)**
```bash
docker build -t api-gateway:latest services/api-gateway/
```

### Docker Compose with MACHINE_IP

```yaml
version: '3.8'

services:
  api-gateway:
    build:
      context: ./services/api-gateway
      args:
        MACHINE_IP: 157.38.3.74
    ports:
      - "8080:8080"

  discovery:
    build:
      context: ./services/discovery
      args:
        MACHINE_IP: 157.38.3.74
    ports:
      - "8761:8761"

  auth-service:
    build:
      context: ./services/auth-service
      args:
        MACHINE_IP: 157.38.3.74
    ports:
      - "8081:8081"
    depends_on:
      - discovery
```

### Health Check Verification

```bash
# Test with public IP
curl http://157.38.3.74:8080/actuator/health

# Test with local IP
curl http://10.198.135.96:8080/actuator/health

# Test with localhost
curl http://localhost:8080/actuator/health
```

### Dockerfile Configuration

Every service includes:

```dockerfile
ARG MACHINE_IP=127.0.0.1

HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
    CMD curl -f http://${MACHINE_IP}:PORT/actuator/health || exit 1
```

---

## Essential Commands

### Build & Start

```bash
# Build project
mvn clean package -DskipTests

# Build with parallel execution (faster)
mvn clean package -DskipTests -T 4

# Start all services
docker-compose up -d

# Start specific service
docker-compose up -d cart-service

# Rebuild and start
docker-compose up -d --build cart-service

# Start with custom compose file
docker-compose -f docker-compose.infrastructure.yml up -d
```

### Monitor & Debug

```bash
# View all running containers
docker-compose ps

# View logs (all services)
docker-compose logs -f

# View logs (specific service)
docker-compose logs -f cart-service

# View last 100 lines
docker-compose logs --tail=100 cart-service

# Check service health
docker inspect shopsphere-cart | grep -A 10 Health

# View resource usage
docker stats

# Check specific container status
docker inspect cart-service --format='{{.State.Health.Status}}'
```

### Stop & Clean

```bash
# Stop all services
docker-compose down

# Stop and remove volumes (clean slate)
docker-compose down -v

# Stop specific service
docker-compose stop cart-service

# Remove specific service
docker-compose rm -f cart-service

# Prune unused resources
docker system prune -a

# Remove all stopped containers
docker container prune
```

### Restart Services

```bash
# Restart all
docker-compose restart

# Restart specific service
docker-compose restart cart-service

# Restart after code change
mvn clean package -DskipTests -pl services/cart-service -am
docker-compose up -d --build cart-service
```

### Database Commands

```bash
# Check PostgreSQL
docker exec shopsphere-postgres pg_isready -U postgres

# Check MongoDB
docker exec shopsphere-mongodb mongosh --eval "db.adminCommand('ping')"

# Check Redis
docker exec shopsphere-redis redis-cli ping

# Check Kafka
docker exec shopsphere-kafka kafka-broker-api-versions.sh --bootstrap-server localhost:9092
```

---

## Troubleshooting

### Service Won't Start

1. **Check logs**:
   ```bash
   docker-compose logs cart-service
   ```

2. **Check dependencies are healthy**:
   ```bash
   docker-compose ps
   ```

3. **Verify JAR was built**:
   ```bash
   dir services\cart-service\target\*.jar
   ```

4. **Restart service**:
   ```bash
   docker-compose restart cart-service
   ```

### Port Already in Use

```bash
# Find process using port
netstat -ano | findstr :8085

# Kill process (admin only)
taskkill /PID <PID> /F

# Or change port in docker-compose.yml
```

### Out of Memory

Increase Docker Desktop memory:
1. Settings → Resources → Memory → 8GB+

Or run fewer services:
```bash
# Minimal setup
docker-compose up -d postgres redis discovery-server api-gateway auth-service
```

### Database Connection Failed

Wait for health checks:
```bash
# Check postgres
docker exec shopsphere-postgres pg_isready -U postgres

# Check mongodb
docker exec shopsphere-mongodb mongosh --eval "db.adminCommand('ping')"

# Check redis
docker exec shopsphere-redis redis-cli ping
```

### Service Not Registering with Eureka

1. **Check Discovery Server is running**:
   ```bash
   curl http://localhost:8761/actuator/health
   ```

2. **Check service logs for connection errors**:
   ```bash
   docker-compose logs cart-service | findstr -i eureka
   ```

3. **Restart the service**:
   ```bash
   docker-compose restart cart-service
   ```

### HEALTHCHECK Failures

```bash
# Check health status
docker-compose ps

# View detailed health info
docker inspect cart-service --format='{{json .State.Health}}'

# Check with specific IP
curl -v http://MACHINE_IP:8085/actuator/health
```

### Network Issues

```bash
# Inspect network
docker network ls
docker network inspect shopsphere_default

# Test connectivity between containers
docker exec cart-service curl http://discovery:8761
```

---

## Advanced Topics

### Performance Optimization

**Maven Parallel Build**
```bash
mvn clean package -DskipTests -T 4
```

**Docker Resource Limits**
```yaml
services:
  cart-service:
    deploy:
      resources:
        limits:
          cpus: '0.5'
          memory: 512M
        reservations:
          cpus: '0.25'
          memory: 256M
```

**View Resource Usage**
```bash
docker stats --no-stream
```

### Custom Environment Variables

In `docker-compose.yml`:
```yaml
services:
  cart-service:
    environment:
      SPRING_JPA_HIBERNATE_DDL_AUTO: update
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/shopsphere
      EUREKA_CLIENT_SERVICE_URL_DEFAULT_ZONE: http://discovery:8761/eureka/
```

### Volume Mounting for Development

```yaml
services:
  cart-service:
    volumes:
      - ./services/cart-service/target:/app/lib
```

### Service Scaling

```bash
# Scale specific service
docker-compose up -d --scale cart-service=3

# Scale with resource limits
docker-compose up -d --scale payment-service=2
```

### Production Considerations

1. **Use Docker Secrets** for sensitive data
2. **Enable TLS** for all external connections
3. **Set resource limits** per service
4. **Use external databases** (RDS, DocumentDB)
5. **Implement service mesh** (Istio/Linkerd)
6. **Add monitoring** (Prometheus + Grafana)
7. **Set up logging** (ELK Stack)
8. **Use Kubernetes** for orchestration

### Development Workflow

```bash
# 1. Make code changes
# 2. Rebuild the service
mvn clean package -DskipTests -pl services/cart-service -am

# 3. Restart container
docker-compose up -d --build cart-service

# 4. View logs
docker-compose logs -f cart-service

# 5. Test API
curl http://localhost:8085/api/cart/items
```

### Testing with Docker

```bash
# Run unit tests
mvn test -pl services/cart-service

# Run integration tests with Docker
docker-compose -f docker-compose.infrastructure.yml up -d
mvn verify -pl services/cart-service

# Cleanup
docker-compose -f docker-compose.infrastructure.yml down -v
```

---

## Common Workflows

### Full Stack Testing

```bash
mvn clean package -DskipTests
docker-compose up -d
# Wait 2-3 minutes
curl http://localhost:8080/api/catalog/products
```

### Develop Single Service

```bash
# Start infrastructure
docker-compose -f docker-compose.infrastructure.yml up -d

# Start core services
docker-compose -f docker-compose.core.yml up -d

# Run your service in IDE (it will connect to Docker infrastructure)
# OR start in Docker:
docker-compose up -d cart-service
```

### Test Service Integration

```bash
# Start service + dependencies
docker-compose up -d postgres redis discovery-server catalog-service inventory-service cart-service

# Test
curl http://localhost:8085/api/cart/items
```

### Clean Restart

```bash
docker-compose down -v
mvn clean package -DskipTests
docker-compose up -d
```

---

## File Reference

| File | Purpose |
|------|---------|
| `docker-compose.yml` | All services (main file) |
| `docker-compose.infrastructure.yml` | Databases & message brokers only |
| `docker-compose.core.yml` | Discovery, Config, Gateway only |
| `services/*/Dockerfile` | Individual service Docker images |

---

## Default Credentials

```
PostgreSQL:
  User: postgres
  Password: shopsphere_password
  Database: shopsphere

MongoDB:
  User: shopsphere
  Password: shopsphere_password
  Database: shopsphere

Redis:
  Password: (none by default)

Kafka:
  Port: 9092
```

**WARNING**: Change these in production!

---

## Resource Requirements

### Minimum (Development)
- **RAM**: 8GB
- **CPU**: 4 cores
- **Disk**: 10GB free

### Recommended (Full Stack)
- **RAM**: 16GB
- **CPU**: 8 cores
- **Disk**: 20GB free

### Per Service Average
- **RAM**: 256-512MB
- **CPU**: 0.5 core
- **Startup**: 30-60 seconds

---

## Git Configuration

All Dockerfiles have been updated with MACHINE_IP support:

```bash
git log --oneline | grep -i "machine_ip\|dockerfile"
```

Recent commits:
- `249b5d7` - Add quick reference guide
- `810bbdf` - Add final verification report
- `fdb6725` - Add setup completion summary
- `3fe47c6` - Add validation report
- `24295f8` - Add configurable MACHINE_IP to all Dockerfiles

---

## Support & Documentation

- [Docker Compose Documentation](https://docs.docker.com/compose/)
- [Spring Cloud Netflix](https://spring.io/projects/spring-cloud-netflix)
- [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)

For issues:
1. Check logs: `docker-compose logs -f`
2. Verify health: `docker-compose ps`
3. Review this guide for detailed troubleshooting

---

**Last Updated**: December 8, 2025  
**Status**: Production Ready ✅  
**Version**: 1.0
