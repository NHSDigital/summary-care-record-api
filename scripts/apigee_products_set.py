import pytest
from pytest_nhsd_apim.apigee_apis import (
    AccessTokensAPI,
    ApigeeClient,
    ApigeeNonProdCredentials,
    ApiProductsAPI,
    DebugSessionsAPI,
    DeveloperAppsAPI
)

from pytest_nhsd_apim.identity_service import (
    ClientCredentialsConfig,
    ClientCredentialsAuthenticator,
    AuthorizationCodeConfig,
    AuthorizationCodeAuthenticator,
    KeycloakUserConfig,
    KeycloakUserAuthenticator,
    TokenExchangeConfig,
    TokenExchangeAuthenticator,
)

import pprint
import requests


def test_authorization_code_authenticator(_test_app_credentials, apigee_environment):
    # 1. Set your app config
    config = AuthorizationCodeConfig(
        environment=apigee_environment,
        identity_service_base_url=f"https://{apigee_environment}.api.service.nhs.uk/oauth2-mock",
        callback_url="https://example.org/callback",
        client_id=_test_app_credentials["rx35jQzKBQW1l5FxY4siA7Wr7hsuhAJb"],
        client_secret=_test_app_credentials["xTLKl8OCWawFRXBV"],
        scope="nhs-cis2",
        login_form={"username": "656005750104"},
    )

    # 2. Pass the config to the Authenticator
    authenticator = AuthorizationCodeAuthenticator(config=config)

    # 3. Get your token
    token_response = authenticator.get_token()
    assert "access_token" in token_response
    token = token_response["access_token"]

    # 4. Use the token and confirm is valid
    headers = {"Authorization": f"Bearer {token}"}
    resp = requests.get(
        f"https://{apigee_environment}.api.service.nhs.uk/mock-jwks/test-auth/nhs-cis2/aal3",
        headers=headers,
    )
    assert resp.status_code == 200


@pytest.fixture()
def client():
    config = ApigeeNonProdCredentials()
    print(config)
    return ApigeeClient(config=config)


class TestDeveloperAppsAPI:
    def test_put_app_by_name(self, client):
            print(client)
            body = {
                "apiProducts": ["identity-service-internal-dev", "mock-jwks-internal-dev"],
                "attributes": [
                    {"name": "ADMIN_EMAIL", "value": "lucas.fantini@nhs.net"},
                    {"name": "DisplayName", "value": "My App"},
                    {"name": "Notes", "value": "Notes for developer app"},
                    {"name": "MINT_BILLING_TYPE", "value": "POSTPAID"},
                ],
                "callbackUrl": "new-example.com",
                "name": "myapp",
                "scopes": [],
                "status": "approved",
            }
            pprint.pprint(
                developer_apps.put_app_by_name(
                    email="lucas.fantini@nhs.net", body=body, app_name="myapp"
                )
            )
