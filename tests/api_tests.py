from typing import List
from time import time
from uuid import uuid4
import asyncio
import pytest
from tests import conftest
from aiohttp import ClientResponse
from api_test_utils import env
from api_test_utils import poll_until
from api_test_utils.apigee_api_apps import ApigeeApiDeveloperApps
from api_test_utils.api_session_client import APISessionClient
from api_test_utils.api_test_session_config import APITestSessionConfig
from api_test_utils.oauth_helper import OauthHelper
from environment import EnvVarWrapper


def _dict_path(raw, path: List[str]):
    if not raw:
        return raw

    if not path:
        return raw

    res = raw.get(path[0])
    if not res or len(path) == 1 or type(res) != dict:
        return res

    return _dict_path(res, path[1:])


# @pytest.mark.asyncio
# async def get_authorised_headers(app: ApigeeApiDeveloperApps):
#     custom_attributes = {
#         "jwks-resource-url": "https://raw.githubusercontent.com/NHSDigital/identity-service-jwks/main/jwks/"
#                              + "internal-dev/9baed6f4-1361-4a8e-8531-1f8426e3aba8.json",
#         "nhs-login-allowed-proofing-level": "P9"
#     }
#     api_products = [ENV['apigee_product']]
#
#     loop = asyncio.new_event_loop()
#     loop.run_until_complete(
#         app.setup_app(
#             api_products=api_products,
#             custom_attributes=custom_attributes,
#         )
#     )
#     oauth = OauthHelper(client_id=app.client_id, client_secret=app.client_secret, redirect_uri=app.callback_url)
#     token_url = f"{ENV['oauth_base_uri']}/{ENV['oauth_proxy']}/token"
#
#     jwt = oauth.create_jwt(
#         **{
#             "kid": "test-1",
#             "claims": {
#                 "sub": app.client_id,
#                 "iss": app.client_id,
#                 "jti": str(uuid4()),
#                 "aud": token_url,
#                 "exp": int(time()) + 60,
#             },
#         }
#     )
#
#     response = await oauth.get_token_response(grant_type="client_credentials", _jwt=jwt)
#     token = response["body"]
#     return {"Authorization": f'Bearer {token["access_token"]}'}

def _base_valid_uri(nhs_number) -> str:
    return f"DocumentReference?patient=https://fhir.nhs.uk/Id/nhs-number|{nhs_number}&_sort=date&type="
        + "http://snomed.info/sct|196981000000101&_count=1"


# @pytest.mark.smoketest
# @pytest.mark.asyncio
# async def test_client_credentials_happy_path(test_app, api_client: APISessionClient):
#     authorised_headers = await conftest.get_authorised_headers(test_app)
#
#     correlation_id = str(uuid4())
#     authorised_headers["X-Correlation-ID"] = correlation_id
#
#     async with api_client.get(
#         _base_valid_uri("9999999990"),
#         headers=authorised_headers,
#         allow_retries=True
#     ) as resp:
#         assert resp.status == 200
#         body = await resp.json()
#         assert "x-correlation-id" in resp.headers, resp.headers
#         assert resp.headers["x-correlation-id"] == correlation_id
#         assert_body(body)


@pytest.mark.smoketest
def test_output_test_config(api_test_config: APITestSessionConfig):
    print(api_test_config)


@pytest.mark.smoketest
@pytest.mark.asyncio
async def test_token_exchange_happy_path(test_app_and_product, api_client: APISessionClient):
    test_product, test_app = test_app_and_product

    token = await conftest.get_token(test_app)
    headers = {"Authorization": f'Bearer {token["access_token"]}'}

    async with api_client.get(
                _base_valid_uri("9999999990"),
                headers=headers,
                allow_retries=True
    ) as resp:
        assert resp.status == 200, 'get scr id failed'
        body = await resp.json()


# @pytest.mark.smoketest
# @pytest.mark.asyncio
# async def test_wait_for_get_scr_id(api_client: APISessionClient):
#     app = ApigeeApiDeveloperApps()
#     headers = await get_authorised_headers(app)
#
#     async def scr_id_returned(resp: ClientResponse):
#         if resp.status != 200:
#             return False
#
#         body = await resp.json()
#         return body.get("resourceType") == "Bundle"
#
#     await poll_until(
#         make_request=lambda: api_client.get(
#             "DocumentReference?patient=https://fhir.nhs.uk/Id/nhs-number"
#             "|9995000180&_sort=date&type=http://snomed.info/sct|196981000000101&_count=1",
#             headers=headers),
#         until=scr_id_returned, timeout=30
#     )
#     app.destroy_app()


@pytest.mark.smoketest
@pytest.mark.asyncio
async def test_wait_for_status(api_client: APISessionClient, api_test_config: APITestSessionConfig):
    async def is_deployed(resp: ClientResponse):
        if resp.status != 200:
            return False

        body = await resp.json()

        if body.get("commitId") != api_test_config.commit_id:
            return False

        backend = _dict_path(body, ["checks", "healthcheck", "outcome", "version"])
        if not backend:
            return True

        return backend.get("commitId") == api_test_config.commit_id

    deploy_timeout = 120 if api_test_config.api_environment.endswith("sandbox") else 30

    responses = await poll_until(
        make_request=lambda: api_client.get(
            "_status", headers={"apikey": env.status_endpoint_api_key()}
        ),
        until=is_deployed,
        body_resolver=lambda r: r.json(),
        timeout=deploy_timeout,
    )

    _, _, last_response_body = responses[-1]
    assert last_response_body.get("status") == "pass", "Last response: " + str(last_response_body)
