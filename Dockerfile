FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app

# 1. Parent POM'u kopyala
COPY pom.xml .

# 2. Tüm modül dizinlerini kopyala
COPY rest/pom.xml ./rest/
COPY tcp/pom.xml ./tcp/
COPY coordinator/pom.xml ./coordinator/

# 3. Sadece ilgili modülün kaynak kodunu kopyala
COPY tcp/src ./tcp/src

# 4. Sadece "tcp" modülünü build et
RUN mvn clean package -pl tcp -am -DskipTests -q

FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app
COPY --from=builder /app/tcp/target/tcp-*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java","-jar","app.jar"]