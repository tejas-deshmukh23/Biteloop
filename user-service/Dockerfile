# ── Stage 1: Build ────────────────────────────────────────
# Use Maven + JDK 17 to compile and package the jar
FROM maven:3.9.5-eclipse-temurin-17 AS build

WORKDIR /app

# Copy parent pom first
COPY pom.xml .

# Copy common-lib (user-service depends on it)
COPY common-lib/pom.xml common-lib/pom.xml
COPY common-lib/src common-lib/src

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
 
# Copy user-service
COPY user-service/pom.xml user-service/pom.xml
COPY user-service/src user-service/src

# Build — skip tests in Docker build (run tests separately in CI)

#-pl stands for: --projects
#It tells Maven: “Build only this specific module/project.”
#Without -pl, Maven would build: everything like common-lib, user-service, order-service, etc
#-am stands for: --also-make
#Meaning: Also build dependencies required by this module.
#-DskipTests : Skip test execution. Makes Docker build faster.
#RUN mvn clean install -pl common-lib -am -DskipTests
# -N = this pom only, no child modules
RUN mvn install -N -DskipTests          
# Step 1: Install common-lib into local Maven repo
RUN mvn install -pl common-lib -DskipTests
RUN mvn clean package -pl user-service -DskipTests

#Difference between install and package that has used above =>
#package =>	creates jar
#install =>	creates jar + stores in local Maven repo
#Why You Need install for common-lib ?
# => Because user-service depends on common-lib.
#So first:
#common-lib → installed into local Maven repo
#Then:
#user-service → can import/use common-lib

# ── Stage 2: Run ──────────────────────────────────────────
# Use only JRE 17 — smaller image, no compiler needed at runtime
FROM eclipse-temurin:17-jre-alpine
 
WORKDIR /app
 
# Copy only the final jar from build stage
COPY --from=build /app/user-service/target/user-service-*.jar app.jar
 
# Port user-service runs on
EXPOSE 8081
 
# Run the jar
ENTRYPOINT ["java", "-jar", "app.jar"]