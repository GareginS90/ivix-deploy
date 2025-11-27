FROM maven:3.8.7-openjdk-17 AS build

WORKDIR /app
COPY backend .

# Сначала устанавливаем родительский POM
RUN mvn clean install -N -DskipTests

# Затем собираем весь проект
RUN mvn clean package -DskipTests

FROM openjdk:17.0.9-slim
WORKDIR /app

# Копируем собранный JAR из app модуля
COPY --from=build /app/app/target/*.jar app.jar

EXPOSE $PORT
CMD ["java", "-jar", "app.jar"]
