# ── Stage 1: Build ─────────────────────────────────────────────
FROM maven:3.9.6-eclipse-temurin-17 AS builder

WORKDIR /app

# Copy parent pom first
COPY pom.xml .

# Copy ALL modules — Maven needs them all even if only building one
COPY common-lib/pom.xml common-lib/
COPY user-service/pom.xml user-service/
COPY provider-service/pom.xml provider-service/
COPY menu-service/pom.xml menu-service/
COPY api-gateway/pom.xml api-gateway/pom.xml
COPY order-service/pom.xml order-service/pom.xml
COPY order-service/src order-service/src

# Now copy all source code
COPY common-lib/src common-lib/src
COPY user-service/src user-service/src
COPY provider-service/src provider-service/src
COPY menu-service/src menu-service/src
COPY api-gateway/src api-gateway/src

# Install parent and common-lib, then build menu-service
RUN mvn install -N -DskipTests
RUN mvn install -pl common-lib -DskipTests
RUN mvn package -pl menu-service -DskipTests

# ── Stage 2: Run ───────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY --from=builder /app/menu-service/target/*.jar app.jar

EXPOSE 8083

ENTRYPOINT ["java", "-jar", "app.jar"]