FROM openjdk:17.0.9-slim

WORKDIR /app
COPY backend .

# Устанавливаем Maven вручную
RUN apt-get update && apt-get install -y maven

# Собираем проект
RUN mvn clean package -DskipTests

EXPOSE $PORT
CMD ["java", "-jar", "app/target/*.jar"]
