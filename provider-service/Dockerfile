FROM maven:3.9.5-eclipse-temurin-17 AS build

WORKDIR /app

COPY pom.xml .

COPY common-lib/pom.xml common-lib/pom.xml
COPY common-lib/src common-lib/src

# user-service must be copied because parent pom.xml lists it as module
COPY user-service/pom.xml user-service/pom.xml
COPY user-service/src user-service/src
COPY api-gateway/pom.xml api-gateway/pom.xml
COPY api-gateway/src api-gateway/src
COPY menu-service/pom.xml menu-service/pom.xml
COPY menu-service/src menu-service/src

COPY provider-service/pom.xml provider-service/pom.xml
COPY provider-service/src provider-service/src

RUN mvn install -N -DskipTests
RUN mvn install -pl common-lib -DskipTests
RUN mvn clean package -pl provider-service -DskipTests

FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY --from=build /app/provider-service/target/provider-service-*.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]