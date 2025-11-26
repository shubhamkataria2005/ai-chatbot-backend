# Build stage
FROM maven:3.9.8-eclipse-temurin-17 AS build

# Install Python and ML libraries
RUN apt-get update && apt-get install -y \
    python3 \
    python3-pip \
    && pip3 install \
    numpy \
    pandas \
    scikit-learn \
    nltk \
    tensorflow-cpu

WORKDIR /app

# Copy pom.xml and download dependencies first (for caching)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre

# Install Python in runtime stage too
RUN apt-get update && apt-get install -y \
    python3 \
    python3-pip \
    && pip3 install \
    numpy \
    pandas \
    scikit-learn \
    nltk \
    tensorflow-cpu

WORKDIR /app

# Copy the built JAR file
COPY --from=build /app/target/ai-chatbot-backend-0.0.1-SNAPSHOT.jar app.jar

# Create directory for ML models and copy them
RUN mkdir -p src/main/resources/models
COPY src/main/resources/models ./src/main/resources/models/

# Expose port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]