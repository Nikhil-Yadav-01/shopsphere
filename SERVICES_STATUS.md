# ShopSphere Services Status

## ✅ Running Services

### 1. Auth Service (Port 8081)

**Status:** ✅ Running
**Java Version:** OpenJDK 21.0.9
**Database:** PostgreSQL (shopsphere_auth)
**Dependencies:** Eureka, JWT Security

**Endpoints:**
```
POST   /auth/register      - User registration
POST   /auth/login         - User authentication  
POST   /auth/refresh       - Token refresh
POST   /auth/logout        - User logout
```

**Test Command:**
```bash
./tests/integration/test-auth-service.sh
```

**Start Command:**
```bash
cd services/auth-service
DB_PASSWORD=shopsphere_password DB_NAME=shopsphere_auth mvn -DskipTests spring-boot:run
```

---

### 2. API Gateway (Port 8080)

**Status:** ✅ Running
**Java Version:** OpenJDK 21.0.9
**Type:** Spring Cloud Gateway (Reactive)
**Dependencies:** Eureka, Service Discovery, JWT Security

**Routes:**
```
/api/auth/**           → auth-service:8081
/api/users/**          → user-service:8082
/api/products/**       → catalog-service:8083
/api/categories/**     → catalog-service:8083
/api/orders/**         → order-service (TBD)
/api/cart/**           → order-service (TBD)
```

**Features:**
- Service discovery via Eureka
- Load balancing
- CSRF protection
- JWT authentication

**Test Command:**
```bash
./tests/integration/test-api-gateway.sh
```

**Start Command:**
```bash
cd services/api-gateway
mvn -DskipTests spring-boot:run
```

---

### 3. Eureka Discovery Server (Port 8761)

**Status:** ✅ Running
**Role:** Service Registry
**Services Registered:** 2 (auth-service, api-gateway)

---

### 4. PostgreSQL Database (Port 5432)

**Status:** ✅ Running (Docker)
**Container:** shopsphere-postgres
**Databases:**
- `shopsphere_auth` - Auth service data
- `shopsphere_users` - User service data (ready)
- `shopsphere_inventory` - Inventory service data (ready)

---

## 📋 Test Suite

### Run All Tests
```bash
./tests/run-all-tests.sh
```

### Run Individual Tests
```bash
# Auth Service Tests
./tests/integration/test-auth-service.sh

# API Gateway Tests
./tests/integration/test-api-gateway.sh
```

### Test Results (Latest Run)

| Test | Result | Details |
|------|--------|---------|
| Auth Register | ✅ Pass | 201 Created |
| Auth Login | ✅ Pass | 200 OK |
| Auth Refresh | ✅ Pass | 200 OK |
| Gateway Health | ✅ Pass | 200 OK |
| Gateway Routes | ✅ Pass | 5 routes active |
| Service Discovery | ✅ Pass | Eureka integrated |

---

## 🔧 Infrastructure Status

### Docker Containers
```bash
# View running containers
docker-compose ps

# Containers:
✅ shopsphere-postgres     - PostgreSQL 16
✅ shopsphere-discovery    - Eureka Server
⏹️ Other services - Ready to start
```

### Ports Summary
| Service | Port | Status |
|---------|------|--------|
| API Gateway | 8080 | ✅ Active |
| Auth Service | 8081 | ✅ Active |
| Eureka Server | 8761 | ✅ Active |
| PostgreSQL | 5432 | ✅ Active |

---

## 📝 Quick Reference

### Start Infrastructure
```bash
cd /home/ubuntu/shopsphere

# Start databases and discovery
docker-compose up -d postgres discovery-server

# Wait 30 seconds for startup
sleep 30
```

### Start Services (in separate terminals)
```bash
# Terminal 1: Auth Service
cd services/auth-service
DB_PASSWORD=shopsphere_password DB_NAME=shopsphere_auth mvn -DskipTests spring-boot:run

# Terminal 2: API Gateway
cd services/api-gateway
mvn -DskipTests spring-boot:run
```

### Run Tests
```bash
./tests/run-all-tests.sh
```

---

## 📊 Performance Metrics

**Auth Service Startup Time:** ~5 seconds
**API Gateway Startup Time:** ~4 seconds
**JWT Token Generation:** < 100ms
**Route Discovery:** Immediate (Eureka)

---

## ⚠️ Known Issues & Notes

1. **Gateway CSRF Protection:** POST requests through gateway require CSRF tokens (expected behavior)
2. **Logout Endpoint:** Returns 403 without JWT filter validation (under investigation)
3. **Actuator Endpoints:** Gateway actuator requires authentication
4. **Java 21 Requirement:** All services require OpenJDK 21.0.9+

---

## 🔐 Security Features

- ✅ JWT Token Authentication (3600s expiration)
- ✅ CSRF Protection on Gateway
- ✅ Password Encryption (BCrypt)
- ✅ Role-Based Access Control (RBAC)
- ✅ Secure Token Refresh
- ✅ Service-to-Service Authentication

---

## 📦 Next Steps

1. **User Service** (Port 8082) - Ready to start
2. **Catalog Service** (Port 8083) - Ready to start
3. **Order Service** (Port 8002/8086) - Ready to start
4. **Additional Microservices** - Available in services/ folder

---

## 🆘 Troubleshooting

### Service Won't Start
```bash
# Check Java version
java -version

# Should output: OpenJDK 21.0.9
```

### Database Connection Error
```bash
# Verify PostgreSQL is running
docker-compose ps

# Check database creation
docker exec shopsphere-postgres psql -U postgres -l
```

### Port Already in Use
```bash
# Kill process using port (example: 8081)
lsof -ti:8081 | xargs kill -9
```

---

**Last Updated:** 2025-12-08
**Environment:** AWS EC2 (Ubuntu 24.04)
**IP Address:** 172.31.47.17
