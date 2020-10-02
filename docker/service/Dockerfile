FROM gradle:jdk11 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle build --no-daemon -x bootJar

FROM adoptopenjdk/openjdk14:jre

EXPOSE 9000

RUN mkdir /app

COPY --from=build /home/gradle/src/build/libs/*.jar /app/integration-adaptor-scr.jar

ENTRYPOINT java -jar /app/integration-adaptor-scr.jar --port 9000
