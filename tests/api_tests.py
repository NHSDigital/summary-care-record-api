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


@pytest.mark.smoketest
def test_get_scr_id(headers):
    response = requests.get(
        f"{_base_valid_uri()}/DocumentReference?patient=https://fhir.nhs.uk/Id/nhs-number|9999999990"
        + "&_sort=date&type=http://snomed.info/sct|196981000000101&_count=1",
        headers=headers_with_token
    )

    assert response.status_code == 200, "GET SCR ID failed"
    response_body = json.loads(response.text)
    with check:
        assert response_body["resourceType"] == "Bundle"


# @pytest.mark.smoketest
# def test_get_bundle(headers):
#     response = requests.get(
#         f"{_base_valid_uri()}/Bundle?composition.identifier=BF4180FD-C403-4EF7-A4AB-E07303AF5477$"
#         + "composition.subject:Patient.identifier=https://fhir.nhs.uk/Id/nhs-number|9995000180",
#         headers=headers_with_token
#     )
#
#     assert response.status_code == 200, "GET SCR failed"
#     response_body = json.loads(response.text)
#     with check:
#         assert response_body["resourceType"] == "Bundle"
