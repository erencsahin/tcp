FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /build

# monorepo root'u komple kopyala
COPY . .

# sadece tcp modülünü derle
RUN mvn clean package -pl tcp -am -DskipTests -q

FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app

COPY --from=builder /build/tcp/target/tcp-*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java","-jar","app.jar"]