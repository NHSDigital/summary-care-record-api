import pytest
import requests


def _base_valid_uri(nhs_number) -> str:
    return f"DocumentReference?patient=https://fhir.nhs.uk/Id/nhs-number|{nhs_number}&_sort=date&type="
    + "http://snomed.info/sct|196981000000101&_count=1"


@pytest.mark.smoketest
def test_retrieve_patient(headers_with_token):
    response = requests.get(
        _base_valid_uri("9999999990"), headers=headers_with_token
    )

    assert response.status == 200, "get scr id failed"
    assert response.json()["resourceType"] == "Bundle"
