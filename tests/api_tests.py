import json
import pytest
from pytest_check import check
import requests
from .configuration import config
import re
import os
import uuid


TEST_DATA_BASE_PATH = os.path.join(os.path.dirname(__file__), './test_data/')


def _base_valid_uri() -> str:
    prNo = re.search("pr-[0-9]+", config.SERVICE_BASE_PATH)
    prString = f"-{prNo.group()}" if prNo is not None else ""

    return f"{config.BASE_URL}/summary-care-record/FHIR/R4{prString}"


def read_body_from_file(file_name):
    with open(os.path.join(TEST_DATA_BASE_PATH, file_name)) as json_file:
        return json.load(json_file)


def send_set_permission_request(headers, permission_code: str):
    headers["Content-Type"] = "application/fhir+json"
    patient_nhs = "9000000009" if "sandbox" in config.ENVIRONMENT else "9995000180"
    if ("Authorization" not in headers):
        headers["Authorization"] = "Bearer U7VUOM5e274qjOppmzqCRxRRZCG4k"

    body_from_file = read_body_from_file("set_permission.json")
    body_as_string = json.dumps(body_from_file) \
        .replace("{{PERMISSION_CODE}}", permission_code) \
        .replace("{{PATIENT_NHS_NUMBER}}", patient_nhs)

    response = requests.post(
        f"{_base_valid_uri()}/$setPermission",
        json=json.loads(body_as_string),
        headers=headers
    )

    assert response.status_code == 201, "POST $setPermission request failed"


@pytest.mark.smoketest
def test_set_permission_no(headers):
    send_set_permission_request(headers, "No")


@pytest.mark.smoketest
def test_update_bundle_without_permissions(headers):
    headers["Content-Type"] = "application/fhir+json"
    patient_nhs = "9995333333" if "sandbox" in config.ENVIRONMENT else "9995000180"
    body_from_file = read_body_from_file("post_bundle.json")
    body_as_string = json.dumps(body_from_file) \
        .replace("{{COMPOSITION_ID}}", str(uuid.uuid4())) \
        .replace("{{BUNDLE_IDENTIFIER_VALUE}}", str(uuid.uuid4())) \
        .replace("{{PATIENT_NHS_NUMBER}}", patient_nhs)

    response = requests.post(
        f"{_base_valid_uri()}/Bundle",
        json=json.loads(body_as_string),
        headers=headers
    )

    assert response.status_code == 403, "POST SCR failed"


@pytest.mark.smoketest
def test_set_permission_yes(headers):
    send_set_permission_request(headers, "Yes")


@pytest.mark.smoketest
def test_get_scr_id(headers):
    patient_nhs = "9000000009" if "sandbox" in config.ENVIRONMENT else "9995000180"
    response = requests.get(
        f"{_base_valid_uri()}/DocumentReference?patient=https://fhir.nhs.uk/Id/nhs-number|{patient_nhs}"
        + "&_sort=date&type=http://snomed.info/sct|196981000000101&_count=1",
        headers=headers
    )

    assert response.status_code == 200, "GET SCR ID request failed"
    response_body = json.loads(response.text)
    with check:
        assert response_body["resourceType"] == "Bundle"
    if (response_body["total"] > 0):
        with check:
            assert response_body["entry"][0]["resource"]["resourceType"] == "DocumentReference"
            assert response_body["entry"][0]["resource"]["content"][0]["attachment"]["url"] is not None


@pytest.mark.smoketest
def test_get_bundle(headers):
    patient_nhs = "9000000009" if "sandbox" in config.ENVIRONMENT else "9995000180"

    response = requests.get(
        f"{_base_valid_uri()}/Bundle?composition.identifier=29B2BAEB-E2E7-4B08-B30E-55C0F90CABDF"
        + f"&composition.subject:Patient.identifier=https://fhir.nhs.uk/Id/nhs-number|{patient_nhs}",
        headers=headers
    )

    assert response.status_code == 200, "GET SCR failed"
    response_body = json.loads(response.text)
    with check:
        assert response_body["resourceType"] == "Bundle"
    if (response_body["total"] > 0):
        with check:
            assert response_body["entry"][0]["resource"]["resourceType"] == "Composition"
            assert response_body["entry"][0]["resource"]["section"] is not None


@pytest.mark.smoketest
def test_audit_event(headers):
    headers["Content-Type"] = "application/fhir+json"
    patient_nhs = "9000000009" if "sandbox" in config.ENVIRONMENT else "9995000180"
    body_from_file = read_body_from_file("audit_event.json")
    body_as_string = json.dumps(body_from_file) \
        .replace("{{PATIENT_NHS_NUMBER}}", patient_nhs)

    response = requests.post(
        f"{_base_valid_uri()}/AuditEvent",
        json=json.loads(body_as_string),
        headers=headers
    )

    assert response.status_code == 201, "POST Audit Event failed"


@pytest.mark.smoketest
def test_auth_token(headers):
    headers["Authorization"] = "invalid_token"
    headers["Content-Type"] = "application/fhir+json"
    patient_nhs = "9000000009" if "sandbox" in config.ENVIRONMENT else "9995000180"
    body_from_file = read_body_from_file("audit_event.json")
    body_as_string = json.dumps(body_from_file) \
        .replace("{{PATIENT_NHS_NUMBER}}", patient_nhs)

    response = requests.post(
        f"{_base_valid_uri()}/AuditEvent",
        json=json.loads(body_as_string),
        headers=headers
    )
    expected_http_status = 201 if "sandbox" in config.ENVIRONMENT else 401
    assert response.status_code == expected_http_status, "auth token check failed"
