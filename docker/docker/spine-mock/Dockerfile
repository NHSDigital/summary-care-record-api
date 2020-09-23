FROM rodolpheche/wiremock

COPY stubs /home/wiremock

EXPOSE 8081

CMD ["java", "-cp", "/var/wiremock/lib/*:/var/wiremock/extensions/*", "com.github.tomakehurst.wiremock.standalone.WireMockServerRunner", "--port", "8081"]