from typing import List
from uuid import uuid4
from time import time
import asyncio
import pytest
from smoke import conftest
from aiohttp import ClientResponse
from api_test_utils import env
from api_test_utils import poll_until
from api_test_utils.api_session_client import APISessionClient
from api_test_utils.api_test_session_config import APITestSessionConfig


def dict_path(raw, path: List[str]):
    if not raw:
        return raw

    if not path:
        return raw

    res = raw.get(path[0])
    if not res or len(path) == 1 or type(res) != dict:
        return res

    return dict_path(res, path[1:])

def _base_valid_uri(nhs_number) -> str:
    return f"DocumentReference?_sort=date&_count=1&type=http://snomed.info/sct|196981000000101&patient=https://fhir.nhs.uk/Id/nhs-number|{nhs_number}"


@pytest.fixture(scope='function')
def authorised_headers(valid_access_token):
    return {"Authorization": f"Bearer {valid_access_token}"}


@pytest.mark.e2etest
@pytest.mark.asyncio
@pytest.mark.parametrize(
    'test_product_and_app',
    [
        {
            'scopes': ['urn:nhsd:apim:user-nhs-login:P9:summary-care-record'],
            'requested_proofing_level': 'P9',
            'identity_proofing_level': 'P9'
        },
        {
            'scopes': ['urn:nhsd:apim:user-nhs-login:P5:summary-care-record'],
            'requested_proofing_level': 'P5',
            'identity_proofing_level': 'P5'
        }
    ],
    indirect=True
)
async def test_token_exchange_both_header_and_exchange(api_client: APISessionClient,
                                                       test_product_and_app):
    test_product, test_app = test_product_and_app
    subject_token_claims = {
        'identity_proofing_level': test_app.request_params.get('identity_proofing_level')
    }
    authorised_headers = {}
    correlation_id = str(uuid4())
    authorised_headers["X-Correlation-ID"] = correlation_id
    authorised_headers["NHSD-User-Identity"] = conftest.nhs_login_id_token(test_app)

    # Use token exchange token in conjunction with JWT header
    token_response = await conftest.get_token_nhs_login_token_exchange(
        test_app,
        subject_token_claims=subject_token_claims
    )
    token = token_response["access_token"]

    authorised_headers["Authorization"] = f"Bearer {token}"

    async with api_client.get(
            _base_valid_uri("9999999990"),
            headers=authorised_headers,
            allow_retries=True
    ) as resp:
        assert resp.status == 200
        body = await resp.json()
        assert "x-correlation-id" in resp.headers, resp.headers
        assert resp.headers["x-correlation-id"] == correlation_id
        assert_body(body)



