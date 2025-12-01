# Multi-stage build for ShopSphere services
# This Dockerfile builds all ShopSphere microservices

# Stage 1: Build stage
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /build

# Copy entire project
COPY . .

# Build all modules with Maven
RUN mvn clean package -DskipTests -B

# Stage 2: Base runtime image for all services
FROM eclipse-temurin:17-jre-alpine

# Create non-root user for security
RUN addgroup -g 1001 appuser && \
    adduser -u 1001 -G appuser -s /sbin/nologin -D appuser

# Install common packages
RUN apk add --no-cache curl bash

# Create app directory
WORKDIR /app

# Health check script
COPY --chown=appuser:appuser health-check.sh /app/
RUN chmod +x /app/health-check.sh

# Create logs directory
RUN mkdir -p /app/logs && chown -R appuser:appuser /app/logs

# Switch to non-root user
USER appuser

# Default entrypoint (can be overridden per service)
ENTRYPOINT ["java", "-jar"]

# Label for metadata
LABEL maintainer="ShopSphere Team"
LABEL version="1.0.0"
LABEL description="ShopSphere E-Commerce Microservices Platform"
