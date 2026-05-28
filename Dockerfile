# ===========================================
# Stage 1: Build the application
# ===========================================
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /app

# Copy Maven wrapper and pom.xml first for dependency caching
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Download dependencies (cached layer)
RUN ./mvnw dependency:go-offline -B -q

# Copy source and build
COPY src src
RUN ./mvnw package -DskipTests -B -q

# ===========================================
# Stage 2: Run the application
# ===========================================
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Create non-root user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy jar from builder
COPY --from=builder /app/target/*.jar app.jar

# Create uploads directory
RUN mkdir -p /app/uploads/documents && chown -R appuser:appgroup /app

USER appuser

EXPOSE 8080

ENTRYPOINT java -jar /app/app.jar --spring.profiles.active=${SPRING_PROFILES_ACTIVE:-h2}
