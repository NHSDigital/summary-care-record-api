# reset all stubs
curl --location --request POST 'http://localhost:8081/__admin/mappings/reset'

# Spine POST
curl --location --request POST 'http://localhost:8081/__admin/mappings' \
--header 'Content-Type: application/json' \
--data-raw '{
  "request": {
    "method": "POST",
    "url": "/summarycarerecord"
  },
  "response": {
    "headers": {
      "Content-Location": "http://mock-spine-service:8080/content",
      "Retry-After": "100"
    },
    "status": 202
  },
  "scenarioName": "test",
  "requiredScenarioState": "Started"
}'

# Spine first GET - returning 202 - processing in progress
curl --location --request POST 'http://localhost:8081/__admin/mappings' \
--header 'Content-Type: application/json' \
--data-raw '{
  "request": {
    "method": "GET",
    "url": "/content"
  },
  "response": {
    "headers": {
      "Retry-After": "1000"
    },
    "status": 202
  },
  "scenarioName": "test",
  "requiredScenarioState": "Started",
  "newScenarioState": "DataReady"
}'

# Spine second GET - returning 200 - processing finished
curl --location --request POST 'http://localhost:8081/__admin/mappings' \
--header 'Content-Type: application/json' \
--data-raw '{
  "request": {
    "method": "GET",
    "url": "/content"
  },
  "response": {
    "status": 200,
    "body": "processing finished"
  },
  "scenarioName": "test",
  "requiredScenarioState": "DataReady",
  "newScenarioState": "Started"
}'

# ACS POST
curl --location --request POST 'http://localhost:8081/__admin/mappings' \
--header 'Content-Type: application/json' \
--data-raw '{
  "request": {
    "method": "POST",
    "url": "/acs"
  },
  "response": {
    "status": 200,
    "body": "set acs completed"
  }
}'