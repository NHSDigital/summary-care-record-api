import json
import pytest
from pytest_check import check
import requests
from .configuration import config
import re
import os


TEST_DATA_BASE_PATH = os.path.join(os.path.dirname(__file__), './test_data/')


def _base_valid_uri() -> str:
    prNo = re.search("pr-[0-9]+", config.SERVICE_BASE_PATH)
    prString = f"-{prNo.group()}" if prNo is not None else ""

    return f"{config.BASE_URL}/summary-care-record/FHIR/R4{prString}"


def read_body_from_file(file_name):
    with open(os.path.join(TEST_DATA_BASE_PATH, file_name)) as json_file:
        return json.load(json_file)


@pytest.mark.smoketest
def test_set_permission(headers):
    headers["Content-Type"] = "application/fhir+json"
    response = requests.post(
        f"{_base_valid_uri()}/$setPermission",
        json=read_body_from_file("set_permission.json"),
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
    with check: assert response_body["resourceType"] == "Bundle"
    if (response_body["total"] > 0):
        with check:
            assert response_body["entry"][0]["resource"]["resourceType"] == "DocumentReference"
            assert response_body["entry"][0]["resource"]["content"][0]["attachment"]["url"] is not None


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
        if (response_body["total"] > 0):
            assert response_body["entry"][0]["resource"]["resourceType"] == "Composition"
            assert response_body["entry"][0]["resource"]["section"] is not None


@pytest.mark.smoketest
def test_update_bundle(headers):
    headers["Content-Type"] = "application/fhir+json"
    response = requests.post(
        f"{_base_valid_uri()}/Bundle",
        json=read_body_from_file("post_bundle.json"),
        headers=headers
    )

    assert response.status_code == 201, "POST SCR failed"


@pytest.mark.smoketest
def test_audit_event(headers):
    headers["Content-Type"] = "application/fhir+json"
    response = requests.post(
        f"{_base_valid_uri()}/AuditEvent",
        json=read_body_from_file("audit_event.json"),
        headers=headers
    )

    assert response.status_code == 201, "POST Audit Event failed"
