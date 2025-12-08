# MACHINE_IP Configuration - Quick Reference

## Current Status
✅ **All 25 services configured with MACHINE_IP support**

## Quick Access IPs

| Type | IP | Usage |
|------|---|-------|
| Public | `157.38.3.74` | External/internet access |
| Local Network | `10.198.135.96` | LAN/internal access |
| Localhost | `127.0.0.1` | Default/single machine |

## Set Environment Variable

```cmd
setx MACHINE_IP "157.38.3.74"
```

## Build Commands

### All services with public IP
```bash
for service in admin analytics api-gateway auth batch cart catalog checkout config coupon discovery fraud inventory media notification order payment pricing recommendation returns review search shipping user websocket-chat
do
  docker build --build-arg MACHINE_IP=157.38.3.74 \
    -t $service:latest services/$service-service/
done
```

### Single service
```bash
docker build --build-arg MACHINE_IP=157.38.3.74 \
  -t api-gateway:latest services/api-gateway/
```

## Docker Compose

```yaml
services:
  api-gateway:
    build:
      context: ./services/api-gateway
      args:
        MACHINE_IP: 157.38.3.74
    ports:
      - "8080:8080"
```

## Test Health Checks

```bash
# Public IP
curl http://157.38.3.74:8080/actuator/health

# Local IP
curl http://10.198.135.96:8080/actuator/health

# Localhost
curl http://localhost:8080/actuator/health
```

## Service Ports

| Service | Port |
|---------|------|
| api-gateway | 8080 |
| auth-service | 8081 |
| user-service | 8082 |
| catalog-service | 8083 |
| inventory-service | 8084 |
| cart-service | 8085 |
| checkout-service | 8086 |
| notification-service | 8087 |
| payment-service | 8087 |
| coupon-service | 8088 |
| admin-service | 8089 |
| batch-service | 8090 |
| analytics-service | 8091 |
| websocket-chat | 8092 |
| returns-service | 8009 |
| fraud-service | 8010 |
| recommendation-service | 8011 |
| review-service | 8012 |
| search-service | 8013 |
| order-service | 8002 |
| shipping-service | 8003 |
| pricing-service | 8085 |
| discovery | 8761 |
| config-server | 8888 |
| media-service | 8086 |

## Git Commits

```
810bbdf - docs: add final verification report
fdb6725 - docs: add MACHINE_IP setup completion summary
3fe47c6 - docs: add comprehensive Dockerfile validation report
24295f8 - chore: add configurable MACHINE_IP to all service Dockerfiles
```

## Configuration Files

- `MACHINE_IP_CONFIG.md` - Setup guide
- `IP_CONFIGURATION_SUMMARY.md` - Detailed instructions
- `DOCKERFILE_VALIDATION_REPORT.md` - Validation details
- `MACHINE_IP_SETUP_COMPLETE.md` - Completion summary
- `VERIFICATION_COMPLETE.md` - Final verification
- `QUICK_REFERENCE.md` - This file

---

**All 25 services ready with dynamic IP configuration ✅**
