# Build Stage: Compile Spring Boot Maven application
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copy pom.xml and source files
COPY pom.xml .
COPY src ./src

# Build production jar skipping tests
RUN mvn clean package -DskipTests

# Runtime Stage: Run light JDK/JRE container
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copy executable jar from build stage
COPY --from=build /app/target/backend-1.0-SNAPSHOT.jar app.jar

# Expose default Spring Boot port
EXPOSE 8083

# Launch Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]
