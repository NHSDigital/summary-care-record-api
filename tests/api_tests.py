from typing import List
import pytest
from aiohttp import ClientResponse
from api_test_utils import env
from api_test_utils import poll_until
from api_test_utils.api_session_client import APISessionClient
from api_test_utils.api_test_session_config import APITestSessionConfig
from api_test_utils.oauth_helper import OauthHelper
from environment import EnvVarWrapper

ENV = EnvVarWrapper(
    **{
        'client_id': 'CLIENT_ID',
        'client_secret': 'CLIENT_SECRET',
        'redirect_uri': 'REDIRECT_URI'
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


@pytest.mark.smoketest
def test_output_test_config(api_test_config: APITestSessionConfig):
    print(api_test_config)


@pytest.mark.smoketest
@pytest.mark.asyncio
async def test_wait_for_get_scr_id(api_client: APISessionClient):
    oauth = OauthHelper(ENV['client_id'], ENV['client_secret'], ENV['redirect_uri'])
    print("client id " + ENV['client_id'])
    print("redirect uri " + ENV['redirect_uri'])
    token = oauth.get_token_response(grant_type="authorization_code")

    async def scr_id_returned(resp: ClientResponse):
        if resp.status != 200:
            return False

        body = await resp.json()
        return body.get("resourceType") == "Bundle"

    await poll_until(
        make_request=lambda: api_client.get(
            "DocumentReference?patient=https://fhir.nhs.uk/Id/nhs-number"
            "|9995000180&_sort=date&type=http://snomed.info/sct|196981000000101&_count=1",
            headers={"Authorization": "Bearer " + token}),
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
