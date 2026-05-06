# ================================
# STAGE 1: Build
# ================================
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Copia primero el pom para aprovechar cache de capas Docker
COPY pom.xml .
COPY .mvn/ .mvn/
COPY mvnw .

# Descarga dependencias (se cachea si el pom no cambia)
RUN ./mvnw dependency:go-offline -B

# Copia el código fuente y compila
COPY src/ src/
RUN ./mvnw package -DskipTests -B

# ================================
# STAGE 2: Runtime
# ================================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Usuario no-root por seguridad
RUN addgroup -S spring && adduser -S spring -G spring
USER spring

# Copia solo el JAR generado en el stage anterior
COPY --from=builder /app/target/ecom-application-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]