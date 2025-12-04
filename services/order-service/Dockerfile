# Build stage
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /build

# Copy entire project structure
COPY pom.xml .
COPY common-models ./common-models
COPY common-utils ./common-utils
COPY common-security ./common-security
COPY common-kafka ./common-kafka
COPY common-db ./common-db
COPY shared-test ./shared-test
COPY services ./services

# Build the specific service
RUN mvn -B clean package -pl services/order-service -am -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Create non-root user
RUN addgroup -g 1001 appuser && \
    adduser -u 1001 -G appuser -s /sbin/nologin -D appuser

# Install curl for healthcheck
RUN apk add --no-cache curl

# Copy the built JAR from builder
COPY --from=builder /build/services/order-service/target/order-service-*.jar app.jar

# Change ownership
RUN chown -R appuser:appuser /app

USER appuser

EXPOSE 8002

HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
    CMD curl -f http://localhost:8002/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
