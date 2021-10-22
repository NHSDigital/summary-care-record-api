# flake8: noqa
import asyncio
from time import time
from uuid import uuid4
from typing import List
import os

import pytest
from api_test_utils.api_test_session_config import APITestSessionConfig
from api_test_utils.fixtures import api_client  # pylint: disable=unused-import
from api_test_utils.apigee_api_apps import ApigeeApiDeveloperApps
from api_test_utils.apigee_api_products import ApigeeApiProducts
from api_test_utils.oauth_helper import OauthHelper


def get_env(variable_name: str) -> str:
    """Returns a environment variable"""
    try:
        var = os.environ[variable_name]
        if not var:
            raise RuntimeError(f"Variable is null, Check {variable_name}.")
        return var
    except KeyError:
        raise RuntimeError(f"Variable is not set, Check {variable_name}.")


@pytest.fixture(scope="session")
def api_test_config() -> APITestSessionConfig:
    return APITestSessionConfig()


# @pytest.yield_fixture(scope='function')
# @pytest.mark.asyncio
# async def test_product():
#     """ Setup and Teardown, create an product at the start and then destroy it at the end """
#
#     # create apigee instance & attach instance to class
#     api = ApigeeApiProducts()
#
#     print("Creating Test Product..")
#     await api.create_new_product()
#
#     yield api
#     # teardown
#     print("Destroying Test Product..")
#     await api.destroy_product()
#
#
# @pytest.yield_fixture(scope='function')
# @pytest.mark.asyncio
# async def test_app():
#     """ Setup and Teardown, create an app at the start and then destroy it at the end """
#
#     # create apigee instance & attach instance to class
#     api = ApigeeApiDeveloperApps()
#
#     print("Creating Test App..")
#     await api.create_new_app() # callback_url=get_env("REDIRECT_URI") czy to potrzebne?
#
#     yield api
#     # teardown
#     print("Destroying Test App..")
#     await api.destroy_app()


@pytest.fixture()
@pytest.mark.asyncio
async def test_app_and_product():
    apigee_product = ApigeeApiProducts()
    await apigee_product.create_new_product()
    await apigee_product.update_scopes(['urn:nshd:apim:app:jwks', "test_scope:USER",
                                      f"urn:nhsd:apim:app:level3:{get_env('APIGEE_PRODUCT')}"])

    apigee_app = ApigeeApiDeveloperApps()
    apigee_app.setup_app(
        api_products=[apigee_product.name],
        custom_attributes={
            "jwks-resource-url": "https://raw.githubusercontent.com/NHSDigital/identity-service-jwks/main/jwks"
            + "/internal-dev/9baed6f4-1361-4a8e-8531-1f8426e3aba8.json"
        },
    )
#     await apigee_app.create_new_app() # callback_url=get_env("REDIRECT_URI") czy to potrzebne?

    apigee_app.oauth = OauthHelper(apigee_app.client_id, apigee_app.client_secret, apigee_app.callback_url)
    yield apigee_product, apigee_app

    await apigee_app.destroy_app()
    await apigee_product.destroy_product()


def nhs_login_subject_token(test_app: ApigeeApiDeveloperApps) -> str:
    id_token_claims = {
        "aud": "tf_-APIM-1",
        "id_status": "verified",
        "token_use": "id",
        "auth_time": 1616600683,
        "iss": "https://internal-dev.api.service.nhs.uk",  # Points to internal dev -> testing JWKS
        "sub": "https://internal-dev.api.service.nhs.uk",
        "exp": int(time()) + 300,
        "iat": int(time()) - 10,
        "vtm": "https://auth.sandpit.signin.nhs.uk/trustmark/auth.sandpit.signin.nhs.uk",
        "jti": str(uuid4()),
        "identity_proofing_level": "P9",
        "birthdate": "1939-09-26",
        "nhs_number": "9912003888",
        "nonce": "randomnonce",
        "surname": "CARTHY",
        "vot": "P9.Cp.Cd",
        "family_name": "CARTHY",
    }

    id_token_headers = {
        "kid": "nhs-login",
        "typ": "JWT",
        "alg": "RS512",
    }

    # private key we retrieved from earlier
    nhs_login_id_token_private_key_path = get_env("ID_TOKEN_NHS_LOGIN_PRIVATE_KEY_ABSOLUTE_PATH")

    with open(nhs_login_id_token_private_key_path, "r") as f:
        contents = f.read()

    id_token_jwt = test_app.oauth.create_id_token_jwt(
        algorithm="RS512",
        claims=id_token_claims,
        headers=id_token_headers,
        signing_key=contents,
    )

    return subject_token


@pytest.mark.asyncio
async def get_token(test_app: ApigeeApiDeveloperApps):
    subject_token = nhs_login_subject_token(test_app)
    client_assertion_jwt = test_app.oauth.create_jwt(kid="test-1")

    token_resp = await test_app.oauth.get_token_response(
        grant_type="token_exchange",
        data={
            "grant_type": "urn:ietf:params:oauth:grant-type:token-exchange",
            "subject_token_type": "urn:ietf:params:oauth:token-type:id_token",
            "client_assertion_type": "urn:ietf:params:oauth:client-assertion-type:jwt-bearer",
            "subject_token": subject_token,
            "client_assertion": client_assertion_jwt,
        },
    )
    assert token_resp["status_code"] == 200

    return token_resp["body"]
