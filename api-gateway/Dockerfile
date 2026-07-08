# ── Stage 1: Build ─────────────────────────────────────────────
FROM maven:3.9.6-eclipse-temurin-17 AS builder

WORKDIR /app

# Copy parent pom
COPY pom.xml .

# Copy ALL module poms — Maven needs the full reactor
COPY common-lib/pom.xml common-lib/
COPY user-service/pom.xml user-service/
COPY provider-service/pom.xml provider-service/
COPY menu-service/pom.xml menu-service/
COPY api-gateway/pom.xml api-gateway/
COPY payment-service/pom.xml payment-service/pom.xml

# Copy ALL source code
COPY common-lib/src common-lib/src
COPY user-service/src user-service/src
COPY provider-service/src provider-service/src
COPY menu-service/src menu-service/src
COPY api-gateway/src api-gateway/src
COPY payment-service/src payment-service/src

COPY order-service/pom.xml order-service/pom.xml
COPY order-service/src order-service/src

COPY notification-service/pom.xml notification-service/pom.xml
COPY notification-service/src notification-service/src

# Install parent pom first
RUN mvn install -N -DskipTests

# Install common-lib into local .m2
RUN mvn install -pl common-lib -DskipTests

# Build api-gateway
RUN mvn package -pl api-gateway -DskipTests

# ── Stage 2: Run ───────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY --from=builder /app/api-gateway/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]