#!/bin/bash

set -e

echo "╔════════════════════════════════════════════════════════╗"
echo "║      ShopSphere Services Startup Script               ║"
echo "╚════════════════════════════════════════════════════════╝"
echo ""

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "$SCRIPT_DIR"

# Step 1: Start Infrastructure
echo "📦 Step 1: Starting Infrastructure (PostgreSQL, Eureka)..."
docker-compose up -d postgres discovery-server
echo "✅ Infrastructure started"
echo ""

# Wait for services to be ready
echo "⏳ Waiting 30 seconds for services to initialize..."
sleep 30
echo "✅ Ready"
echo ""

# Step 2: Create databases if needed
echo "🗄️  Step 2: Ensuring databases exist..."
docker exec shopsphere-postgres psql -U postgres -c "CREATE DATABASE shopsphere_auth;" 2>/dev/null || true
docker exec shopsphere-postgres psql -U postgres -c "CREATE DATABASE shopsphere_users;" 2>/dev/null || true
echo "✅ Databases ready"
echo ""

# Step 3: Display startup instructions
echo "╔════════════════════════════════════════════════════════╗"
echo "║           Start Services in New Terminals              ║"
echo "╚════════════════════════════════════════════════════════╝"
echo ""
echo "📋 Terminal 1 - Auth Service (Port 8081):"
echo "   cd $SCRIPT_DIR/services/auth-service"
echo "   DB_PASSWORD=shopsphere_password DB_NAME=shopsphere_auth \\"
echo "   mvn -DskipTests spring-boot:run"
echo ""
echo "📋 Terminal 2 - API Gateway (Port 8080):"
echo "   cd $SCRIPT_DIR/services/api-gateway"
echo "   mvn -DskipTests spring-boot:run"
echo ""
echo "╔════════════════════════════════════════════════════════╗"
echo "║              Monitor Services Startup                  ║"
echo "╚════════════════════════════════════════════════════════╝"
echo ""
echo "Check service health with:"
echo "  curl -s http://localhost:8081/actuator/health"
echo "  curl -s http://localhost:8080/actuator/health"
echo ""
echo "Once services are running, run tests:"
echo "  ./tests/run-all-tests.sh"
echo ""
echo "View infrastructure status:"
echo "  docker-compose ps"
echo ""
