FROM openjdk:17

WORKDIR /app
COPY backend .

# Устанавливаем Maven
RUN microdnf install maven

# Собираем проект
RUN mvn clean package -DskipTests

EXPOSE 8080
CMD ["java", "-jar", "app/target/*.jar"]
