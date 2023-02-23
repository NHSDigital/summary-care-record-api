import pytest
import uuid
import jwt
import requests
import time

from .configuration import config


@pytest.mark.appsmoketest
def test_valid_access_token():

    claims = {
        "sub": config.APPLICATION_RESTRICTED_API_KEY,
        "iss": config.APPLICATION_RESTRICTED_API_KEY,
        "jti": str(uuid.uuid4()),
        "aud": f"{config.BASE_URL}/oauth2-mock/token",
        "exp": int(time.time()) + 300,
    }

    headers = {"kid": config.KEY_ID}

    with open(f"/Users/abid.majid/Documents/test-2.pem", "r") as f:
        private_key = f.read()

    encoded_jwt = jwt.encode(
        claims, private_key, algorithm="RS512", headers=headers
    )

    response = requests.post(
        f"{config.BASE_URL}/oauth2-mock/token",
        data={
            "grant_type": "client_credentials",
            "client_assertion_type": "urn:ietf:params:oauth:client-assertion-type:jwt-bearer",
            "client_assertion": encoded_jwt,
        },
    )

    response_json = response.json()
    print(response_json)

    # Does our response object contain the expected keys (maybe others too):
    assert {"access_token", "expires_in", "token_type", "issued_at"} <= set(
        response_json.keys()
    )
    assert response_json["access_token"] is not None
    assert response_json["token_type"] == "Bearer"
    assert response_json["expires_in"] and int(response_json["expires_in"]) > 0
