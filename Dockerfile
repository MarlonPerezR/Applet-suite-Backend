# Etapa de compilación
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY . .
RUN ./mvnw clean package -DskipTests

# Etapa de ejecución
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Puerto que expone tu backend (Render usará este)
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
