FROM eclipse-temurin:17-jdk

WORKDIR /app
COPY . .
RUN ./gradlew build

CMD ["java", "-jar", "build/libs/backendEscro-0.0.1-SNAPSHOT.jar"]