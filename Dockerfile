FROM maven:3.9.9-eclipse-temurin-17@sha256:f58d59b6273e785ac0a4477f6e9b5ba1d7731c75b906c0f7b34076f1851318cc AS build
WORKDIR /build
COPY pom.xml .
COPY src/main src/main
RUN mvn -B -DskipTests package
FROM eclipse-temurin:17-jre-jammy@sha256:aa6381384f8db99762fa7d0d3c2b5de58874e5f12c96df4c13a1455376ae0ed1
WORKDIR /app
COPY --from=build /build/target/taller7-java-api-1.0.0.jar app.jar
USER 10001:10001
EXPOSE 8085
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

