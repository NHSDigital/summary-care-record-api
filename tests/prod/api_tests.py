import pytest
import requests
from .configuration import config


def _base_valid_uri() -> str:
    return f"{config.BASE_URL}/summary-care-record/FHIR/R4"

def _headers() -> dict:
    headers = { "apikey" : config.API_KEY}
    return headers

@pytest.mark.prodsmoketest
def test_healthcheck():
    response = requests.get(
        f"{_base_valid_uri()}/_status",
        headers=_headers()
    )

    assert response.status_code == 200, "GET _status failed"
    assert response.json().get("status") == "pass", "_status response: " + str(response.json())
