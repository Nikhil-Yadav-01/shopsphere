# ShopSphere Docker Quick Reference

## 🚀 One-Command Deployment

```bash
# Build and run everything
mvn clean package -DskipTests && docker-compose up -d
```

## 📋 Three Deployment Strategies

### Strategy 1: Full Stack (Recommended for Testing)
```bash
docker-compose up -d
```
- Starts all 25 services + infrastructure
- Takes 2-3 minutes for full startup
- Requires 8GB+ RAM

### Strategy 2: Layered (Recommended for Development)
```bash
# Layer 1: Infrastructure (30s)
docker-compose -f docker-compose.infrastructure.yml up -d

# Layer 2: Core Services (40s)
docker-compose -f docker-compose.core.yml up -d

# Layer 3: Business Services (as needed)
docker-compose up -d cart-service checkout-service order-service
```

### Strategy 3: Individual Service
```bash
# Example: Run cart-service with dependencies
docker-compose up -d postgres redis discovery-server catalog-service inventory-service cart-service
```

## 🔗 Service Ports Quick Reference

```
8080  - API Gateway (Main Entry)
8761  - Eureka Dashboard
8888  - Config Server

8081  - Auth Service
8082  - User Service
8083  - Catalog Service
8084  - Inventory Service
8085  - Cart Service
8086  - Checkout Service
8087  - Payment Service
8088  - Coupon Service
8089  - Admin Service
8090  - Batch Service
8091  - Analytics Service
8092  - WebSocket Chat
8093  - Pricing Service
8094  - Notification Service
8095  - Media Service

8002  - Order Service
8003  - Shipping Service
8009  - Returns Service
8010  - Fraud Service
8011  - Recommendation Service
8012  - Review Service
8013  - Search Service
```

## 🗄️ Infrastructure Ports

```
5432  - PostgreSQL
27017 - MongoDB
6379  - Redis
9092  - Kafka
9200  - Elasticsearch
```

## 🛠️ Essential Commands

### Start/Stop
```bash
docker-compose up -d                    # Start all
docker-compose up -d cart-service       # Start specific service
docker-compose stop                     # Stop all
docker-compose down                     # Stop and remove
docker-compose down -v                  # Stop, remove, and delete data
```

### Monitor
```bash
docker-compose ps                       # List all services
docker-compose logs -f                  # Follow all logs
docker-compose logs -f cart-service     # Follow specific service
docker-compose logs --tail=50 cart-service  # Last 50 lines
```

### Rebuild
```bash
# Rebuild specific service after code change
mvn clean package -DskipTests -pl services/cart-service -am
docker-compose up -d --build cart-service
```

### Health Check
```bash
docker-compose ps                       # See health status
curl http://localhost:8761              # Eureka dashboard
curl http://localhost:8080/actuator/health  # Gateway health
```

## 🐛 Quick Troubleshooting

### Service won't start
```bash
docker-compose logs cart-service        # Check logs
docker-compose restart cart-service     # Restart
```

### Port conflict
```bash
netstat -ano | findstr :8085           # Find process
taskkill /PID <PID> /F                 # Kill process
```

### Database connection failed
```bash
docker exec shopsphere-postgres pg_isready -U postgres
docker exec shopsphere-mongodb mongosh --eval "db.adminCommand('ping')"
docker exec shopsphere-redis redis-cli ping
```

### Service not in Eureka
```bash
# Wait 30-60 seconds after startup
# Check: http://localhost:8761
docker-compose restart cart-service     # Force re-registration
```

### Out of memory
```bash
# Run minimal setup
docker-compose up -d postgres redis discovery-server api-gateway auth-service
```

## 📊 Service Dependencies Cheat Sheet

**No Dependencies:**
- discovery-server

**Postgres Only:**
- auth-service, user-service, inventory-service, pricing-service
- coupon-service, order-service, payment-service, fraud-service
- shipping-service, returns-service, review-service, media-service
- websocket-chat, admin-service, batch-service

**MongoDB Only:**
- catalog-service, recommendation-service, analytics-service

**Redis Only:**
- cart-service, websocket-chat

**Kafka Only:**
- notification-service

**Multiple Dependencies:**
- cart-service: redis + catalog + inventory
- checkout-service: cart + inventory + pricing + coupon
- search-service: elasticsearch + kafka

## 🎯 Common Workflows

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

## 📁 File Reference

| File | Purpose |
|------|---------|
| `docker-compose.yml` | All services (main file) |
| `docker-compose.infrastructure.yml` | Databases & message brokers only |
| `docker-compose.core.yml` | Discovery, Config, Gateway only |
| `DOCKER_README.md` | Complete documentation |
| `DOCKER_GUIDE.md` | Detailed deployment guide |
| `services/*/Dockerfile` | Individual service Docker images |

## ⚡ Performance Tips

```bash
# Parallel Maven build (faster)
mvn clean package -DskipTests -T 4

# Start services in parallel
docker-compose up -d --scale cart-service=1 --scale order-service=1

# View resource usage
docker stats

# Prune unused resources
docker system prune -a
```

## 🔐 Default Credentials

```
PostgreSQL:
  User: postgres
  Password: shopsphere_password

MongoDB:
  User: shopsphere
  Password: shopsphere_password

Redis:
  Password: (none)
```

## 📞 Need Help?

1. Check logs: `docker-compose logs -f service-name`
2. Check health: `docker-compose ps`
3. Review: `DOCKER_README.md` for detailed guide
4. Review: `DOCKER_GUIDE.md` for troubleshooting

## ✅ Verification Steps

After starting services:

1. **Check Eureka**: http://localhost:8761
   - Should show all started services

2. **Check Gateway**: http://localhost:8080/actuator/health
   - Should return `{"status":"UP"}`

3. **Check Services**:
   ```bash
   docker-compose ps
   # All should show "healthy" or "running"
   ```

4. **Test API**:
   ```bash
   curl http://localhost:8080/api/catalog/products
   ```

---

**Quick Start**: `mvn clean package -DskipTests && docker-compose up -d`

**Quick Stop**: `docker-compose down`

**Quick Clean**: `docker-compose down -v && docker system prune -f`
