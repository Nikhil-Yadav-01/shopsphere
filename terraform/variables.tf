variable "aws_region" {
  description = "AWS region"
  type        = string
  default     = "us-east-1"
}

variable "environment" {
  description = "Environment name"
  type        = string
  default     = "dev"
  validation {
    condition     = contains(["dev", "staging", "prod"], var.environment)
    error_message = "Environment must be dev, staging, or prod."
  }
}

variable "app_name" {
  description = "Application name"
  type        = string
  default     = "shopsphere"
}

variable "vpc_cidr" {
  description = "VPC CIDR block"
  type        = string
  default     = "10.0.0.0/16"
}

variable "container_port" {
  description = "Port on which containers listen"
  type        = number
  default     = 8080
}

variable "postgres_username" {
  description = "PostgreSQL master username"
  type        = string
  default     = "shopsphere"
  sensitive   = true
}

variable "postgres_password" {
  description = "PostgreSQL master password"
  type        = string
  sensitive   = true
  validation {
    condition     = length(var.postgres_password) >= 16
    error_message = "PostgreSQL password must be at least 16 characters."
  }
}

variable "postgres_version" {
  description = "PostgreSQL version"
  type        = string
  default     = "16.1"
}

variable "mongo_username" {
  description = "MongoDB root username"
  type        = string
  default     = "shopsphere"
  sensitive   = true
}

variable "mongo_password" {
  description = "MongoDB root password"
  type        = string
  sensitive   = true
  validation {
    condition     = length(var.mongo_password) >= 16
    error_message = "MongoDB password must be at least 16 characters."
  }
}

variable "instance_type" {
  description = "EC2 instance type"
  type        = string
  default     = "t3.medium"
}

variable "min_capacity" {
  description = "Minimum number of backend instances"
  type        = number
  default     = 2
}

variable "max_capacity" {
  description = "Maximum number of backend instances"
  type        = number
  default     = 4
}

variable "jwt_secret" {
  description = "JWT secret for authentication"
  type        = string
  sensitive   = true
  validation {
    condition     = length(var.jwt_secret) >= 32
    error_message = "JWT secret must be at least 32 characters."
  }
}

variable "stripe_api_key" {
  description = "Stripe API key"
  type        = string
  sensitive   = true
  default     = ""
}

variable "stripe_webhook_secret" {
  description = "Stripe webhook secret"
  type        = string
  sensitive   = true
  default     = ""
}

variable "enable_rds" {
  description = "Use RDS for PostgreSQL instead of EC2"
  type        = bool
  default     = true
}

variable "enable_documentdb" {
  description = "Use DocumentDB for MongoDB instead of EC2"
  type        = bool
  default     = false
}

variable "docker_image_uri" {
  description = "ECR URI for Docker images"
  type        = string
  default     = ""
  # Example: "123456789012.dkr.ecr.us-east-1.amazonaws.com/shopsphere"
}

variable "tags" {
  description = "Common tags for all resources"
  type        = map(string)
  default = {
    Project     = "ShopSphere"
    ManagedBy   = "Terraform"
    CreatedDate = "2025-12-01"
  }
}

variable "enable_monitoring" {
  description = "Enable CloudWatch monitoring"
  type        = bool
  default     = true
}

variable "enable_backup" {
  description = "Enable automated backups"
  type        = bool
  default     = true
}

variable "backup_retention_days" {
  description = "Number of days to retain backups"
  type        = number
  default     = 7
  validation {
    condition     = var.backup_retention_days > 0 && var.backup_retention_days <= 35
    error_message = "Backup retention must be between 1 and 35 days."
  }
}

variable "allowed_ssh_cidr" {
  description = "CIDR blocks allowed for SSH access"
  type        = list(string)
  default     = ["0.0.0.0/0"]  # CHANGE THIS IN PRODUCTION
}
