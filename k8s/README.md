# ShopSphere Kubernetes Deployment Guide

This directory contains Kubernetes manifests for deploying ShopSphere microservices on a Kubernetes cluster.

## Prerequisites

- Kubernetes 1.24+
- `kubectl` command-line tool configured
- Persistent Volume support (for databases)
- Optional: Ingress controller (nginx-ingress recommended)
- Optional: Cert-manager for TLS

## Directory Structure

```
k8s/
├── namespace.yaml              # Kubernetes namespace
├── secrets.yaml                # Sensitive data (JWT, Stripe)
├── postgres-config.yaml        # PostgreSQL config and secrets
├── postgres-deployment.yaml    # PostgreSQL database
├── mongodb-deployment.yaml     # MongoDB database
├── service-deployment.yaml     # All microservices
├── ingress.yaml               # Ingress configuration
└── README.md
```

## Deployment Instructions

### 1. Create Namespace and Base Infrastructure

```bash
# Create the ShopSphere namespace
kubectl apply -f k8s/namespace.yaml

# Apply secrets and configurations
kubectl apply -f k8s/secrets.yaml
kubectl apply -f k8s/postgres-config.yaml
```

### 2. Deploy Databases

```bash
# Deploy PostgreSQL
kubectl apply -f k8s/postgres-deployment.yaml

# Deploy MongoDB
kubectl apply -f k8s/mongodb-deployment.yaml

# Wait for databases to be ready
kubectl wait --for=condition=Ready pod -l app=postgres -n shopsphere --timeout=300s
kubectl wait --for=condition=Ready pod -l app=mongodb -n shopsphere --timeout=300s
```

### 3. Deploy Microservices

```bash
# Deploy all services (Eureka, API Gateway, Auth, Catalog, Payment, Checkout)
kubectl apply -f k8s/service-deployment.yaml

# Wait for services to be ready
kubectl wait --for=condition=Ready pod -l tier=service -n shopsphere --timeout=600s
```

### 4. Deploy Ingress (Optional)

```bash
# Deploy Ingress controller
kubectl apply -f k8s/ingress.yaml

# Verify ingress is created
kubectl get ingress -n shopsphere
```

## Monitoring and Debugging

### Check Deployment Status

```bash
# List all deployments
kubectl get deployments -n shopsphere

# Get pod status
kubectl get pods -n shopsphere

# Describe a specific pod
kubectl describe pod <pod-name> -n shopsphere
```

### View Logs

```bash
# View logs from a service
kubectl logs -n shopsphere deployment/auth-service

# Follow logs in real-time
kubectl logs -f -n shopsphere deployment/auth-service

# View logs from all containers in a pod
kubectl logs -n shopsphere <pod-name> --all-containers=true
```

### Port Forwarding for Development

```bash
# Forward to API Gateway
kubectl port-forward -n shopsphere svc/api-gateway 8080:80

# Forward to Eureka
kubectl port-forward -n shopsphere svc/eureka 8761:8761

# Forward to PostgreSQL
kubectl port-forward -n shopsphere svc/postgres 5432:5432

# Forward to MongoDB
kubectl port-forward -n shopsphere svc/mongodb 27017:27017
```

### Execute Commands in Containers

```bash
# Connect to a service container
kubectl exec -it -n shopsphere deployment/auth-service -- /bin/sh

# Run a command in a container
kubectl exec -n shopsphere deployment/auth-service -- curl http://eureka:8761/eureka/status
```

## Scaling Services

```bash
# Scale a deployment to 3 replicas
kubectl scale deployment auth-service -n shopsphere --replicas=3

# Auto-scale based on CPU usage
kubectl autoscale deployment catalog-service -n shopsphere --min=2 --max=5 --cpu-percent=80
```

## Updating Configurations

### Updating Secrets

```bash
# Edit secrets
kubectl edit secret jwt-secret -n shopsphere

# Or apply updated secrets
kubectl apply -f k8s/secrets.yaml
```

### Rolling Updates

```bash
# Update the image version
kubectl set image deployment/payment-service \
  payment-service=shopsphere/payment-service:v1.1.0 \
  -n shopsphere

# Monitor rollout status
kubectl rollout status deployment/payment-service -n shopsphere

# Rollback if needed
kubectl rollout undo deployment/payment-service -n shopsphere
```

## Database Management

### PostgreSQL

```bash
# Connect to PostgreSQL
kubectl exec -it -n shopsphere deployment/postgres -- psql -U shopsphere -d shopsphere

# Run SQL commands
kubectl exec -n shopsphere deployment/postgres -- \
  psql -U shopsphere -d shopsphere -c "SELECT version();"

# Backup database
kubectl exec -n shopsphere deployment/postgres -- \
  pg_dump -U shopsphere shopsphere > backup.sql
```

### MongoDB

```bash
# Connect to MongoDB
kubectl exec -it -n shopsphere deployment/mongodb -- mongosh -u shopsphere -p mongodb --authenticationDatabase admin

# List collections
kubectl exec -n shopsphere deployment/mongodb -- \
  mongosh -u shopsphere -p mongodb --authenticationDatabase admin --eval "show collections"
```

## Production Considerations

### Storage

- PostgreSQL and MongoDB use `local` storage driver for demo
- For production, use cloud-specific storage classes:
    - AWS: `ebs-sc` (EBS)
    - Azure: `managed-premium`
    - GCP: `pd-ssd`

Update the PersistentVolumeClaim `storageClassName` field:

```yaml
spec:
  storageClassName: gp3  # For AWS EBS
  # or
  storageClassName: managed-premium  # For Azure
```

### Resource Limits

Current limits are suitable for development. For production, adjust based on expected traffic:

```yaml
resources:
  requests:
    memory: "1Gi"    # Increase for larger deployments
    cpu: "500m"
  limits:
    memory: "2Gi"
    cpu: "1000m"
```

### High Availability

For production, increase replicas:

```bash
# Scale API Gateway to 3 replicas
kubectl scale deployment api-gateway -n shopsphere --replicas=3

# Scale services to at least 2 replicas
kubectl scale deployment auth-service -n shopsphere --replicas=2
kubectl scale deployment catalog-service -n shopsphere --replicas=2
kubectl scale deployment payment-service -n shopsphere --replicas=2
```

### Security

1. **Update Secrets**: Change default values in `secrets.yaml`:

```bash
# Generate new JWT secret
openssl rand -base64 32

# Update Stripe keys from production
# Update database passwords
```

2. **Network Policies**: Restrict traffic between pods:

```yaml
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: shopsphere-network-policy
  namespace: shopsphere
spec:
  podSelector: {}
  policyTypes:
  - Ingress
  - Egress
  ingress:
  - from:
    - podSelector: {}
  egress:
  - to:
    - podSelector: {}
```

3. **RBAC**: Create service accounts with minimal permissions

4. **Pod Security**: Use Pod Security Policies or Standards

## Cleanup

```bash
# Delete all resources in shopsphere namespace
kubectl delete namespace shopsphere

# Or delete specific resources
kubectl delete -f k8s/ -n shopsphere
```

## Troubleshooting

### Pods Not Starting

```bash
# Check pod status
kubectl describe pod <pod-name> -n shopsphere

# Look for errors in events section
# Check resource constraints
kubectl top nodes
kubectl top pods -n shopsphere
```

### Service Discovery Issues

```bash
# Test service discovery
kubectl exec -n shopsphere deployment/auth-service -- nslookup eureka

# Verify DNS resolution
kubectl run -it --rm debug --image=nicolaka/netshoot --restart=Never -n shopsphere -- nslookup eureka
```

### Database Connection Issues

```bash
# Test PostgreSQL connectivity
kubectl exec -n shopsphere deployment/postgres -- pg_isready -U shopsphere

# Test MongoDB connectivity
kubectl exec -n shopsphere deployment/mongodb -- \
  mongosh -u shopsphere -p mongodb --authenticationDatabase admin --eval "db.adminCommand('ping')"
```

### Memory/CPU Issues

```bash
# Check resource usage
kubectl top pods -n shopsphere

# Check node resources
kubectl top nodes

# Increase limits if needed
kubectl set resources deployment auth-service -n shopsphere \
  --limits=cpu=1000m,memory=1Gi \
  --requests=cpu=500m,memory=512Mi
```

## References

- [Kubernetes Documentation](https://kubernetes.io/docs/)
- [Spring Boot on Kubernetes](https://spring.io/blog/2021/06/21/the-path-to-spring-boot-3-0)
- [PostgreSQL Kubernetes Operator](https://www.crunchydata.com/blog/postgres-operator)
- [MongoDB Kubernetes Operator](https://docs.mongodb.com/kubernetes-operator/)
