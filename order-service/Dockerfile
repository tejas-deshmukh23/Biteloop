# ── Stage 1: Build ─────────────────────────────────────────────
FROM maven:3.9.6-eclipse-temurin-17 AS builder

WORKDIR /app

# Copy parent pom
COPY pom.xml .

# Copy ALL module poms
COPY common-lib/pom.xml common-lib/
COPY user-service/pom.xml user-service/
COPY provider-service/pom.xml provider-service/
COPY menu-service/pom.xml menu-service/
COPY api-gateway/pom.xml api-gateway/
COPY order-service/pom.xml order-service/

# Copy ALL source
COPY common-lib/src common-lib/src
COPY user-service/src user-service/src
COPY provider-service/src provider-service/src
COPY menu-service/src menu-service/src
COPY api-gateway/src api-gateway/src
COPY order-service/src order-service/src

# Build in correct order
RUN mvn install -N -DskipTests
RUN mvn install -pl common-lib -DskipTests
RUN mvn package -pl order-service -DskipTests

# ── Stage 2: Run ───────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY --from=builder /app/order-service/target/*.jar app.jar

EXPOSE 8084

ENTRYPOINT ["java", "-jar", "app.jar"]