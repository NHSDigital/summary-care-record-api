import pytest
import requests
from .configuration import config
import re


def _base_valid_uri() -> str:
    prNo = re.search("pr-[0-9]+", config.SERVICE_BASE_PATH)
    prString = f"-{prNo.group()}" if prNo is not None else ""
    return f"{config.BASE_URL}/summary-care-record/FHIR/R4{prString}"


@pytest.mark.smoketest
def test_get_scr_id(headers_with_token):
    response = requests.get(
        f"{_base_valid_uri()}/DocumentReference?patient=https://fhir.nhs.uk/Id/nhs-number|9999999990"
        + "&_sort=date&type=http://snomed.info/sct|196981000000101&_count=1",
        headers=headers_with_token
    )

    assert response.status == 200, "get scr id failed"
    assert response.json()["resourceType"] == "Bundle"
