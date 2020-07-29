FROM gradle:jdk11 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle build --no-daemon -x test -x integrationTest -x checkstyleIntegrationTest -x checkstyleMain -x checkstyleTest -x spotbugsIntegrationTest -x spotbugsMain -x spotbugsTest

FROM adoptopenjdk/openjdk11-openj9:jre

EXPOSE 8080

RUN mkdir /app

COPY --from=build /home/gradle/src/build/libs/*.jar /app/summary-care-record-api.jar

ENTRYPOINT ["java", "-XX:+UnlockExperimentalVMOptions", "-XX:+UseCGroupMemoryLimitForHeap", "-Djava.security.egd=file:/dev/./urandom","-jar","/app/summary-care-record-api.jar"]