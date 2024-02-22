# Using the Postman collection

## 1. Importing the collection and environment variables

* Use postman 10.21.14 or later.
* Go to File->Import
* Select the request collection and environment json files
* Import

## 2. Filling in required environment variables

`key`
* For the dev environment this value is taken from the [NHS Digital Onboarding Service](https://dos-test.ptl.api.platform.nhs.uk/).
Once you've set up your application you can see the Active API Keys.
* In an int or production environment this value can be found in the [NHS Developer Portal](https://onboarding.prod.api.platform.nhs.uk).

`secret`
* The `secret` variable is taken from the same place as `key`.

`app-restricted-bearer-token`
* Should you be attempting to make calls using the Application Restricted Access calls in the Postman
collection, you can add your bearer token here.

`URID`
* This represents the NHS session user role ID of the currently logged-in user.
* Value is taken from a call to the `/userinfo` endpoint. You will see this value in the response under
`nhsid_nrbac_roles.[0].person_roleid`.
* Under `admin` in `User-Info (Generate URID)` you'll see a test which automatically sets the URID in a script.
It is advised to make this call immediately after logging in when you are using the Postman collection.

`callbackURL`
* This variable has to remain unchanged. An example.org url is acceptable.

`authURL`
* For dev environments [please use this url](https://internal-dev.api.service.nhs.uk/oauth2-mock/authorize).
* For int (smart card) environments [please use this url](https://int.api.service.nhs.uk/oauth2/authorize)
* For int (mock auth) environments [please use this url](https://int.api.service.nhs.uk/oauth2-mock/authorize)

`accessTokenURL`
* For dev environments [please use this url](https://internal-dev.api.service.nhs.uk/oauth2-mock/token).
* For int (smart card) environments [please use this url](https://int.api.service.nhs.uk/oauth2/token)
* For int (mock auth) environments [please use this url](https://int.api.service.nhs.uk/oauth2-mock/token)

`environment`
* We recommend making copies of these environment variables for each environment you are working on.
* This value will likely be either `internal-dev` or `int`.

`oauth-endpoint`
* If you're logging in without a smart card please use `oauth2-mock`, otherwise this value should be `oauth2`.

`PR-Code`
* Not typically used by NMEs. This is available to API internal developers to test new features. NMEs
should leave this variable blank.

## 3. How to use

Each API call and section within this Postman collection has its own documentation page. Please refer to
each of these for more details. After you have imported the collection and setup the environment variables
please navigate to the root folder of the collection, and click the "Authorization" tab. At the bottom
you will see the orange "Get New Access Token" button. Click this, filling in an NHS id you have access to,
or select one from the documentation on the same page. Then make a call to `admin/User-Info`.
You may now start using the collection as per your needs.
A typical SCR update happens by clicking the `bundle-round-trip` folder, and calling `Document Reference`
then `GET GP Summary Bundle` and finally `POST GP Summary Bundle`. Please pay attention to the Tests
section on each call, as we sometime set variables to make the process a little easier. Please also
read in detail the documentation on each call of the Postman collection, and [online](https://digital.nhs.uk/developer/api-catalogue/summary-care-record-fhir).
