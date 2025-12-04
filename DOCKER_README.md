# ShopSphere Docker Setup

## 📋 Overview

ShopSphere provides multiple Docker deployment strategies:

1. **Full Stack** - Run everything with one command
2. **Layered** - Start infrastructure, core, then business services
3. **Individual** - Run specific services with their dependencies

## 🚀 Quick Start (Full Stack)

```bash
# 1. Build the project
mvn clean package -DskipTests

# 2. Start everything
docker-compose up -d

# 3. Check status
docker-compose ps

# 4. View logs
docker-compose logs -f api-gateway
```

Access the API Gateway at: http://localhost:8080

## 📦 Available Compose Files

| File | Purpose | Services |
|------|---------|----------|
| `docker-compose.yml` | Full stack deployment | All 25 services + infrastructure |
| `docker-compose.infrastructure.yml` | Infrastructure only | Postgres, MongoDB, Redis, Kafka, Elasticsearch |
| `docker-compose.core.yml` | Core services | Discovery, Config, Gateway |

## 🏗️ Layered Deployment (Recommended for Development)

### Step 1: Start Infrastructure

```bash
docker-compose -f docker-compose.infrastructure.yml up -d
```

Wait 30-40 seconds for health checks to pass:
```bash
docker-compose -f docker-compose.infrastructure.yml ps
```

### Step 2: Start Core Services

```bash
docker-compose -f docker-compose.core.yml up -d
```

Wait for Discovery Server to be healthy (check at http://localhost:8761)

### Step 3: Start Business Services

```bash
# Start specific services you need
docker-compose up -d auth-service user-service catalog-service cart-service

# Or start all business services
docker-compose up -d
```

## 🎯 Running Individual Services

### Example: Run Cart Service

Cart service depends on:
- Redis (cache)
- Discovery Server (service registry)
- Catalog Service (product info)
- Inventory Service (stock info)

```bash
# 1. Start infrastructure
docker-compose -f docker-compose.infrastructure.yml up -d postgres redis

# 2. Start core
docker-compose -f docker-compose.core.yml up -d discovery-server

# 3. Wait for discovery to be healthy
sleep 40

# 4. Start dependencies
docker-compose up -d catalog-service inventory-service

# 5. Start cart service
docker-compose up -d cart-service
```

### Example: Run Checkout Service

```bash
# Infrastructure
docker-compose -f docker-compose.infrastructure.yml up -d postgres redis kafka

# Core
docker-compose -f docker-compose.core.yml up -d

# Dependencies
docker-compose up -d cart-service inventory-service pricing-service coupon-service catalog-service

# Checkout
docker-compose up -d checkout-service
```

## 🔗 Service Dependency Map

```
Infrastructure Layer:
├── postgres (5432)
├── mongodb (27017)
├── redis (6379)
├── kafka (9092)
└── elasticsearch (9200)

Core Layer:
├── discovery-server (8761) → None
├── config-server (8888) → discovery-server
└── api-gateway (8080) → discovery-server, config-server

Business Layer:
├── auth-service (8081) → postgres, discovery-server
├── user-service (8082) → postgres, discovery-server
├── catalog-service (8083) → mongodb, kafka, discovery-server
├── inventory-service (8084) → postgres, kafka, discovery-server
├── cart-service (8085) → redis, discovery-server, catalog-service, inventory-service
├── pricing-service (8093) → postgres, discovery-server
├── coupon-service (8088) → postgres, discovery-server
├── checkout-service (8086) → discovery-server, cart, inventory, pricing, coupon
├── order-service (8002) → postgres, kafka, discovery-server
├── payment-service (8087) → postgres, kafka, discovery-server
├── fraud-service (8010) → postgres, kafka, discovery-server
├── shipping-service (8003) → postgres, kafka, discovery-server, order-service
├── returns-service (8009) → postgres, kafka, discovery-server, order-service
├── notification-service (8094) → kafka, discovery-server
├── review-service (8012) → postgres, kafka, discovery-server
├── recommendation-service (8011) → mongodb, kafka, discovery-server
├── search-service (8013) → elasticsearch, kafka, discovery-server
├── media-service (8095) → postgres, discovery-server
├── websocket-chat (8092) → postgres, redis, discovery-server
├── admin-service (8089) → postgres, discovery-server
├── batch-service (8090) → postgres, discovery-server
└── analytics-service (8091) → mongodb, kafka, discovery-server
```

## 🛠️ Common Commands

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

## 🔍 Health Checks

All services include health checks. Check status:

```bash
# All services
docker-compose ps

# Specific service
docker inspect shopsphere-cart --format='{{.State.Health.Status}}'

# Eureka Dashboard (see all registered services)
# Open: http://localhost:8761
```

## 🐛 Troubleshooting

### Service Won't Start

1. Check logs:
   ```bash
   docker-compose logs cart-service
   ```

2. Check dependencies are healthy:
   ```bash
   docker-compose ps
   ```

3. Verify JAR was built:
   ```bash
   dir services\cart-service\target\*.jar
   ```

### Port Already in Use

```bash
# Find process using port
netstat -ano | findstr :8085

# Kill process
taskkill /PID <PID> /F
```

### Out of Memory

Increase Docker Desktop memory:
- Settings → Resources → Memory → 8GB+

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

1. Check Discovery Server is running:
   ```bash
   curl http://localhost:8761/actuator/health
   ```

2. Check service logs for connection errors:
   ```bash
   docker-compose logs cart-service | findstr -i eureka
   ```

3. Restart the service:
   ```bash
   docker-compose restart cart-service
   ```

## 📊 Resource Requirements

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

## 🔐 Environment Variables

Default credentials (change in production):

```bash
# PostgreSQL
POSTGRES_USER=postgres
POSTGRES_PASSWORD=shopsphere_password

# MongoDB
MONGO_INITDB_ROOT_USERNAME=shopsphere
MONGO_INITDB_ROOT_PASSWORD=shopsphere_password

# Redis
REDIS_PASSWORD="" (no password by default)
```

Override in docker-compose:
```yaml
services:
  postgres:
    environment:
      POSTGRES_PASSWORD: ${DB_PASSWORD:-shopsphere_password}
```

## 🚀 Production Considerations

1. **Use Docker Secrets** for sensitive data
2. **Enable TLS** for all external connections
3. **Set resource limits** per service
4. **Use external databases** (RDS, DocumentDB)
5. **Implement service mesh** (Istio/Linkerd)
6. **Add monitoring** (Prometheus + Grafana)
7. **Set up logging** (ELK Stack)
8. **Use Kubernetes** for orchestration

## 📝 Development Workflow

### Making Changes to a Service

```bash
# 1. Make code changes
# 2. Rebuild the service
mvn clean package -DskipTests -pl services/cart-service -am

# 3. Restart container
docker-compose up -d --build cart-service

# 4. View logs
docker-compose logs -f cart-service
```

### Testing Changes

```bash
# Run tests
mvn test -pl services/cart-service

# Integration tests with Docker
docker-compose up -d postgres redis discovery-server
mvn verify -pl services/cart-service
```

## 🎓 Learning Resources

- [Docker Compose Documentation](https://docs.docker.com/compose/)
- [Spring Cloud Netflix](https://spring.io/projects/spring-cloud-netflix)
- [Microservices Patterns](https://microservices.io/patterns/)

## 📞 Support

For issues:
1. Check logs: `docker-compose logs -f`
2. Verify health: `docker-compose ps`
3. Review DOCKER_GUIDE.md for detailed troubleshooting
