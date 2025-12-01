#!/bin/bash
set -e

# Update system packages
yum update -y
yum install -y docker git curl wget

# Install CloudWatch logs agent
wget https://s3.amazonaws.com/amazoncloudwatch-agent/amazon_linux/amd64/latest/amazon-cloudwatch-agent.rpm
rpm -U ./amazon-cloudwatch-agent.rpm

# Start Docker
systemctl start docker
systemctl enable docker
usermod -a -G docker ec2-user

# Install Docker Compose
curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
chmod +x /usr/local/bin/docker-compose

# Create application directory
mkdir -p /app/shopsphere
cd /app/shopsphere

# Create environment file for Docker containers
cat > .env << EOF
# Database Configuration
POSTGRES_HOST=${postgres_endpoint%:*}
POSTGRES_PORT=5432
POSTGRES_USER=${postgres_username}
POSTGRES_PASSWORD=${postgres_password}
POSTGRES_DB=shopsphere

MONGO_HOST=${mongo_endpoint%:*}
MONGO_PORT=27017
MONGO_USER=${mongo_username}
MONGO_PASSWORD=${mongo_password}
MONGO_DB=shopsphere

# Service Configuration
JWT_SECRET=${jwt_secret}
STRIPE_API_KEY=${stripe_api_key}
STRIPE_WEBHOOK_SECRET=${stripe_webhook_secret}

# Logging
AWS_REGION=$(ec2-metadata --availability-zone | cut -d' ' -f2 | sed 's/[a-z]$$//')
CLOUDWATCH_LOG_GROUP=${cloudwatch_log_group}
EOF

chmod 600 .env

# Create CloudWatch configuration
cat > /opt/aws/amazon-cloudwatch-agent/etc/amazon-cloudwatch-agent.json << 'EOF'
{
  "logs": {
    "logs_collected": {
      "files": {
        "collect_list": [
          {
            "file_path": "/var/lib/docker/containers/*/*.log",
            "log_group_name": "${cloudwatch_log_group}",
            "log_stream_name": "{instance_id}"
          }
        ]
      }
    }
  }
}
EOF

# Start CloudWatch agent
/opt/aws/amazon-cloudwatch-agent/bin/amazon-cloudwatch-agent-ctl \
  -a fetch-config \
  -m ec2 \
  -s \
  -c file:/opt/aws/amazon-cloudwatch-agent/etc/amazon-cloudwatch-agent.json

# Create docker-compose.yml for backend services
cat > docker-compose.yml << 'EOF'
version: '3.9'

services:
  eureka:
    image: ${docker_image_uri}/eureka:latest
    ports:
      - "8761:8761"
    environment:
      SPRING_APPLICATION_NAME: discovery
      SPRING_PROFILES_ACTIVE: prod
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8761/eureka/status"]
      interval: 10s
      timeout: 5s
      retries: 5

  api-gateway:
    image: ${docker_image_uri}/api-gateway:latest
    ports:
      - "8080:8080"
    environment:
      SPRING_APPLICATION_NAME: api-gateway
      SPRING_PROFILES_ACTIVE: prod
      EUREKA_CLIENT_SERVICEURL_DEFAULTZONE: http://eureka:8761/eureka/
    depends_on:
      eureka:
        condition: service_healthy
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
      interval: 15s
      timeout: 5s
      retries: 5

  auth-service:
    image: ${docker_image_uri}/auth-service:latest
    environment:
      SPRING_APPLICATION_NAME: auth-service
      SPRING_PROFILES_ACTIVE: prod
      EUREKA_CLIENT_SERVICEURL_DEFAULTZONE: http://eureka:8761/eureka/
      SPRING_DATASOURCE_URL: jdbc:postgresql://${postgres_endpoint}/shopsphere
      SPRING_DATASOURCE_USERNAME: ${postgres_username}
      SPRING_DATASOURCE_PASSWORD: ${postgres_password}
      JWT_SECRET: ${jwt_secret}
    depends_on:
      - eureka
    restart: unless-stopped

  catalog-service:
    image: ${docker_image_uri}/catalog-service:latest
    environment:
      SPRING_APPLICATION_NAME: catalog-service
      SPRING_PROFILES_ACTIVE: prod
      EUREKA_CLIENT_SERVICEURL_DEFAULTZONE: http://eureka:8761/eureka/
      SPRING_DATA_MONGODB_URI: mongodb://${mongo_username}:${mongo_password}@${mongo_endpoint}/shopsphere?authSource=admin
    depends_on:
      - eureka
    restart: unless-stopped

  payment-service:
    image: ${docker_image_uri}/payment-service:latest
    environment:
      SPRING_APPLICATION_NAME: payment-service
      SPRING_PROFILES_ACTIVE: prod
      EUREKA_CLIENT_SERVICEURL_DEFAULTZONE: http://eureka:8761/eureka/
      SPRING_DATASOURCE_URL: jdbc:postgresql://${postgres_endpoint}/shopsphere
      SPRING_DATASOURCE_USERNAME: ${postgres_username}
      SPRING_DATASOURCE_PASSWORD: ${postgres_password}
      STRIPE_API_KEY: ${stripe_api_key}
      STRIPE_WEBHOOK_SECRET: ${stripe_webhook_secret}
    depends_on:
      - eureka
    restart: unless-stopped

  checkout-service:
    image: ${docker_image_uri}/checkout-service:latest
    environment:
      SPRING_APPLICATION_NAME: checkout-service
      SPRING_PROFILES_ACTIVE: prod
      EUREKA_CLIENT_SERVICEURL_DEFAULTZONE: http://eureka:8761/eureka/
    depends_on:
      - eureka
    restart: unless-stopped

  notification-service:
    image: ${docker_image_uri}/notification-service:latest
    environment:
      SPRING_APPLICATION_NAME: notification-service
      SPRING_PROFILES_ACTIVE: prod
      EUREKA_CLIENT_SERVICEURL_DEFAULTZONE: http://eureka:8761/eureka/
    depends_on:
      - eureka
    restart: unless-stopped

networks:
  default:
    driver: bridge
EOF

# Start services
cd /app/shopsphere
docker-compose up -d

# Configure log rotation
cat > /etc/logrotate.d/shopsphere << EOF
/var/log/shopsphere/*.log {
    daily
    rotate 7
    compress
    delaycompress
    notifempty
    create 0640 ec2-user ec2-user
}
EOF

# Setup cron job for health checks
cat > /etc/cron.d/shopsphere-health << EOF
*/5 * * * * ec2-user curl -f http://localhost:8080/actuator/health > /dev/null 2>&1 || systemctl restart docker-shopsphere
EOF

# Create systemd service for docker-compose
cat > /etc/systemd/system/shopsphere.service << EOF
[Unit]
Description=ShopSphere Docker Compose Services
After=docker.service
Requires=docker.service

[Service]
Type=simple
User=ec2-user
WorkingDirectory=/app/shopsphere
ExecStart=/usr/local/bin/docker-compose up
ExecStop=/usr/local/bin/docker-compose down
Restart=unless-stopped
RestartSec=10

[Install]
WantedBy=multi-user.target
EOF

systemctl daemon-reload
systemctl enable shopsphere.service

echo "ShopSphere backend initialized successfully"
