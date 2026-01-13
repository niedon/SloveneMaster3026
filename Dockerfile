# Dockerfile Simplificado - Una sola etapa
FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

# Instalar Maven
RUN apk add --no-cache maven

# Copiar archivos necesarios para la compilación
COPY pom.xml .
COPY src ./src

# Compilar el proyecto
RUN mvn clean package -DskipTests -q

# Verificar que el JAR existe
RUN echo "=== JAR creado ===" && \
    ls -la /app/target/*.jar && \
    echo "=== Contenido app.jar ===" && \
    jar tf /app/target/slovenemaster3026-1.0-SNAPSHOT.jar | head -20

# Copiar el JAR a la raíz (lo hacemos aquí mismo, no desde builder)
RUN cp /app/target/slovenemaster3026-1.0-SNAPSHOT.jar /app/app.jar && \
    ls -la /app/app.jar

# Copiar los recursos estáticos (JSP, CSS, JS, imágenes)
COPY src/main/webapp/WEB-INF /app/WEB-INF

# Crear directorios para datos con permisos de escritura
RUN mkdir -p /data/xml && chmod 777 /data /data/xml

# Variables de entorno
ENV XML_PATH=/data/xml
ENV SPRING_PROFILES_ACTIVE=docker

# Exponer puerto
EXPOSE 8080

# Ejecutar - Mostrar que JSP existen antes de ejecutar
CMD ["sh", "-c", "echo '=== Verificando JSP antes de ejecutar ===' && find /app/WEB-INF -name '*.jsp' && echo '=== Iniciando aplicación ===' && java -Dspring.profiles.active=docker -jar /app/app.jar"]
