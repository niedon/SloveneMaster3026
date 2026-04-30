# Dockerfile Multi-stage - Construcción y Ejecución
# ETAPA 1: Construcción (Builder)
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder
WORKDIR /build

# Copiar solo el pom.xml primero para aprovechar la caché de Docker
COPY pom.xml .
# Descargar todas las dependencias sin compilar todavía
RUN mvn dependency:go-offline

# Ahora copiamos el código fuente (se ejecutará solo si el código cambia)
COPY src ./src
# Compilar el JAR
RUN mvn clean package -DskipTests -q

# ETAPA 2: Ejecución
FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

# Copiar el JAR desde la etapa 1
COPY --from=builder /build/target/slovenemaster3026-1.0-SNAPSHOT.jar /app/app.jar

# Crear directorios independientes para los volúmenes, con permisos no exclusivos
RUN mkdir -p /app-data/xml /app-data/db && chmod 777 -R /app-data

# Variables de entorno por defecto
ENV XML_PATH=/app-data/xml
ENV SPRING_PROFILES_ACTIVE=docker

EXPOSE 8080 5005

CMD ["java", "-Dspring.profiles.active=docker", "-jar", "/app/app.jar"]
