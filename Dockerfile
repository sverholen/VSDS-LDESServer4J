# syntax=docker/dockerfile:1

<<<<<<< HEAD
FROM alpine:latest AS bom
RUN apk update && apk add git maven
RUN git clone https://github.com/Informatievlaanderen/VSDS-LDES.git vsds
WORKDIR /vsds
RUN mvn clean install -DskipFormatCode=true


#FROM maven:3.8.5-openjdk-18 AS builder
COPY . /ldes-server
=======
FROM maven:3.8.5-openjdk-18 AS builder
RUN git clone https://github.com/Informatievlaanderen/VSDS-LDES.git # Temporary build VSDS-LDES until there's an artifactory
WORKDIR /VSDS-LDES
RUN git checkout 69356992e012803b5fa4fb13a7bcfdf110ffcf55 # Use version 0.0.1-SNAPSHOT
RUN mvn clean install

RUN mkdir /ldes-server
>>>>>>> 3d7c4817c87cd75c2b319a06c5e6b223c4a1ef0a
WORKDIR /ldes-server
COPY . /ldes-server
RUN mvn clean install -DskipFormatCode=true

FROM openjdk:18-ea-alpine
COPY --from=builder /ldes-server/ldes-server-application/target/ldes-server-application.jar ./
COPY --from=builder /ldes-server/ldes-server-infra-mongo/target/ldes-server-infra-mongo-jar-with-dependencies.jar ./plugins/
COPY --from=builder /ldes-server/ldes-server-port-publication-rest/target/ldes-server-port-publication-rest-jar-with-dependencies.jar ./plugins/
CMD ["java", "-cp", "ldes-server-application.jar", "-Dloader.path=plugins/", "org.springframework.boot.loader.PropertiesLauncher"]
