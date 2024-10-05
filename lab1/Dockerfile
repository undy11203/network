FROM jelastic/maven:3.9.5-openjdk-21 AS builder

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean package

FROM openjdk:21-slim

  # Копируем собранный .jar файл из предыдущего образа
COPY --from=builder /app/target/lab1-1.0-SNAPSHOT.jar /app/app.jar

  # Запускаем приложение
CMD ["sh", "-c", "java -jar /app/app.jar $GROUP_IP"]