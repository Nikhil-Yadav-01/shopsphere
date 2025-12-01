# Outputs for ShopSphere Infrastructure

output "vpc_id" {
  description = "VPC ID"
  value       = aws_vpc.main.id
}

output "vpc_cidr" {
  description = "VPC CIDR block"
  value       = aws_vpc.main.cidr_block
}

output "public_subnet_ids" {
  description = "IDs of public subnets"
  value       = aws_subnet.public[*].id
}

output "private_subnet_ids" {
  description = "IDs of private subnets"
  value       = aws_subnet.private[*].id
}

output "alb_dns_name" {
  description = "DNS name of the Application Load Balancer"
  value       = aws_lb.main.dns_name
}

output "alb_arn" {
  description = "ARN of the Application Load Balancer"
  value       = aws_lb.main.arn
}

output "rds_postgres_endpoint" {
  description = "RDS PostgreSQL endpoint"
  value       = var.enable_rds ? aws_rds_instance.postgres[0].endpoint : "Not using RDS"
}

output "rds_postgres_username" {
  description = "RDS PostgreSQL username"
  value       = var.postgres_username
  sensitive   = true
}

output "rds_postgres_database" {
  description = "RDS PostgreSQL database name"
  value       = "shopsphere"
}

output "mongodb_instance_private_ip" {
  description = "Private IP of MongoDB EC2 instance"
  value       = !var.enable_documentdb ? aws_instance.mongodb[0].private_ip : "Using DocumentDB"
}

output "backend_asg_name" {
  description = "Auto Scaling Group name for backend services"
  value       = aws_autoscaling_group.backend.name
}

output "backend_asg_capacity" {
  description = "Current capacity of Backend Auto Scaling Group"
  value = {
    min     = aws_autoscaling_group.backend.min_size
    desired = aws_autoscaling_group.backend.desired_capacity
    max     = aws_autoscaling_group.backend.max_size
  }
}

output "cloudwatch_log_group" {
  description = "CloudWatch log group for ECS"
  value       = aws_cloudwatch_log_group.ecs.name
}

output "security_groups" {
  description = "Security group IDs"
  value = {
    alb      = aws_security_group.alb.id
    backend  = aws_security_group.backend.id
    database = aws_security_group.database.id
  }
}

output "region" {
  description = "AWS region"
  value       = var.aws_region
}

output "environment" {
  description = "Environment"
  value       = var.environment
}

output "application_name" {
  description = "Application name"
  value       = var.app_name
}

output "connection_strings" {
  description = "Connection strings for databases and services"
  value = {
    api_gateway    = "http://${aws_lb.main.dns_name}"
    postgres       = var.enable_rds ? "postgresql://${var.postgres_username}:${var.postgres_password}@${aws_rds_instance.postgres[0].address}:5432/shopsphere" : "EC2-based"
    mongodb        = !var.enable_documentdb ? "mongodb://${var.mongo_username}:${var.mongo_password}@${aws_instance.mongodb[0].private_ip}:27017" : "DocumentDB"
  }
  sensitive = true
}

output "deployment_info" {
  description = "Deployment information"
  value = {
    account_id           = data.aws_caller_identity.current.account_id
    availability_zones   = local.azs
    nat_gateway_eips     = aws_eip.nat[*].public_ip
    alb_public_ip        = aws_lb.main.zone_id
  }
}

output "next_steps" {
  description = "Next steps to complete the setup"
  value = [
    "1. Update security group rules (SSH CIDR) in variables.tf for your IP",
    "2. Create .ssh/id_rsa.pub SSH key: ssh-keygen -t rsa -b 4096",
    "3. Deploy services to ALB using CI/CD pipeline",
    "4. Update Route53 DNS to point to ALB: ${aws_lb.main.dns_name}",
    "5. Configure HTTPS listener with ACM certificate",
    "6. Setup CloudWatch dashboards for monitoring",
    "7. Enable RDS automated backups and snapshots"
  ]
}
