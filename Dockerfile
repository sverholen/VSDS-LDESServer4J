# syntax=docker/dockerfile:1

FROM alpine:latest AS bom
RUN apk update && apk add git maven
RUN git clone https://github.com/sverholen/VSDS-LDES.git vsds


FROM maven:3.8.5-openjdk-18 AS builder
COPY --from=bom /vsds/pom.xml .
RUN mvn clean install -DskipFormatCode=true
COPY . /ldes-server
WORKDIR /ldes-server
RUN mvn clean package -DskipFormatCode=true -DSkipTests=true

FROM openjdk:18-ea-alpine
#RUN apk --no-cache add ca-certificates
#RUN wget -q -O /etc/apk/keys/sgerrand.rsa.pub https://alpine-pkgs.sgerrand.com/sgerrand.rsa.pub
#RUN wget https://github.com/sgerrand/alpine-pkg-glibc/releases/download/2.29-r0/glibc-2.29-r0.apk
#RUN wget https://github.com/sgerrand/alpine-pkg-glibc/releases/download/2.29-r0/glibc-bin-2.29-r0.apk
#RUN apk add glibc-2.29-r0.apk glibc-bin-2.29-r0.apk
#RUN wget https://github.com/sgerrand/alpine-pkg-glibc/releases/download/2.29-r0/glibc-i18n-2.29-r0.apk
#RUN apk add glibc-bin-2.29-r0.apk glibc-i18n-2.29-r0.apk
#RUN /usr/glibc-compat/bin/localedef -i en_US -f UTF-8 en_US.UTF-8

COPY --from=builder /ldes-server/ldes-server-application/target/ldes-server-application.jar ./
COPY --from=builder /ldes-server/ldes-server-infra-mongo/target/ldes-server-infra-mongo-jar-with-dependencies.jar ./plugins/
CMD ["java", "-cp", "ldes-server-application.jar", "-Dloader.path=plugins/", "org.springframework.boot.loader.PropertiesLauncher"]
