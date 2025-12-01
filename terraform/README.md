# ShopSphere Terraform Infrastructure as Code

This directory contains Terraform configuration to deploy ShopSphere on AWS.

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────┐
│                        AWS VPC (10.0.0.0/16)                │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  Public Subnets (3x, for ALB)                        │  │
│  │  - 10.0.101.0/24, 10.0.102.0/24, 10.0.103.0/24      │  │
│  │  - NAT Gateways for private subnet egress            │  │
│  └──────────────────────────────────────────────────────┘  │
│         │                                                    │
│         ▼                                                    │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  Application Load Balancer (ALB)                      │  │
│  │  - Port 80/443                                        │  │
│  │  - Routes to backend services                         │  │
│  └──────────────────────────────────────────────────────┘  │
│         │                                                    │
│         ▼                                                    │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  Private Subnets (3x, for EC2)                       │  │
│  │  - 10.0.1.0/24, 10.0.2.0/24, 10.0.3.0/24            │  │
│  │  - EC2 instances in Auto Scaling Group               │  │
│  │  - Docker containers with microservices              │  │
│  └──────────────────────────────────────────────────────┘  │
│         │                                                    │
│         ├──────────────────┬────────────────────┐           │
│         ▼                  ▼                    ▼           │
│    ┌────────────┐  ┌──────────────┐  ┌──────────────┐     │
│    │ RDS        │  │ EC2 MongoDB  │  │ DocumentDB   │     │
│    │ PostgreSQL │  │ (if enabled) │  │ (if enabled) │     │
│    └────────────┘  └──────────────┘  └──────────────┘     │
│    (Private SG)    (Private SG)        (Private SG)        │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

## Prerequisites

### Local Requirements

1. **Terraform** >= 1.0
   ```bash
   # Install Terraform (macOS)
   brew install terraform
   
   # Verify installation
   terraform version
   ```

2. **AWS CLI** >= 2.0
   ```bash
   # Install AWS CLI
   brew install awscli
   
   # Configure credentials
   aws configure
   # Enter: AWS Access Key ID, Secret Access Key, Default region, Default output format
   ```

3. **SSH Key Pair**
   ```bash
   # Generate SSH key if you don't have one
   ssh-keygen -t rsa -b 4096 -f ~/.ssh/id_rsa -N ""
   ```

### AWS Requirements

1. **AWS Account** with appropriate permissions
   - EC2, RDS, VPC, Auto Scaling, CloudWatch, IAM
   
2. **S3 Bucket** for Terraform state (optional but recommended)
   ```bash
   aws s3 mb s3://shopsphere-terraform-state
   aws s3api put-bucket-versioning \
     --bucket shopsphere-terraform-state \
     --versioning-configuration Status=Enabled
   ```

3. **DynamoDB Table** for state locking (optional but recommended)
   ```bash
   aws dynamodb create-table \
     --table-name shopsphere-terraform-locks \
     --attribute-definitions AttributeName=LockID,AttributeType=S \
     --key-schema AttributeName=LockID,KeyType=HASH \
     --provisioned-throughput ReadCapacityUnits=5,WriteCapacityUnits=5
   ```

## Quick Start

### 1. Setup Terraform Variables

```bash
# Copy example variables file
cp terraform.tfvars.example terraform.tfvars

# Edit with your values
vim terraform.tfvars
```

**Important variables:**

```hcl
aws_region = "us-east-1"
environment = "dev"  # or "staging" / "prod"

# Database passwords (minimum 16 characters)
postgres_password = "GenerateStrongPassword123!"
mongo_password = "GenerateStrongPassword456!"

# JWT Secret (minimum 32 characters)
jwt_secret = "GenerateStrongJwtSecret12345678901234567890"

# Security: Restrict SSH access
allowed_ssh_cidr = ["YOUR_IP_ADDRESS/32"]

# Stripe (optional)
stripe_api_key = "sk_test_..."
stripe_webhook_secret = "whsec_..."

# Database strategy
enable_rds = true           # Recommended for production
enable_documentdb = false   # Set to true for AWS-managed MongoDB
```

### 2. Initialize Terraform

```bash
# Initialize Terraform working directory
terraform init

# Verify backend configuration
terraform init -backend-config="key=dev/terraform.tfstate"
```

### 3. Plan Infrastructure

```bash
# Preview changes (dry run)
terraform plan -out=tfplan

# Review the plan output carefully
```

### 4. Apply Infrastructure

```bash
# Create AWS resources
terraform apply tfplan

# This will take 15-20 minutes

# Wait for completion and note the outputs
```

### 5. Verify Deployment

```bash
# Get deployment information
terraform output

# Test API Gateway
ALB_DNS=$(terraform output -raw alb_dns_name)
curl http://$ALB_DNS/actuator/health

# SSH into an instance (if needed)
aws ec2 describe-instances --filters "Name=tag:Name,Values=shopsphere-*" \
  --query 'Reservations[0].Instances[0].PrivateIpAddress' \
  --output text
```

## Infrastructure Components

### VPC & Networking

- **VPC**: 10.0.0.0/16 with DNS support
- **Public Subnets**: 3x for ALB and NAT Gateways
- **Private Subnets**: 3x for EC2 instances
- **NAT Gateways**: 3x (one per AZ) for high availability
- **Internet Gateway**: For public internet access

### Compute

- **Application Load Balancer**: 
  - Listens on port 80 (HTTP) and 443 (HTTPS - manual setup)
  - Health checks every 30 seconds
  - Sticky sessions enabled (24 hours)

- **Auto Scaling Group**:
  - Minimum: 2 instances
  - Desired: 2 instances
  - Maximum: 4 instances
  - Scales based on CPU and memory metrics

- **EC2 Instances**:
  - Type: t3.medium (configurable)
  - OS: Amazon Linux 2
  - Storage: 30GB gp3 encrypted
  - Monitoring: CloudWatch enabled

### Databases

- **RDS PostgreSQL** (if enabled):
  - Version: 16.1
  - Instance class: db.t3.micro
  - Storage: 20GB gp3 encrypted
  - Multi-AZ: Yes (production only)
  - Backups: 7 days retention
  - Performance Insights: Enabled (prod)

- **MongoDB EC2** (if DocumentDB disabled):
  - Container: Docker on EC2
  - Storage: 30GB encrypted
  - Private network access only
  - Initialization with collections and indexes

- **DocumentDB** (if enabled):
  - AWS-managed MongoDB compatibility
  - Multi-AZ deployment
  - Automatic backups

### Monitoring & Logging

- **CloudWatch Log Group**: `/ecs/shopsphere-{env}`
- **CloudWatch Alarms**:
  - ALB unhealthy host count
  - RDS CPU utilization
  - RDS free storage space
- **CloudWatch Agent**: On all EC2 instances

### Security

- **Security Groups**:
  - ALB: Ports 80/443 from anywhere
  - Backend: Container ports from ALB only
  - Database: PostgreSQL (5432) and MongoDB (27017) from backend only

- **IAM Roles**:
  - EC2 instances: CloudWatch, SSM, S3 access
  - No hardcoded credentials

- **Encryption**:
  - RDS: Encrypted at rest
  - EBS volumes: Encrypted at rest
  - Secrets: Managed via environment variables

## Managing Infrastructure

### Scaling

```bash
# Scale up ASG manually
aws autoscaling set-desired-capacity \
  --auto-scaling-group-name shopsphere-dev-backend-asg \
  --desired-capacity 4

# Enable auto-scaling based on CPU
aws autoscaling put-scaling-policy \
  --auto-scaling-group-name shopsphere-dev-backend-asg \
  --policy-name cpu-scaling \
  --policy-type TargetTrackingScaling \
  --target-tracking-configuration file://cpu-scaling.json
```

### Updating Configuration

```bash
# Update terraform.tfvars
vim terraform.tfvars

# Plan and apply changes
terraform plan -out=tfplan
terraform apply tfplan
```

### Monitoring

```bash
# View CloudWatch logs
aws logs tail /ecs/shopsphere-dev --follow

# Get ALB metrics
aws cloudwatch get-metric-statistics \
  --namespace AWS/ApplicationELB \
  --metric-name TargetResponseTime \
  --dimensions Name=LoadBalancer,Value=$(terraform output -raw alb_arn | cut -d: -f6) \
  --start-time 2025-01-01T00:00:00Z \
  --end-time 2025-12-31T23:59:59Z \
  --period 300 \
  --statistics Average

# Get RDS metrics
aws cloudwatch get-metric-statistics \
  --namespace AWS/RDS \
  --metric-name CPUUtilization \
  --dimensions Name=DBInstanceIdentifier,Value=shopsphere-dev-postgres \
  --start-time 2025-01-01T00:00:00Z \
  --end-time 2025-12-31T23:59:59Z \
  --period 300 \
  --statistics Average
```

### Backup & Recovery

```bash
# Manual RDS snapshot
aws rds create-db-snapshot \
  --db-instance-identifier shopsphere-dev-postgres \
  --db-snapshot-identifier shopsphere-dev-postgres-manual-snapshot

# List snapshots
aws rds describe-db-snapshots --db-instance-identifier shopsphere-dev-postgres

# Restore from snapshot
aws rds restore-db-instance-from-db-snapshot \
  --db-instance-identifier shopsphere-dev-postgres-restored \
  --db-snapshot-identifier shopsphere-dev-postgres-manual-snapshot
```

## Updating Docker Images

```bash
# SSH into an instance via Systems Manager
aws ssm start-session --target <instance-id>

# Update docker-compose.yml
sudo vim /app/shopsphere/docker-compose.yml

# Restart services
cd /app/shopsphere
sudo docker-compose pull
sudo docker-compose up -d

# Verify services
docker-compose ps
```

## Destroying Infrastructure

```bash
# Review what will be destroyed
terraform plan -destroy

# Destroy all resources (CAUTION!)
terraform destroy

# Confirm when prompted
```

## Production Checklist

Before deploying to production:

- [ ] Use `environment = "prod"` in variables
- [ ] Set `enable_rds = true` for managed PostgreSQL
- [ ] Set `enable_documentdb = true` for managed MongoDB
- [ ] Enable Multi-AZ for RDS
- [ ] Enable Performance Insights for RDS
- [ ] Set `min_capacity = 3` and `max_capacity = 6`
- [ ] Restrict `allowed_ssh_cidr` to your network
- [ ] Set strong `postgres_password`, `mongo_password`, `jwt_secret`
- [ ] Configure `stripe_api_key` and `stripe_webhook_secret`
- [ ] Setup CloudFront CDN for static assets
- [ ] Configure Route53 DNS
- [ ] Setup ACM certificate for HTTPS
- [ ] Enable S3 versioning for Terraform state
- [ ] Enable DynamoDB locks for state management
- [ ] Configure backup retention (7-30 days)
- [ ] Setup CloudWatch alarms and SNS notifications
- [ ] Enable VPC Flow Logs for security monitoring
- [ ] Configure AWS Config for compliance
- [ ] Setup IAM MFA for AWS console access

## Troubleshooting

### Common Issues

#### 1. SSH Key Not Found

```bash
# Error: "ssh: public key not found"

# Solution: Create SSH key
ssh-keygen -t rsa -b 4096 -f ~/.ssh/id_rsa -N ""

# Update terraform.tfvars
aws_key_pair_public_key = file("~/.ssh/id_rsa.pub")
```

#### 2. S3 Backend Bucket Doesn't Exist

```bash
# Error: "Error: error reading S3 Bucket"

# Solution: Create S3 bucket or comment out backend in main.tf
aws s3 mb s3://shopsphere-terraform-state
```

#### 3. Permission Denied

```bash
# Error: "An error occurred (AccessDenied) when calling..."

# Solution: Check AWS credentials
aws sts get-caller-identity

# Ensure user has EC2, RDS, VPC, IAM permissions
```

#### 4. Insufficient Capacity

```bash
# Error: "InsufficientInstanceCapacity in AZ..."

# Solution: Change instance type or region
variable "instance_type" = "t3.small"  # Instead of t3.medium
```

### Debugging

```bash
# Enable debug logging
TF_LOG=DEBUG terraform plan

# Check current state
terraform state list
terraform state show aws_vpc.main

# Validate configuration
terraform validate

# Format code
terraform fmt -recursive
```

## References

- [Terraform AWS Provider](https://registry.terraform.io/providers/hashicorp/aws/latest/docs)
- [AWS VPC Documentation](https://docs.aws.amazon.com/vpc/)
- [AWS RDS Documentation](https://docs.aws.amazon.com/rds/)
- [AWS EC2 Auto Scaling](https://docs.aws.amazon.com/autoscaling/)
- [Terraform Best Practices](https://www.terraform.io/cloud-docs/best-practices)

## Support

For issues or questions:
1. Check the troubleshooting section
2. Review Terraform logs with `TF_LOG=DEBUG`
3. Check AWS CloudTrail for API errors
4. Review security group rules for connectivity issues
