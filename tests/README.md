# Integration Tests

This directory contains integration tests for ShopSphere services.

## Structure

```
tests/
├── integration/              # Integration test scripts
│   ├── test-auth-service.sh
│   ├── test-api-gateway.sh
│   └── ...
└── README.md
```

## Running Tests

### Auth Service Tests

```bash
./tests/integration/test-auth-service.sh
```

Tests auth service endpoints:
- ✅ POST /auth/register - User registration
- ✅ POST /auth/login - User authentication
- ✅ POST /auth/refresh - Token refresh
- ⚠️ POST /auth/logout - Logout (requires JWT filter)
- ✅ Error handling - Invalid credentials

### API Gateway Tests

```bash
./tests/integration/test-api-gateway.sh
```

Tests API Gateway routing and integration:
- ✅ Health check - Gateway operational status
- ✅ Service discovery - Eureka integration
- ✅ Route: /api/auth/** → auth-service
- ✅ Route: /api/users/** → user-service
- ✅ Route: /api/products/** → catalog-service
- ✅ Route: /api/orders/** → order-service
- ✅ Authentication - Token validation
- ⚠️ CSRF protection - Enabled on gateway

## Prerequisites

**Services must be running:**

```bash
# Terminal 1 - Auth Service
cd services/auth-service
DB_PASSWORD=shopsphere_password DB_NAME=shopsphere_auth \
  mvn -DskipTests spring-boot:run

# Terminal 2 - API Gateway
cd services/api-gateway
mvn -DskipTests spring-boot:run
```

**Dependencies:**
- PostgreSQL (running via Docker)
- Eureka Discovery Server (running)
- curl installed
- jq (optional, for better JSON parsing)

## Test Results Summary

| Service | Endpoint | Status |
|---------|----------|--------|
| Auth | POST /auth/register | ✅ 201 Created |
| Auth | POST /auth/login | ✅ 200 OK |
| Auth | POST /auth/refresh | ✅ 200 OK |
| Gateway | GET /actuator/health | ✅ 200 OK |
| Gateway | Route /api/auth/** | ✅ Proxying |
| Gateway | Service Discovery | ✅ Active |

## Known Issues

- Gateway CSRF protection enforced on POST requests (by design)
- Logout endpoint requires JWT filter validation
- Actuator endpoints require authentication on gateway

## Adding New Tests

1. Create a new script in `tests/integration/`
2. Follow naming convention: `test-{service-name}.sh`
3. Include clear output with emoji indicators
4. Update this README with test details
5. Make script executable: `chmod +x test-{service}.sh`
