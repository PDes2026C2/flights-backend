FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder

WORKDIR /build

# Copiamos primero solo los archivos de configuración
COPY pom.xml .
COPY .mvn .mvn

RUN mvn dependency:go-offline

COPY src ./src

# Compilamos el proyecto sion tests
RUN mvn clean package -DskipTests


# ETAPA 2: Ejecución (Run)
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copiamos el .jar generado desde la etapa anterior (la llamamos "builder")
COPY --from=builder /build/target/flights-api.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]