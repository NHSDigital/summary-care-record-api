import pytest
from api_test_utils.api_session_client import APISessionClient


def _base_valid_uri(nhs_number) -> str:
    return f"DocumentReference?patient=https://fhir.nhs.uk/Id/nhs-number|{nhs_number}&_sort=date&type="
    + "http://snomed.info/sct|196981000000101&_count=1"


@pytest.mark.smoketest
@pytest.mark.asyncio
async def test_retrieve_patient(headers_with_token, api_client: APISessionClient):
    async with api_client.get(
                _base_valid_uri("9999999990"),
                headers=headers_with_token,
                allow_retries=True
    ) as resp:
        assert resp.status == 200, "get scr id failed"
        assert resp.json()["resourceType"] == "Bundle"
