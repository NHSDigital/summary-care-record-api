import uuid
import jwt
import requests
import time

while True:
    try:
        claims = {
            "sub": "yLnp8eYDuujs52lkGWHBWmn6hyP5jDDc",
            "iss": "yLnp8eYDuujs52lkGWHBWmn6hyP5jDDc",
            "jti": str(uuid.uuid4()),
            "aud": f"https://internal-dev.api.service.nhs.uk/oauth2-mock/token",
            "exp": int(time.time()) + 300,
        }

        headers = {"kid": "test-2"}

        with open(f"/Users/abid.majid/Documents/test-2.pem", "r") as f:
            private_key = f.read()

        encoded_jwt = jwt.encode(
            claims, private_key, algorithm="RS512", headers=headers
        )

        response = requests.post(
            f"https://internal-dev.api.service.nhs.uk/oauth2-mock/token",
            data={
                "grant_type": "client_credentials",
                "client_assertion_type": "urn:ietf:params:oauth:client-assertion-type:jwt-bearer",
                "client_assertion": encoded_jwt,
            },
        )

        response_json = response.json()
        myToken = response_json["access_token"]

        headersGet = {"NHSD-session-URID": "555254242102", "Authorization": "Bearer " + myToken}

        docRefURL = f"https://internal-dev.api.service.nhs.uk/summary-care-record/FHIR/R4-pr-441/" + \
                    "DocumentReference?patient=https://fhir.nhs.uk/Id/nhs-number%7C9995000180&type=" + \
                    "http://snomed.info/sct%7C196981000000101&_sort=date&_count=1"

        bundleUrl = f"https://internal-dev.api.service.nhs.uk/summary-care-record/FHIR/R4-pr-441/" + \
                    "Bundle?composition.identifier=F5EC9F9F-46D4-4FA3-8131-415FF6BA1B44&" + \
                    "composition.subject:Patient.identifier=https://fhir.nhs.uk/Id/nhs-number|9995000180"

        docRef = requests.get(url=docRefURL, headers=headersGet)
        bundleUrl = requests.get(url=bundleUrl, headers=headersGet)

        docRef_json = docRef.json()
        bundle_json = bundleUrl.json()
        time.sleep(60)  # every 60 seconds
        print("DocumentReference\n")
        print(docRef_json)
        print("\n\n")
        print("Bundle\n")
        print(bundle_json)

    except:
        print("There was an error sending the requests.")
