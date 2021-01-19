# How to add new UAT
1. Create directory structure, e.g.: _resources/uat/acs_set/bad_request, acs_set/success_
2. Put request and/or response files in it. Follow these rules:
    - request file name must end with request.json
    - response file name must end with response.json
    - you can have just one file per scenario - when request/response is empty (it happens when either response or request have no body, e.g Upload SCR happy path scenario - _UploadScrUAT.testTranslatingFromFhirToHL7v3_)
    - both request and response must have the same prefix, e.g.: _1-nhsNumberMissing.request.json, 1-nhsNumberMissing.response.json_
    - you can add as many request/response pairs as you want, they all will be picked up automatically
3  Some of the test cases require Spine to be mocked. This can be achieved using _WireMock_. Mocked Spine responses are kept in _uat/responses_ directory.
It depends on a specific test case scenario to decide whether mocking is required.
4. Create an ArgumentProvider class extending CustomArgumentsProvider. Provide directory details. See CustomArgumentsProvider.java
5. Create test classes and store in _uk.nhs.adaptors.scr.uat_ package. Classes should have _UAT_ suffix, e.g. _SendAlertUAT_. Annotate test method with _@ArgumentsSource_ passing your argument provider class as a parameter, e.g. _@ArgumentsSource(SetAcsBadRequest.class)_
6. All set. Tests will be run with the rest of integration tests.
