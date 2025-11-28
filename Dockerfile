FROM gradle:8.11-jdk21

WORKDIR /app

COPY . .

RUN ./gradlew --no-daemon clean build

CMD ["java", "-jar", "build/libs/app-0.0.1-SNAPSHOT.jar"]