#!/bin/bash
# Health check script for ShopSphere services
# Checks if the service is responding to health endpoint

SERVICE_PORT=${SERVICE_PORT:-8080}
HEALTH_ENDPOINT=${HEALTH_ENDPOINT:-/actuator/health}
HEALTH_URL="http://localhost:${SERVICE_PORT}${HEALTH_ENDPOINT}"

# Try to connect to health endpoint
http_code=$(curl -s -o /dev/null -w "%{http_code}" "$HEALTH_URL" 2>/dev/null || echo "000")

# Exit with 0 if service is healthy (2xx response)
if [[ "$http_code" =~ ^2[0-9][0-9]$ ]]; then
    echo "Service is healthy (HTTP $http_code)"
    exit 0
else
    echo "Service is unhealthy (HTTP $http_code)"
    exit 1
fi
