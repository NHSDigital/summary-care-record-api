FROM gradle:7.2.0-jdk11 AS build
COPY --chown=gradle:gradle . /home/gradle/src/
WORKDIR /home/gradle/src
RUN gradle build --no-daemon --console plain bootJar

FROM adoptopenjdk/openjdk11

EXPOSE 9000

RUN mkdir /app

COPY --from=build /home/gradle/src/build/libs/integration-adaptor-scr.jar /app/integration-adaptor-scr.jar

ENTRYPOINT java -jar /app/integration-adaptor-scr.jar
