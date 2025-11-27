FROM eclipse-temurin:17-jre
WORKDIR /app
COPY backend/app/target/*.jar app.jar
EXPOSE $PORT
CMD ["java", "-jar", "app.jar"]
