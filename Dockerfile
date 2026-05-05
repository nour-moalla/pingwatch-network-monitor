# ============================================
# STAGE 1: BUILD
# Compiles the Java code
# This stage does NOT appear in the final image
# ============================================
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /app

# Copy pom.xml first for dependency caching
# If pom.xml doesn't change, Maven reuses cached dependencies
# This makes subsequent builds much faster
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Now copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests

# ============================================
# STAGE 2: RUNTIME
# Lightweight final image — only what's needed to RUN
# ============================================
FROM eclipse-temurin:17-jre-alpine

# Why alpine? Minimal Linux = smaller attack surface + smaller size
# Regular JRE: ~300MB | Alpine JRE: ~80MB

WORKDIR /app

# Security: NEVER run as root in production
# Create a dedicated user for the application
RUN addgroup -S pingwatch \
    && adduser -S pingwatch -G pingwatch

# Copy ONLY the compiled JAR from builder stage
# No source code, no Maven, no build tools in final image
COPY --from=builder /app/target/*.jar app.jar

# Give ownership to our non-root user
RUN chown pingwatch:pingwatch app.jar

# Switch to non-root user
USER pingwatch

# Document the port (doesn't publish — just documentation)
EXPOSE 8080

# Health check — Docker monitors if app is alive
# If this fails 3 times → container marked as unhealthy
HEALTHCHECK --interval=30s --timeout=10s --start-period=90s --retries=3 \
    CMD wget -q --spider http://localhost:8080/api/health || exit 1

# Start the application
ENTRYPOINT ["java", "-jar", "app.jar"]