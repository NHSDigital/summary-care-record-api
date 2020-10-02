FROM gradle:jdk14 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle build --no-daemon --console plain -x bootJar
RUN ls -laR /home/gradle/src/build/libs

FROM adoptopenjdk/openjdk14

EXPOSE 9000

RUN mkdir /app

COPY --from=build /home/gradle/src/build/libs/*.jar /app/integration-adaptor-scr.jar

ENTRYPOINT java -jar /app/integration-adaptor-scr.jar --port 9000
