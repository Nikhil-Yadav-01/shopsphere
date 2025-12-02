# EC2 for MongoDB (when DocumentDB not used)
resource "aws_instance" "mongodb" {
  count                       = !var.enable_documentdb ? 1 : 0
  ami                         = data.aws_ami.amazon_linux_2.id
  instance_type               = "t3.medium"
  key_name                    = aws_key_pair.deployer.key_name
  subnet_id                   = aws_subnet.private[0].id
  vpc_security_group_ids = [aws_security_group.backend.id, aws_security_group.database.id]
  associate_public_ip_address = false

  root_block_device {
    volume_type           = "gp3"
    volume_size           = 30
    encrypted             = true
    delete_on_termination = true
  }

  user_data = base64encode(templatefile("${path.module}/scripts/mongodb-init.sh", {
    mongo_username = var.mongo_username
    mongo_password = var.mongo_password
  }))

  monitoring           = true
  iam_instance_profile = aws_iam_instance_profile.ec2_profile.name

  tags = {
    Name = "${local.common_name}-mongodb"
  }

  depends_on = [aws_security_group.backend, aws_security_group.database]
}

# Auto Scaling Group for Backend Services
resource "aws_launch_template" "backend" {
  name_prefix   = "${local.common_name}-backend-"
  image_id      = data.aws_ami.amazon_linux_2.id
  instance_type = var.instance_type
  key_name      = aws_key_pair.deployer.key_name

  block_device_mappings {
    device_name = "/dev/xvda"
    ebs {
      volume_type           = "gp3"
      volume_size           = 30
      delete_on_termination = true
      encrypted             = true
    }
  }

  iam_instance_profile {
    name = aws_iam_instance_profile.ec2_profile.name
  }

  monitoring {
    enabled = true
  }

  network_interfaces {
    associate_public_ip_address = false
    security_groups = [aws_security_group.backend.id]
    delete_on_termination       = true
  }

  user_data = base64encode(templatefile("${path.module}/scripts/backend-init.sh", {
    docker_image_uri      = var.docker_image_uri
    postgres_endpoint     = var.enable_rds ? aws_rds_instance.postgres[0].endpoint :
      "${aws_instance.mongodb[0].private_ip}:5432"
    postgres_username     = var.postgres_username
    postgres_password     = var.postgres_password
    mongo_endpoint        = !var.enable_documentdb ? "${aws_instance.mongodb[0].private_ip}:27017" :
      "documentdb.example.com:27017"
    mongo_username        = var.mongo_username
    mongo_password        = var.mongo_password
    jwt_secret            = var.jwt_secret
    stripe_api_key        = var.stripe_api_key
    stripe_webhook_secret = var.stripe_webhook_secret
    cloudwatch_log_group  = aws_cloudwatch_log_group.ecs.name
  }))

  tag_specifications {
    resource_type = "instance"
    tags = {
      Name = "${local.common_name}-backend"
    }
  }

  lifecycle {
    create_before_destroy = true
  }
}

resource "aws_autoscaling_group" "backend" {
  name                      = "${local.common_name}-backend-asg"
  vpc_zone_identifier       = aws_subnet.private[*].id
  target_group_arns = [aws_lb_target_group.backend.arn]
  health_check_type         = "ELB"
  health_check_grace_period = 300

  min_size         = var.min_capacity
  max_size         = var.max_capacity
  desired_capacity = var.min_capacity

  launch_template {
    id      = aws_launch_template.backend.id
    version = "$Latest"
  }

  tag {
    key                 = "Name"
    value               = "${local.common_name}-backend"
    propagate_at_launch = true
  }

  lifecycle {
    create_before_destroy = true
  }

  depends_on = [
    aws_security_group.backend,
    aws_rds_instance.postgres
  ]
}

# Application Load Balancer
resource "aws_lb" "main" {
  name               = "${local.common_name}-alb"
  internal           = false
  load_balancer_type = "application"
  security_groups = [aws_security_group.alb.id]
  subnets            = aws_subnet.public[*].id

  enable_deletion_protection       = false
  enable_http2                     = true
  enable_cross_zone_load_balancing = true

  tags = {
    Name = "${local.common_name}-alb"
  }
}

resource "aws_lb_target_group" "backend" {
  name        = "${local.common_name}-backend-tg"
  port        = var.container_port
  protocol    = "HTTP"
  vpc_id      = aws_vpc.main.id
  target_type = "instance"

  health_check {
    healthy_threshold   = 2
    unhealthy_threshold = 2
    timeout             = 5
    interval            = 30
    path                = "/actuator/health"
    matcher             = "200-299"
  }

  stickiness {
    type            = "lb_cookie"
    enabled         = true
    cookie_duration = 86400
  }

  tags = {
    Name = "${local.common_name}-backend-tg"
  }
}

resource "aws_lb_listener" "http" {
  load_balancer_arn = aws_lb.main.arn
  port              = "80"
  protocol          = "HTTP"

  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.backend.arn
  }
}

# CloudWatch Alarms for ALB
resource "aws_cloudwatch_metric_alarm" "alb_target_health" {
  count               = var.enable_monitoring ? 1 : 0
  alarm_name          = "${local.common_name}-alb-unhealthy-hosts"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = 2
  metric_name         = "UnHealthyHostCount"
  namespace           = "AWS/ApplicationELB"
  period              = 300
  statistic           = "Average"
  threshold           = 0
  alarm_description   = "Alert when ALB has unhealthy targets"
  treat_missing_data  = "notBreaching"

  dimensions = {
    TargetGroup  = aws_lb_target_group.backend.arn_suffix
    LoadBalancer = aws_lb.main.arn_suffix
  }
}

# EC2 IAM Role
resource "aws_iam_role" "ec2_role" {
  name = "${local.common_name}-ec2-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          Service = "ec2.amazonaws.com"
        }
      }
    ]
  })
}

resource "aws_iam_role_policy" "ec2_policy" {
  name = "${local.common_name}-ec2-policy"
  role = aws_iam_role.ec2_role.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "cloudwatch:PutMetricData",
          "ec2messages:AcknowledgeMessage",
          "ec2messages:DeleteMessage",
          "ec2messages:FailMessage",
          "ec2messages:GetEndpoint",
          "ec2messages:GetMessages",
          "ec2messages:SendReply",
          "ssm:UpdateInstanceInformation",
          "logs:CreateLogGroup",
          "logs:CreateLogStream",
          "logs:PutLogEvents",
          "logs:DescribeLogStreams"
        ]
        Resource = "*"
      },
      {
        Effect = "Allow"
        Action = [
          "s3:GetObject",
          "s3:PutObject"
        ]
        Resource = "${aws_s3_bucket.alb_logs.arn}/*"
      }
    ]
  })
}

resource "aws_iam_instance_profile" "ec2_profile" {
  name = "${local.common_name}-ec2-profile"
  role = aws_iam_role.ec2_role.name
}

# SSH Key Pair
resource "aws_key_pair" "deployer" {
  key_name = "${local.common_name}-deployer"
  public_key = file("${path.module}/../.ssh/id_rsa.pub")

  tags = {
    Name = "${local.common_name}-deployer"
  }
}

# Data source for Amazon Linux 2 AMI
data "aws_ami" "amazon_linux_2" {
  most_recent = true
  owners = ["amazon"]

  filter {
    name = "name"
    values = ["amzn2-ami-hvm-*-x86_64-gp2"]
  }

  filter {
    name = "virtualization-type"
    values = ["hvm"]
  }
}

# Outputs
output "alb_dns_name" {
  description = "DNS name of the ALB"
  value       = aws_lb.main.dns_name
}

output "asg_name" {
  description = "Name of the Auto Scaling Group"
  value       = aws_autoscaling_group.backend.name
}

output "mongodb_private_ip" {
  description = "Private IP of MongoDB instance"
  value       = !var.enable_documentdb ? aws_instance.mongodb[0].private_ip : null
}
