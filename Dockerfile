FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn -pl back-end package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/back-end/target/quarkus-app/ /app/
CMD ["java", "-jar", "quarkus-run.jar"]