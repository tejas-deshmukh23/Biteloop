FROM maven:3.9.5-eclipse-temurin-17 AS build

WORKDIR /app

# Copy parent pom first
COPY pom.xml .

# Copy all module poms AND src — Maven needs src when using -am flag
COPY common-lib/pom.xml common-lib/pom.xml
COPY common-lib/src common-lib/src

COPY user-service/pom.xml user-service/pom.xml
COPY user-service/src user-service/src

COPY provider-service/pom.xml provider-service/pom.xml
COPY provider-service/src provider-service/src

COPY menu-service/pom.xml menu-service/pom.xml
COPY menu-service/src menu-service/src

COPY api-gateway/pom.xml api-gateway/pom.xml
COPY api-gateway/src api-gateway/src

COPY order-service/pom.xml order-service/pom.xml
COPY order-service/src order-service/src

COPY payment-service/pom.xml payment-service/pom.xml
COPY payment-service/src payment-service/src

COPY notification-service/pom.xml notification-service/pom.xml
COPY notification-service/src notification-service/src

# Install parent pom without building modules
RUN mvn install -N -DskipTests

# Install common-lib into local Maven repo first
RUN mvn install -pl common-lib -DskipTests

# Build notification-service
RUN mvn clean package -pl notification-service -DskipTests

# ── Runtime image ──────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY --from=build /app/notification-service/target/notification-service-*.jar app.jar

EXPOSE 8088

ENTRYPOINT ["java", "-jar", "app.jar"]