FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app


COPY . .

RUN mvn -B clean install -f common/pom.xml -DskipTests
RUN mvn -B clean install -f common-config/pom.xml -DskipTests

RUN mvn -B clean package -f test-order-service/pom.xml -DskipTests


FROM eclipse-temurin:21-jdk
WORKDIR /app


COPY --from=builder /app/test-order-service/target/test-order-service-*.jar app.jar

ENV TZ=Asia/Ho_Chi_Minh

RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

ENTRYPOINT ["java", "-jar", "app.jar"]