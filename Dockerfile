# Build stage
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app
COPY . .
RUN ./mvnw clean package -DskipTests

# Run stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Create non-root user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# Copy only the JAR file from build stage
COPY --from=build /app/target/*.jar app.jar

# Environment variable configuration
ARG OPENAI_API_KEY
ARG DB_URL
ENV OPENAI_API_KEY=${OPENAI_API_KEY}
ENV DB_URL=${DB_URL}

# Expose the default Spring Boot port
EXPOSE 8080

# Healthcheck to verify container is running properly
HEALTHCHECK --interval=30s --timeout=3s CMD wget -q --spider http://localhost:8080/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

# Docker network information in comments:
# This container should be started with:
# docker run --network dbs -e OPENAI_API_KEY=your_key -e DB_URL=jdbc_url [other-options] --port 3001:8080 image-name