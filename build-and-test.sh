#!/bin/bash
# ShopSphere Build & Test Script
# Builds individual services and performs local testing

set -e

COLOR_GREEN='\033[0;32m'
COLOR_BLUE='\033[0;34m'
COLOR_YELLOW='\033[1;33m'
COLOR_RED='\033[0;31m'
NC='\033[0m' # No Color

# Configuration
SERVICES=(
  "auth-service"
  "user-service"
  "catalog-service"
  "cart-service"
  "inventory-service"
  "order-service"
  "payment-service"
  "checkout-service"
  "notification-service"
  "shipping-service"
  "review-service"
  "fraud-service"
  "pricing-service"
  "media-service"
  "search-service"
  "recommendation-service"
  "admin-service"
  "batch-service"
  "analytics-service"
  "coupon-service"
  "returns-service"
)

INFRA_SERVICES=(
  "discovery"
  "config-server"
  "api-gateway"
)

# Functions
print_header() {
  echo -e "${COLOR_BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
  echo -e "${COLOR_BLUE}$1${NC}"
  echo -e "${COLOR_BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
}

print_success() {
  echo -e "${COLOR_GREEN}✓ $1${NC}"
}

print_error() {
  echo -e "${COLOR_RED}✗ $1${NC}"
}

print_warning() {
  echo -e "${COLOR_YELLOW}⚠ $1${NC}"
}

# Check prerequisites
check_prerequisites() {
  print_header "Checking Prerequisites"
  
  if ! command -v docker &> /dev/null; then
    print_error "Docker is not installed"
    exit 1
  fi
  print_success "Docker is installed: $(docker --version)"
  
  if ! command -v docker-compose &> /dev/null; then
    print_error "Docker Compose is not installed"
    exit 1
  fi
  print_success "Docker Compose is installed: $(docker-compose --version)"
  
  if ! command -v mvn &> /dev/null; then
    print_error "Maven is not installed"
    exit 1
  fi
  print_success "Maven is installed: $(mvn --version | head -1)"
}

# Local Maven build
build_locally() {
  print_header "Local Maven Build (Compilation Check)"
  
  echo "Building common modules..."
  mvn -B clean package -pl common-models,common-utils,common-security,common-kafka,common-db -am -DskipTests
  
  print_success "Common modules built"
  
  # Build each service
  for service in "${SERVICES[@]}"; do
    echo -e "${COLOR_YELLOW}Building $service...${NC}"
    if mvn -B clean package -pl services/$service -am -DskipTests > /tmp/$service-build.log 2>&1; then
      print_success "$service built successfully"
    else
      print_error "$service build failed"
      echo "See /tmp/$service-build.log for details"
      return 1
    fi
  done
  
  # Build infrastructure services
  for service in "${INFRA_SERVICES[@]}"; do
    echo -e "${COLOR_YELLOW}Building $service...${NC}"
    if mvn -B clean package -pl services/$service -am -DskipTests > /tmp/$service-build.log 2>&1; then
      print_success "$service built successfully"
    else
      print_error "$service build failed"
      echo "See /tmp/$service-build.log for details"
      return 1
    fi
  done
}

# Docker build
docker_build() {
  print_header "Building Docker Images"
  
  # Build infrastructure services first
  for service in "${INFRA_SERVICES[@]}"; do
    echo -e "${COLOR_YELLOW}Building Docker image for $service...${NC}"
    if docker build -f services/$service/Dockerfile -t shopsphere-$service:latest . > /tmp/$service-docker.log 2>&1; then
      print_success "Docker image built: shopsphere-$service:latest"
    else
      print_error "Docker image build failed for $service"
      echo "See /tmp/$service-docker.log for details"
      return 1
    fi
  done
  
  # Build microservices
  for service in "${SERVICES[@]}"; do
    echo -e "${COLOR_YELLOW}Building Docker image for $service...${NC}"
    if docker build -f services/$service/Dockerfile -t shopsphere-$service:latest . > /tmp/$service-docker.log 2>&1; then
      print_success "Docker image built: shopsphere-$service:latest"
    else
      print_error "Docker image build failed for $service"
      echo "See /tmp/$service-docker.log for details"
      return 1
    fi
  done
}

# Start Docker Compose
docker_start() {
  print_header "Starting Docker Compose Stack"
  
  docker-compose -f docker-compose-full.yml up -d
  
  echo "Waiting for services to be healthy..."
  sleep 10
  
  print_success "Docker Compose stack started"
  echo "Checking service health..."
  
  docker-compose -f docker-compose-full.yml ps
}

# Health checks
health_checks() {
  print_header "Performing Health Checks"
  
  # Check Eureka
  echo "Checking Eureka Service Discovery..."
  if curl -f http://localhost:8761/eureka/status &>/dev/null; then
    print_success "Eureka is healthy"
  else
    print_warning "Eureka is not responding yet (may still be starting)"
  fi
  
  # Check Auth Service
  echo "Checking Auth Service..."
  if curl -f http://localhost:8001/actuator/health &>/dev/null; then
    print_success "Auth Service is healthy"
  else
    print_warning "Auth Service is not responding yet"
  fi
  
  # Check Order Service
  echo "Checking Order Service..."
  if curl -f http://localhost:8005/actuator/health &>/dev/null; then
    print_success "Order Service is healthy"
  else
    print_warning "Order Service is not responding yet"
  fi
  
  # Check API Gateway
  echo "Checking API Gateway..."
  if curl -f http://localhost:8080/actuator/health &>/dev/null; then
    print_success "API Gateway is healthy"
  else
    print_warning "API Gateway is not responding yet"
  fi
}

# Cleanup
cleanup() {
  print_header "Cleanup & Shutdown"
  
  read -p "Do you want to stop and remove all containers? (y/n) " -n 1 -r
  echo
  if [[ $REPLY =~ ^[Yy]$ ]]; then
    docker-compose -f docker-compose-full.yml down
    print_success "All containers stopped and removed"
  else
    print_warning "Containers still running. Use 'docker-compose -f docker-compose-full.yml down' to stop"
  fi
}

# Main
main() {
  print_header "ShopSphere Build & Test Suite"
  
  check_prerequisites
  
  # Parse arguments
  if [ "$1" == "local" ]; then
    build_locally
  elif [ "$1" == "docker" ]; then
    docker_build
  elif [ "$1" == "start" ]; then
    docker_start
    sleep 10
    health_checks
  elif [ "$1" == "health" ]; then
    health_checks
  elif [ "$1" == "stop" ]; then
    cleanup
  elif [ "$1" == "full" ]; then
    build_locally && docker_build && docker_start && sleep 20 && health_checks
  else
    echo -e "${COLOR_YELLOW}Usage:${NC}"
    echo "  ./build-and-test.sh local       - Local Maven build only"
    echo "  ./build-and-test.sh docker      - Build Docker images"
    echo "  ./build-and-test.sh start       - Start Docker Compose stack"
    echo "  ./build-and-test.sh health      - Run health checks"
    echo "  ./build-and-test.sh stop        - Stop containers"
    echo "  ./build-and-test.sh full        - Full build, Docker, and startup"
    echo ""
    echo -e "${COLOR_YELLOW}Examples:${NC}"
    echo "  # Full workflow: build locally → Docker images → start services"
    echo "  ./build-and-test.sh full"
    echo ""
    echo "  # Build only (no Docker)"
    echo "  ./build-and-test.sh local"
    echo ""
    echo "  # Start services (assumes images exist)"
    echo "  ./build-and-test.sh start"
  fi
}

# Run main
main "$@"
