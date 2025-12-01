# RDS PostgreSQL Database
resource "aws_db_subnet_group" "postgres" {
  count       = var.enable_rds ? 1 : 0
  name        = "${local.common_name}-postgres-subnet-group"
  subnet_ids  = aws_subnet.private[*].id
  description = "Subnet group for PostgreSQL"

  tags = {
    Name = "${local.common_name}-postgres-subnet-group"
  }
}

resource "aws_rds_instance" "postgres" {
  count                       = var.enable_rds ? 1 : 0
  identifier                  = "${local.common_name}-postgres"
  db_name                     = "shopsphere"
  engine                      = "postgres"
  engine_version              = var.postgres_version
  instance_class              = "db.t3.micro"
  allocated_storage            = 20
  storage_type                = "gp3"
  storage_encrypted           = true
  
  username                    = var.postgres_username
  password                    = var.postgres_password
  db_subnet_group_name        = aws_db_subnet_group.postgres[0].name
  vpc_security_group_ids      = [aws_security_group.database.id]
  
  publicly_accessible         = false
  skip_final_snapshot         = false
  final_snapshot_identifier   = "${local.common_name}-postgres-snapshot-${formatdate("YYYY-MM-DD-hhmm", timestamp())}"
  
  backup_retention_period     = var.backup_retention_days
  backup_window               = "03:00-04:00"
  maintenance_window          = "mon:04:00-mon:05:00"
  
  multi_az                    = var.environment == "prod"
  enable_cloudwatch_logs_exports = ["postgresql"]
  
  deletion_protection         = var.environment == "prod"
  performance_insights_enabled = var.environment == "prod"

  tags = {
    Name = "${local.common_name}-postgres"
  }

  depends_on = [aws_security_group.database]
}

# RDS Snapshot for Backup
resource "aws_db_instance_snapshot" "postgres_backup" {
  count              = var.enable_rds && var.enable_backup ? 1 : 0
  db_instance_identifier = aws_rds_instance.postgres[0].identifier
  snapshot_identifier = "${local.common_name}-postgres-backup-${formatdate("YYYY-MM-DD-hhmm", timestamp())}"

  tags = {
    Name = "${local.common_name}-postgres-backup"
  }

  depends_on = [aws_rds_instance.postgres]
}

# RDS Monitoring
resource "aws_cloudwatch_metric_alarm" "rds_cpu" {
  count             = var.enable_rds && var.enable_monitoring ? 1 : 0
  alarm_name        = "${local.common_name}-postgres-high-cpu"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = 2
  metric_name       = "CPUUtilization"
  namespace         = "AWS/RDS"
  period            = 300
  statistic         = "Average"
  threshold         = 80
  alarm_description = "Alert when RDS CPU exceeds 80%"
  treat_missing_data = "notBreaching"

  dimensions = {
    DBInstanceIdentifier = aws_rds_instance.postgres[0].id
  }
}

resource "aws_cloudwatch_metric_alarm" "rds_storage" {
  count             = var.enable_rds && var.enable_monitoring ? 1 : 0
  alarm_name        = "${local.common_name}-postgres-low-storage"
  comparison_operator = "LessThanThreshold"
  evaluation_periods  = 1
  metric_name       = "FreeStorageSpace"
  namespace         = "AWS/RDS"
  period            = 300
  statistic         = "Average"
  threshold         = 1073741824  # 1GB
  alarm_description = "Alert when RDS free storage is below 1GB"
  treat_missing_data = "notBreaching"

  dimensions = {
    DBInstanceIdentifier = aws_rds_instance.postgres[0].id
  }
}

# Outputs for RDS
output "rds_endpoint" {
  description = "RDS endpoint"
  value       = var.enable_rds ? aws_rds_instance.postgres[0].endpoint : null
}

output "rds_db_name" {
  description = "RDS database name"
  value       = var.enable_rds ? aws_rds_instance.postgres[0].db_name : null
}
