import http
import json
import pytest
import jwt
from pytest_check import check
import requests
from .configuration import config
import re
import os
import uuid
import time

# This file is executed by make test in the root directory
# This is the smoketest file.

TEST_DATA_BASE_PATH = os.path.join(os.path.dirname(__file__), './test_data/')

# Generated a new token, based on a jwt key, that is generated using client credentials, as well as a private key
# stored in the build environment.
def new_token():
    claims = {
        "sub": "yLnp8eYDuujs52lkGWHBWmn6hyP5jDDc",
        "iss": "yLnp8eYDuujs52lkGWHBWmn6hyP5jDDc",
        "jti": str(uuid.uuid4()),
        "aud": config.TOKEN_URL,
        "exp": int(time.time()) + 300,
    }

    headers = {"kid": "test-2"}

    encoded_jwt = jwt.encode(
        claims, config.PRIVATE_KEY, algorithm="RS512", headers=headers
    )

    response = requests.post(
        config.TOKEN_URL,
        data={
            "grant_type": "client_credentials",
            "client_assertion_type": "urn:ietf:params:oauth:client-assertion-type:jwt-bearer",
            "client_assertion": encoded_jwt,
        },
    )

    response_json = response.json()

    return "Bearer " + response_json["access_token"]


# Adds a new, second generation token to the headers, replacing any older, existing ones.
def update_headers_to_second_gen(headers):
    headers['Authorization'] = new_token()
    return headers


# Builds the base uri that the requests will be sent to
def _base_valid_uri() -> str:
    pr_number = re.search("pr-[0-9]+", config.SERVICE_BASE_PATH)
    pr_string = f"-{pr_number.group()}" if pr_number is not None else ""

    return f"{config.BASE_URL}/summary-care-record/FHIR/R4{pr_string}"


# Reads the contents of test file, and returns the file contents encoded as a JSON string.
def read_body_from_file(file_name):
    with open(os.path.join(TEST_DATA_BASE_PATH, file_name)) as json_file:
        return json.load(json_file)


# Sends a request for setting permissions, based on the provided permission_code, which can be "yes" or "no", depending
# on the text scenario.
def send_set_permission_request(headers, permission_code: str):
    headers["Content-Type"] = "application/fhir+json"
    patient_nhs = "9000000009" if "sandbox" in config.ENVIRONMENT else "9995000180"

    if "Authorization" not in headers:
        headers["Authorization"] = "Bearer U7VUOM5e274qjOppmzqCRxRRZCG4k"

    body_from_file = read_body_from_file("set_permission.json")
    body_as_string = json.dumps(body_from_file) \
        .replace("{{PERMISSION_CODE}}", permission_code) \
        .replace("{{PATIENT_NHS_NUMBER}}", patient_nhs)

    headers = update_headers_to_second_gen(headers)

    response = requests.post(
        f"{_base_valid_uri()}/$setPermission",
        json=json.loads(body_as_string),
        headers=headers
    )

    return response


# Tests the healthcheck endpoint with a GET request. The response should be 200 while in sandbox environment,
# or 401 in internal-dev.
@pytest.mark.smoketest
def test_healthcheck(headers):
    headers = update_headers_to_second_gen(headers)

    response = requests.get(
        f"{_base_valid_uri()}/healthcheck",
        headers=headers
    )

    expected_http_status = http.HTTPStatus.OK if "sandbox" in config.ENVIRONMENT else http.HTTPStatus.UNAUTHORIZED
    assert response.status_code == expected_http_status, "GET healthcheck request failed"


# Tests the set permission endpoint with a POST request, and NO permissions. The response should be 201 while in the
# sandbox environment, otherwise a 401 in internal-dev.
@pytest.mark.smoketest
def test_set_permission_no(headers):
    headers = update_headers_to_second_gen(headers)

    # Send the actual request, and make sure it has NO permissions.
    response = send_set_permission_request(headers, "No")

    expected_http_status = http.HTTPStatus.CREATED if "sandbox" in config.ENVIRONMENT else http.HTTPStatus.UNAUTHORIZED
    assert response.status_code == expected_http_status, "POST $setPermission request failed"


# Tests the set bundle endpoint with a POST request and NO permissions. The response should be 403 while in the
# sandbox environment, otherwise a 401 in internal-dev.
@pytest.mark.smoketest
def test_update_bundle_without_permissions(headers):
    # we don't want permissions for this test
    send_set_permission_request(headers, "No")

    headers["Content-Type"] = "application/fhir+json"
    patient_nhs = "9995333333" if "sandbox" in config.ENVIRONMENT else "9995000180"
    body_from_file = read_body_from_file("post_bundle.json")
    body_as_string = json.dumps(body_from_file) \
        .replace("{{COMPOSITION_ID}}", str(uuid.uuid4())) \
        .replace("{{BUNDLE_IDENTIFIER_VALUE}}", str(uuid.uuid4())) \
        .replace("{{PATIENT_NHS_NUMBER}}", patient_nhs)

    headers.pop("Authorization")

    response = requests.post(
        f"{_base_valid_uri()}/Bundle",
        json=json.loads(body_as_string),
        headers=headers
    )

    expected_http_status = http.HTTPStatus.FORBIDDEN if "sandbox" in config.ENVIRONMENT else http.HTTPStatus.UNAUTHORIZED
    assert response.status_code == expected_http_status, "POST SCR failed"


# Tests the set permission endpoint with a POST request, WITH permissions. The response should be 201 while in the
# sandbox environment, otherwise a 401 in internal-dev.
@pytest.mark.smoketest
def test_set_permission_yes(headers):
    response = send_set_permission_request(headers, "Yes")

    expected_http_status = http.HTTPStatus.CREATED if "sandbox" in config.ENVIRONMENT else http.HTTPStatus.UNAUTHORIZED
    assert response.status_code == expected_http_status, "POST $setPermission request failed"


# Tests the set DocumentReference endpoint with a GET request. The response should be 200, and the correct elements
# should be present in the response.
@pytest.mark.smoketest
def test_get_scr_id(headers):
    patient_nhs = "9000000009" if "sandbox" in config.ENVIRONMENT else "9995000180"
    headers = update_headers_to_second_gen(headers)

    response = requests.get(
        f"{_base_valid_uri()}/DocumentReference?patient=https://fhir.nhs.uk/Id/nhs-number|{patient_nhs}"
        + "&_sort=date&type=http://snomed.info/sct|196981000000101&_count=1",
        headers=headers
    )

    assert response.status_code == http.HTTPStatus.OK, "GET SCR ID request failed"

    response_body = json.loads(response.text)

    with check:
        assert response_body["resourceType"] == "Bundle"

    if response_body["total"] > 0:
        with check:
            assert response_body["entry"][0]["resource"]["resourceType"] == "DocumentReference"
            assert response_body["entry"][0]["resource"]["content"][0]["attachment"]["url"] is not None


# Tests the set bundle endpoint with a GET request. The response should be 200, and the correct elements should be
# present in the response.
@pytest.mark.smoketest
def test_get_bundle(headers):
    patient_nhs = "9000000009" if "sandbox" in config.ENVIRONMENT else "9995000180"

    headers = update_headers_to_second_gen(headers)

    response = requests.get(
        f"{_base_valid_uri()}/Bundle?composition.identifier=29B2BAEB-E2E7-4B08-B30E-55C0F90CABDF"
        + f"&composition.subject:Patient.identifier=https://fhir.nhs.uk/Id/nhs-number|{patient_nhs}",
        headers=headers
    )

    assert response.status_code == http.HTTPStatus.OK, "GET SCR failed"
    response_body = json.loads(response.text)

    with check:
        assert response_body["resourceType"] == "Bundle"

    if response_body["total"] > 0:
        with check:
            assert response_body["entry"][0]["resource"]["resourceType"] == "Composition"
            assert response_body["entry"][0]["resource"]["section"] is not None


# Sends a POST request to the AuditEvent endpoint, and returns the response.
def send_audit_event(headers):
    headers["Content-Type"] = "application/fhir+json"
    patient_nhs = "9000000009" if "sandbox" in config.ENVIRONMENT else "9995000180"
    body_from_file = read_body_from_file("audit_event.json")
    body_as_string = json.dumps(body_from_file) \
        .replace("{{PATIENT_NHS_NUMBER}}", patient_nhs)

    headers = update_headers_to_second_gen(headers)

    response = requests.post(
        f"{_base_valid_uri()}/AuditEvent",
        json=json.loads(body_as_string),
        headers=headers
    )

    return response


# Tests the AuditEvent endpoint with a POST request. The result should be 201 when running in sandbox, and 401 in
# internal-dev.
@pytest.mark.smoketest
def test_audit_event(headers):
    response = send_audit_event(headers)

    expected_http_status = http.HTTPStatus.CREATED if "sandbox" in config.ENVIRONMENT else http.HTTPStatus.UNAUTHORIZED
    assert response.status_code == expected_http_status, "POST Audit Event failed"


# Tests the AuditEvent endpoint with a POST request, and an invalid token. The result should be 201 when running in
# sandbox, and 401 in internal-dev.
@pytest.mark.smoketest
def test_auth_token(headers):
    headers["Authorization"] = "invalid_token"

    response = send_audit_event(headers)

    expected_http_status = http.HTTPStatus.CREATED if "sandbox" in config.ENVIRONMENT else http.HTTPStatus.UNAUTHORIZED
    assert response.status_code == expected_http_status, "auth token check failed"
