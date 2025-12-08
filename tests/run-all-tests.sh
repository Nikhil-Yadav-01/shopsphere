#!/bin/bash

set -e

echo "╔════════════════════════════════════════════════════════╗"
echo "║         ShopSphere Integration Tests Runner            ║"
echo "╚════════════════════════════════════════════════════════╝"
echo ""

# Check if services are running
echo "🔍 Checking if services are running..."
echo ""

# Check Auth Service
if curl -s http://localhost:8081/actuator/health > /dev/null 2>&1; then
  echo "✅ Auth Service is running on http://localhost:8081"
else
  echo "❌ Auth Service is NOT running on http://localhost:8081"
  echo "   Start it with: cd services/auth-service && mvn -DskipTests spring-boot:run"
  exit 1
fi

# Check API Gateway
if curl -s http://localhost:8080/actuator/health > /dev/null 2>&1; then
  echo "✅ API Gateway is running on http://localhost:8080"
else
  echo "❌ API Gateway is NOT running on http://localhost:8080"
  echo "   Start it with: cd services/api-gateway && mvn -DskipTests spring-boot:run"
  exit 1
fi

echo ""
echo "════════════════════════════════════════════════════════"
echo ""

# Run Auth Service tests
echo "📋 Running Auth Service Tests..."
echo ""
./tests/integration/test-auth-service.sh
TEST_1_RESULT=$?
echo ""

# Run API Gateway tests
echo "📋 Running API Gateway Tests..."
echo ""
./tests/integration/test-api-gateway.sh
TEST_2_RESULT=$?
echo ""

# Summary
echo "════════════════════════════════════════════════════════"
echo ""
echo "📊 Test Summary:"
echo ""
if [ $TEST_1_RESULT -eq 0 ]; then
  echo "  ✅ Auth Service Tests - PASSED"
else
  echo "  ❌ Auth Service Tests - FAILED"
fi

if [ $TEST_2_RESULT -eq 0 ]; then
  echo "  ✅ API Gateway Tests - PASSED"
else
  echo "  ❌ API Gateway Tests - FAILED"
fi

echo ""
echo "════════════════════════════════════════════════════════"
echo ""

if [ $TEST_1_RESULT -eq 0 ] && [ $TEST_2_RESULT -eq 0 ]; then
  echo "🎉 All integration tests passed!"
  exit 0
else
  echo "⚠️  Some tests failed. Check output above."
  exit 1
fi
