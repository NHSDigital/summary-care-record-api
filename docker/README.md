# integration-adaptor-scr
National Integration Adaptors - Summary Care Record

## Requirements:
1. JDK 14

## How to run unit tests:
* Navigate to `service`
* Run: `./gradlew test`

## How to run integration tests:
* Navigate to `service`
* Run: `./gradlew integrationTest`

## How to use Spine Mock Service
By default Spine Mock Service will run on `http://localhost:8081` and provide 2 configuration endpoints:

---
* POST `/setup` - configures mock endpoints

Example:
```
POST /setup HTTP/1.1
Content-Type: application/json
{
    "url": "/sample",
    "httpStatusCode": 200,
    "httpMethod": "GET",
    "responseContent": "My sample message."
}
```
will configure new `GET /sample` endpoint returning `200` and `My sample message.`

---
* POST `/setup/reset` - removes all custom endpoints created by /setup.
---