#How to add new UAT
1. Create directory structure, e.g.: _acs_set/bad_request, acs_set/success_
2. Put request and/or response files in it. Follow these rules:
    - request file name must end with request.json
    - response file name must end with response.json
    - you can have just one file per scenario - when request/response is empty
    - both request and response must have the same prefix, e.g.: _1-nhsNumberMissing.request.json, 1-nhsNumberMissing.response.json_
    - you can add as many request/response pairs as you want, they all will be picked up automatically
3. Create an ArgumentProvider class extending CustomArgumentsProvider. Provide directory details. See CustomArgumentsProvider.java
4. Create a test class. Annotate test method with _@ArgumentsSource_ passing your argument provider class as a parameter, e.g. _@ArgumentsSource(SetAcsBadRequest.class)_
5. All set. Tests will be run with the rest of integration tests.
