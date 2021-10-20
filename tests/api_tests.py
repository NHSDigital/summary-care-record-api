from typing import List
from time import time
from uuid import uuid4
import asyncio
import pytest
from aiohttp import ClientResponse
from api_test_utils import env
from api_test_utils import poll_until
from api_test_utils.apigee_api_apps import ApigeeApiDeveloperApps
from api_test_utils.api_session_client import APISessionClient
from api_test_utils.api_test_session_config import APITestSessionConfig
from api_test_utils.oauth_helper import OauthHelper
from environment import EnvVarWrapper


ENV = EnvVarWrapper(
    **{
        'client_id': 'CLIENT_ID',
        'client_secret': 'CLIENT_SECRET',
        'redirect_uri': 'REDIRECT_URI',
        'oauth_proxy': 'OAUTH_PROXY',
        'oauth_base_uri': 'OAUTH_BASE_URI',
        'apigee_product': 'APIGEE_PRODUCT'
    }
)


def _dict_path(raw, path: List[str]):
    if not raw:
        return raw

    if not path:
        return raw

    res = raw.get(path[0])
    if not res or len(path) == 1 or type(res) != dict:
        return res

    return _dict_path(res, path[1:])


def get_product_names(suffixes) -> List[str]:
    return [f"{ENV['apigee_product']}{suffix}" for suffix in suffixes]


async def get_authorised_headers():
    custom_attributes = {
        "jwks-resource-url": "https://raw.githubusercontent.com/NHSDigital/identity-service-jwks/main/jwks/"
                             + "internal-dev/9baed6f4-1361-4a8e-8531-1f8426e3aba8.json",
        "nhs-login-allowed-proofing-level": "P9"
    }

    api_products = get_product_names(["-user-restricted"])

    app = ApigeeApiDeveloperApps()

    loop = asyncio.new_event_loop()
    loop.run_until_complete(
        app.setup_app(
            api_products=api_products,
            custom_attributes=custom_attributes,
        )
    )
    oauth = OauthHelper(client_id=app.client_id, client_secret=app.client_secret, redirect_uri=app.callback_url)
    token_url = f"{ENV['oauth_base_uri']}/{ENV['oauth_proxy']}/token"

    jwt = oauth.create_jwt(
        **{
            "kid": "test-1",
            "claims": {
                "sub": app.client_id,
                "iss": app.client_id,
                "jti": str(uuid4()),
                "aud": token_url,
                "exp": int(time()) + 60,
            },
        }
    )

    response = await oauth.get_token_response(grant_type="client_credentials", _jwt=jwt)
    token = response["body"]
    app.destroy_app()
    return {"Authorization": f'Bearer {token["access_token"]}'}


@pytest.mark.smoketest
def test_output_test_config(api_test_config: APITestSessionConfig):
    print(api_test_config)


@pytest.mark.smoketest
@pytest.mark.asyncio
async def test_wait_for_get_scr_id(api_client: APISessionClient):
    headers = await get_authorised_headers()

    async def scr_id_returned(resp: ClientResponse):
        if resp.status != 200:
            return False

        body = await resp.json()
        return body.get("resourceType") == "Bundle"

    await poll_until(
        make_request=lambda: api_client.get(
            "DocumentReference?patient=https://fhir.nhs.uk/Id/nhs-number"
            "|9995000180&_sort=date&type=http://snomed.info/sct|196981000000101&_count=1",
            headers=headers),
        until=scr_id_returned, timeout=30
    )


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
