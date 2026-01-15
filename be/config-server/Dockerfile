FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app


COPY . .


RUN mvn -B clean install -f common/pom.xml -DskipTests
RUN mvn -B clean install -f common-config/pom.xml -DskipTests


RUN mvn -B clean package -f config-server/pom.xml -DskipTests


FROM eclipse-temurin:21-jdk
WORKDIR /app


COPY --from=builder /app/config-server/target/config-server-*.jar app.jar


COPY .env .
COPY config-repository/ config-repository/


ENTRYPOINT ["java", "-jar", "app.jar", \
            "--spring.config.import=optional:file:./.env", \
            "--spring.cloud.config.server.native.searchLocations=file:./config-repository"]