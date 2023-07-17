import pytest
from pytest_nhsd_apim.apigee_apis import (
    AccessTokensAPI,
    ApigeeClient,
    ApigeeNonProdCredentials,
    ApiProductsAPI,
    DebugSessionsAPI,
    DeveloperAppsAPI,
)
import pprint


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
