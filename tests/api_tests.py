import json
import pytest
from pytest_check import check
import requests
from .configuration import config
import re


def _base_valid_uri() -> str:
    prNo = re.search("pr-[0-9]+", config.SERVICE_BASE_PATH)
    prString = f"-{prNo.group()}" if prNo is not None else ""

    return f"{config.BASE_URL}/summary-care-record/FHIR/R4{prString}"


def read_body_from_file(file_name):
    file = open(f"resources/{file_name}", "r")
    body = json.load(file)
    file.close()

    return body

@pytest.mark.smoketest
def test_set_permission(headers):
    headers["Content-Type"] = "application/fhir+json"
    response = requests.post(
        f"{_base_valid_uri()}/$setPermission",
        data=read_body_from_file("set_permission.json"),
        headers=headers
    )

    assert response.status_code == 201, "POST $setPermission request failed"


@pytest.mark.smoketest
def test_get_scr_id(headers):
    response = requests.get(
        f"{_base_valid_uri()}/DocumentReference?patient=https://fhir.nhs.uk/Id/nhs-number|9995000180"
        + "&_sort=date&type=http://snomed.info/sct|196981000000101&_count=1",
        headers=headers
    )

    assert response.status_code == 200, "GET SCR ID request failed"
    response_body = json.loads(response.text)
    with check:
        assert response_body["resourceType"] == "Bundle"
        assert response_body["entry"][0]["resource"]["resourceType"] == "DocumentReference"
        assert response_body["entry"][0]["resource"]["content"]["attachment"]["url"] is not None


@pytest.mark.smoketest
def test_get_bundle(headers):
    response = requests.get(
        f"{_base_valid_uri()}/Bundle?composition.identifier=29B2BAEB-E2E7-4B08-B30E-55C0F90CABDF"
        + "$composition.subject:Patient.identifier=https://fhir.nhs.uk/Id/nhs-number|9995000180",
        headers=headers
    )

    assert response.status_code == 200, "GET SCR failed"
    response_body = json.loads(response.text)
    with check:
        assert response_body["resourceType"] == "Bundle"
        assert response_body["entry"][0]["resource"]["resourceType"] == "Composition"
        assert response_body["entry"][0]["resource"]["section"] is not None


@pytest.mark.smoketest
def test_update_bundle(headers):
    headers["Content-Type"] = "application/fhir+json"
    response = requests.post(
        f"{_base_valid_uri()}/Bundle",
        data=read_body_from_file("post_bundle.json"),
        headers=headers
    )

    assert response.status_code == 201, "POST SCR failed"


@pytest.mark.smoketest
def test_audit_event(headers):
    headers["Content-Type"] = "application/fhir+json"
    response = requests.post(
        f"{_base_valid_uri()}/AuditEvent",
        data=read_body_from_file("audit_event.json"),
        headers=headers
    )

    assert response.status_code == 201, "POST Audit Event failed"
