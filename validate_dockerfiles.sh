#!/bin/bash

echo "======================================="
echo "Dockerfile Configuration Validation"
echo "======================================="
echo ""

MISSING_COUNT=0
CONFIGURED_COUNT=0

for dockerfile in f:/AppStorage/shopsphere/services/*/Dockerfile; do
    service_name=$(basename $(dirname "$dockerfile"))
    
    if grep -q "ARG MACHINE_IP" "$dockerfile" 2>/dev/null; then
        if grep -q "\${MACHINE_IP}" "$dockerfile" 2>/dev/null; then
            echo "✅ $service_name - CONFIGURED"
            ((CONFIGURED_COUNT++))
        else
            echo "⚠️  $service_name - ARG exists but not used in HEALTHCHECK"
            ((MISSING_COUNT++))
        fi
    else
        echo "❌ $service_name - MISSING MACHINE_IP configuration"
        ((MISSING_COUNT++))
    fi
done

echo ""
echo "======================================="
echo "Summary"
echo "======================================="
echo "Configured: $CONFIGURED_COUNT services"
echo "Missing/Incomplete: $MISSING_COUNT services"
echo ""

if [ $MISSING_COUNT -gt 0 ]; then
    echo "⚠️  ACTION REQUIRED: Some Dockerfiles need configuration"
    exit 1
else
    echo "✅ All Dockerfiles are properly configured"
    exit 0
fi
