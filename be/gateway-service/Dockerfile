FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app

COPY . .


RUN mvn -B clean install -f common/pom.xml -DskipTests
RUN mvn -B clean install -f common-config/pom.xml -DskipTests

RUN mvn -B clean package -f gateway-service/pom.xml -DskipTests

FROM eclipse-temurin:21-jdk
WORKDIR /app


COPY --from=builder /app/gateway-service/target/gateway-service-*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]