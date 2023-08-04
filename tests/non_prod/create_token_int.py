import uuid
import jwt
import requests
import time

# This file is used to generate an access token which you can then use on the POSTMAN app-restricted API calls.
# This file should be added to tests/non_prod
# Check the path to the pem key is correct.
# Sub and ISS values need to be the app key.
# You will need to run the following to make this work.
# pip3 install PyJWT requests

claims = {
            "sub": "GLp7y8O5dyeAZo4NJq4yAIGHxgW8i8Pa",
            "iss": "GLp7y8O5dyeAZo4NJq4yAIGHxgW8i8Pa",
            "jti": str(uuid.uuid4()),
            "aud": f"https://int.api.service.nhs.uk/oauth2-mock/token",
            "exp": int(time.time()) + 300,
        }

headers = {"kid": "test-3"}

with open(f"/Users/steven.mccullagh/Documents/Projects/NHSD/Keys/int/test-3.pem", "r") as f:
    private_key = f.read()

encoded_jwt = jwt.encode(
    claims, private_key, algorithm="RS512", headers=headers
)

response = requests.post(
    f"https://int.api.service.nhs.uk/oauth2-mock/token",
    data={
        "grant_type": "client_credentials",
        "client_assertion_type": "urn:ietf:params:oauth:client-assertion-type:jwt-bearer",
        "client_assertion": encoded_jwt,
    },
)

response_json = response.json()
print(response_json)