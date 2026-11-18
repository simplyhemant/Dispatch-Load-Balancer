
# ==========================================
# Stage 1: Build the Spring Boot application
# ==========================================
FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copy Maven configuration first for dependency caching
COPY pom.xml .

# Download dependencies
RUN mvn dependency:go-offline -B

# Copy application source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# ==========================================
# Stage 2: Run the application
# ==========================================
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy the executable JAR
COPY --from=build /app/target/*.jar app.jar

# Render provides PORT dynamically
ENV PORT=8080

EXPOSE 8080

# Start Spring Boot
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
