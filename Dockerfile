FROM openjdk:17-jdk-alpine as build
ADD . /home/app/niem-api
COPY ./build/libs/api-2.0.jar /home/app/niem-api/target/niem-api-2.0.jar
RUN mkdir -p /opt/niem-api/db-backups
ENTRYPOINT ["java","-jar","/home/app/niem-api/target/niem-api-2.0.jar"]
