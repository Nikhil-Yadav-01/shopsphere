# ShopSphere Docker Compose Guide

This guide explains how to run the complete ShopSphere microservices platform using Docker Compose.

## Prerequisites

- Docker Engine 20.10+
- Docker Compose 2.0+
- Minimum 8GB RAM available for Docker
- 20GB free disk space for containers and volumes

## Quick Start

### 1. Build All Services

```bash
# Build the parent image and all services
docker-compose build

# Build specific service
docker-compose build auth-service
```

### 2. Start All Services

```bash
# Start all services in the background
docker-compose up -d

# Start with logs visible
docker-compose up

# Start specific services
docker-compose up -d postgres mongodb kafka eureka
```

### 3. Verify Services Are Running

```bash
# List running containers
docker-compose ps

# Expected output:
# NAME              COMMAND              SERVICE            STATUS        PORTS
# shopsphere-postgres    postgres         postgres           Up ...        5432/tcp
# shopsphere-mongodb     mongod           mongodb            Up ...        27017/tcp
# shopsphere-kafka       kafka-broker     kafka              Up ...        9092/tcp
# shopsphere-eureka      java -jar        eureka             Up ...        8761/tcp
# shopsphere-gateway     java -jar        api-gateway        Up ...        8080/tcp
# ... (and other services)
```

### 4. Access Services

Once started, access services via:

- **API Gateway**: http://localhost:8080
- **Eureka Dashboard**: http://localhost:8761
- **Auth Service**: http://localhost:8001
- **Catalog Service**: http://localhost:8002
- **Cart Service**: http://localhost:8003
- **Inventory Service**: http://localhost:8004
- **Order Service**: http://localhost:8005
- **Payment Service**: http://localhost:8006
- **Checkout Service**: http://localhost:8007
- **Notification Service**: http://localhost:8008
- **PostgreSQL**: localhost:5432
- **MongoDB**: localhost:27017
- **Kafka**: localhost:9092

## Configuration

### Environment Variables

Create a `.env` file in the project root:

```bash
# Database credentials
DB_PASSWORD=your-secure-postgres-password
MONGO_PASSWORD=your-secure-mongo-password

# JWT Secret
JWT_SECRET=your-super-secret-jwt-key-change-in-production

# Stripe API Keys (get from https://stripe.com)
STRIPE_API_KEY=sk_test_YOUR_STRIPE_API_KEY
STRIPE_WEBHOOK_SECRET=whsec_YOUR_WEBHOOK_SECRET

# Email configuration
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USERNAME=your-email@gmail.com
SMTP_PASSWORD=your-app-password
```

Or use defaults (NOT recommended for production):

```bash
# Default credentials are set in compose.yaml
# Change them before deploying to production
```

### Custom Configuration

Modify `compose.yaml` to:

1. **Change database credentials**:

```yaml
postgres:
  environment:
    POSTGRES_PASSWORD: ${DB_PASSWORD:-newpassword}
```

2. **Add more service replicas**:

```yaml
services:
  auth-service:
    deploy:
      replicas: 3  # Instead of implicit single instance
```

3. **Mount volumes for code changes** (development):

```yaml
auth-service:
  volumes:
    - ./services/auth-service/target:/app/app.jar:ro
```

## Monitoring and Debugging

### View Logs

```bash
# View logs from all services
docker-compose logs

# View logs from specific service
docker-compose logs auth-service

# Follow logs in real-time (like tail -f)
docker-compose logs -f payment-service

# View last 100 lines
docker-compose logs --tail=100
```

### Execute Commands in Containers

```bash
# Connect to PostgreSQL
docker-compose exec postgres psql -U shopsphere -d shopsphere

# Connect to MongoDB
docker-compose exec mongodb mongosh -u shopsphere -p

# Execute a command in a service
docker-compose exec auth-service curl http://localhost:8001/actuator/health

# Open shell in a container
docker-compose exec api-gateway /bin/sh
```

### Check Service Health

```bash
# Check health of all services
docker-compose exec api-gateway curl http://api-gateway:8080/actuator/health

# Check if PostgreSQL is ready
docker-compose exec postgres pg_isready -U shopsphere

# Check if MongoDB is ready
docker-compose exec mongodb mongosh -u shopsphere -p mongodb --authenticationDatabase admin --eval "db.adminCommand('ping')"
```

## Database Management

### PostgreSQL

```bash
# Access PostgreSQL CLI
docker-compose exec postgres psql -U shopsphere -d shopsphere

# Useful commands in psql:
# \dt                 - List tables
# \d table_name       - Describe table
# SELECT * FROM ...   - Query data
# \q                  - Exit

# Backup database
docker-compose exec postgres pg_dump -U shopsphere shopsphere > backup.sql

# Restore database
docker-compose exec -T postgres psql -U shopsphere shopsphere < backup.sql

# Reset database (drop and recreate)
docker-compose exec postgres psql -U shopsphere -d postgres -c "DROP DATABASE shopsphere;"
docker-compose exec postgres psql -U shopsphere -c "CREATE DATABASE shopsphere;"
```

### MongoDB

```bash
# Access MongoDB CLI
docker-compose exec mongodb mongosh -u shopsphere -p mongodb --authenticationDatabase admin

# Useful commands in mongosh:
# show dbs              - List databases
# use shopsphere        - Switch database
# show collections      - List collections
# db.collection.find()  - Query data

# Export collections
docker-compose exec mongodb mongosh -u shopsphere -p mongodb --authenticationDatabase admin \
  --eval "db.products.find().pretty()" > products.json

# Create backup
docker-compose exec mongodb mongodump -u shopsphere -p mongodb -d shopsphere -o /backup

# List MongoDB users
docker-compose exec mongodb mongosh -u shopsphere -p mongodb --authenticationDatabase admin \
  --eval "db.getUsers()"
```

## Stopping and Cleaning Up

### Stop Services

```bash
# Stop all services (containers remain)
docker-compose stop

# Stop specific service
docker-compose stop auth-service

# Restart services
docker-compose restart

# Stop and remove containers
docker-compose down
```

### Remove Data

```bash
# Remove containers and volumes (WARNING: deletes all data)
docker-compose down -v

# Remove containers, volumes, and images
docker-compose down -v --rmi all

# Remove dangling images
docker image prune -f
```

## Scaling Services

```bash
# Scale auth-service to 3 instances
docker-compose up -d --scale auth-service=3

# Scale multiple services
docker-compose up -d --scale catalog-service=2 --scale payment-service=2

# Note: Services that depend on databases should not be scaled
# (only stateless services can be scaled safely)
```

## Networking

Services communicate via the `shopsphere-network` bridge network:

```bash
# View network details
docker network inspect shopsphere-network

# Services can reach each other by service name:
# Example: auth-service can reach catalog-service at http://catalog-service:8002
```

## Performance Optimization

### Memory Limits

Adjust Docker's allocated memory if services are slow:

```bash
# Docker Desktop (Mac/Windows) - Settings → Resources
# Set Memory: 8GB or more
# Set CPUs: 4 or more
```

### Database Optimization

```bash
# Monitor PostgreSQL performance
docker-compose exec postgres psql -U shopsphere -d shopsphere -c \
  "SELECT * FROM pg_stat_statements ORDER BY total_time DESC LIMIT 10;"

# MongoDB query profiling
docker-compose exec mongodb mongosh -u shopsphere -p mongodb --authenticationDatabase admin \
  --eval "db.setProfilingLevel(1)"
```

### Kafka Optimization

```bash
# Monitor Kafka topics
docker-compose exec kafka kafka-topics.sh \
  --bootstrap-server localhost:9092 \
  --list

# Check consumer lag
docker-compose exec kafka kafka-consumer-groups.sh \
  --bootstrap-server localhost:9092 \
  --group payment-group \
  --describe
```

## Troubleshooting

### Port Already in Use

```bash
# Find what's using the port (e.g., 8080)
netstat -ano | findstr :8080  # Windows
lsof -i :8080                 # Mac/Linux

# Change port in compose.yaml or use a different port:
docker-compose up -d -p 8081:8080
```

### Out of Memory

```bash
# Check resource usage
docker stats

# Increase Docker memory allocation:
# Docker Desktop Settings → Resources → Memory
# Or increase limits in docker-compose.yaml:
services:
  postgres:
    deploy:
      resources:
        limits:
          memory: 1G
```

### Service Connection Issues

```bash
# Test network connectivity
docker-compose exec auth-service ping catalog-service

# Check DNS resolution
docker-compose exec auth-service nslookup eureka

# View service logs
docker-compose logs -f auth-service
```

### Database Not Initializing

```bash
# Check postgres logs
docker-compose logs postgres

# Recreate postgres with fresh volume
docker-compose down -v
docker-compose up -d postgres
docker-compose logs -f postgres
```

## Production Deployment

For production deployment, use Kubernetes instead of Docker Compose. See [k8s/README.md](k8s/README.md).

However, if you must use Docker Compose in production:

1. **Use environment files for secrets**:

```bash
docker-compose --env-file .env.production up -d
```

2. **Enable restart policies**:

```yaml
services:
  auth-service:
    restart: unless-stopped
```

3. **Setup health checks** (already configured):

```yaml
auth-service:
  healthcheck:
    test: ["CMD", "curl", "-f", "http://localhost:8001/actuator/health"]
    interval: 15s
    timeout: 5s
    retries: 5
    start_period: 30s
```

4. **Use external volumes for databases**:

```bash
docker volume create shopsphere-postgres-data
```

5. **Setup logging driver**:

```yaml
services:
  auth-service:
    logging:
      driver: "awslogs"
      options:
        awslogs-group: "shopsphere"
        awslogs-region: "us-east-1"
```

## References

- [Docker Compose Documentation](https://docs.docker.com/compose/)
- [Docker CLI Reference](https://docs.docker.com/engine/reference/commandline/docker/)
- [Spring Boot Docker Guide](https://spring.io/blog/2020/08/14/the-road-to-java-17-and-beyond)
