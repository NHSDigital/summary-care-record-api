# integration-adaptor-scr
National Integration Adaptors - Summary Care Record

## Requirements:
1. JDK 14

## Configuration

The adaptor reads its configuration from environment variables. The following sections describe the environment variables
 used to configure the adaptor. 
 
Variables without a default value and not marked optional, *MUST* be defined for the adaptor to run.

### General Configuration Options

| Environment Variable               | Default                   | Description 
| -----------------------------------|---------------------------|-------------
| SCR_SERVER_PORT                    | 8080                      | The port on which the SCR API will run
| SCR_LOGGING_LEVEL                  | INFO                      | Application logging level. One of: DEBUG, INFO, WARN, ERROR. The level DEBUG **MUST NOT** be used when handling live patient data.
| SCR_PARTY_ID_TO                    |                           | Spine party id key
| SCR_SPINE_URL                      |                           | Spine SCR URL (eg. INT https://msg.intspineservices.nhs.uk)
| SCR_SPINE_ENDPOINT_CERT            |                           | Spine client PEM certificate used for mutual TLS
| SCR_SPINE_ENDPOINT_KEY             |                           | Key for the client PEM certificate
| SCR_SPINE_ENDPOINT_CACERT          |                           | CA cert PEM used for spine certificate validation

## How to run service:
* Navigate to `docker/docker/service`
* Add environment tag `export TAG=latest`
* Run script: `build-image.sh` (excute privileges might be required `chmod +x build-image.sh`)
* Navigate to `docker`
* Run script: `start-local-environment.sh`

If gradle-wrapper.jar doesn't exist navigate to docker/service in terminal and run:
* If gradle isn't installed `brew install gradle`
* Update gradle `gradle wrapper`

## How to run unit tests:
* Navigate to `service`
* Run: `./gradlew test`

## How to run integration tests:
* Navigate to `service`
* Run: `./gradlew integrationTest`

## How to run all checks:
* Navigate to `service`
* Run: `./gradlew check`

## How to use Spine Mock Service
Spine Mock Service is built using WireMock `http://wiremock.org/`
All API operations defined in `http://wiremock.org/docs/api/` are available to use
By default Spine Mock Service will run on `http://mock-spine-service:8081` (add `127.0.0.1 mock-spine-service` to your local `/etc/hosts` fille) and have predefined stubs in place 

For convenience, predefined Spine stubs are available. Check `./docker/docker/spine-mock/stubs/mappings/` or `http://mock-spine-service:8081/__admin`
