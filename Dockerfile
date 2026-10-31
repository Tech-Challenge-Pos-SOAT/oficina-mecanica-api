# ---- Build stage ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -B dependency:go-offline
COPY src ./src
RUN mvn -B clean package -DskipTests

# ---- Runtime stage ----
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Usuario sem privilegios: o container nao roda como root (exigencia comum de
# PodSecurity/Autopilot e boa pratica apontada pelo Trivy).
RUN addgroup -S -g 10001 app && adduser -S -u 10001 -G app -H -h /app app
COPY --from=build --chown=app:app /app/target/*.jar app.jar
USER app

# Dentro do container a JVM respeita o limite de memoria do Pod; 75% do limite
# vai para o heap (o padrao e' 25%, o que desperdicaria a memoria reservada).
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:+ExitOnOutOfMemoryError"

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
