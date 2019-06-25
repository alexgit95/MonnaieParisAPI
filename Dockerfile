FROM maven:3.6.1-jdk-11-slim AS builder

COPY . /src
WORKDIR /src
RUN mvn package
RUN ls /src/target

FROM adoptopenjdk/openjdk11:x86_64-debian-jre-11.0.3_7
COPY --from=builder /src/target /apps
COPY src/main/java/resources/index.html /apps/monnaie.html
COPY props.properties /apps/props.properties
WORKDIR /apps
EXPOSE 8181
ENTRYPOINT ["java", "-jar", "MonnaieParisAPI-0.0.1-SNAPSHOT-fat.jar"]
