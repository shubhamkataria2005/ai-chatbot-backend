# Build stage
FROM maven:3.9.8-eclipse-temurin-17 AS build

# Install Python and create virtual environment for ML libraries
RUN apt-get update && apt-get install -y \
    python3 \
    python3-venv \
    python3-pip \
    && python3 -m venv /opt/venv

# Install ML libraries in virtual environment
RUN /opt/venv/bin/pip install --upgrade pip && \
    /opt/venv/bin/pip install \
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

# Install Python and create virtual environment in runtime stage
RUN apt-get update && apt-get install -y \
    python3 \
    python3-venv \
    && python3 -m venv /opt/venv

# Copy the virtual environment from build stage
COPY --from=build /opt/venv /opt/venv

WORKDIR /app

# Copy the built JAR file
COPY --from=build /app/target/ai-chatbot-backend-0.0.1-SNAPSHOT.jar app.jar

# Create directory for ML models and copy them
RUN mkdir -p src/main/resources/models
COPY src/main/resources/models ./src/main/resources/models/

# Set Python path to use our virtual environment
ENV PATH="/opt/venv/bin:$PATH"

# Expose port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]