package uk.nhs.adaptors.scr.controllers;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.nhs.adaptors.scr.models.requests.EndpointMockData;
import uk.nhs.adaptors.scr.utils.MockedEndpointsStorage;

@RestController
@AllArgsConstructor
@Slf4j
public class SetupController {
    @Autowired
    private final MockedEndpointsStorage storage;

    @PostMapping(value = "/setup", consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<String> postSetup(@RequestBody EndpointMockData endpointMockData) {
        if (HttpStatus.resolve(endpointMockData.getHttpStatusCode()) == null) {
            return new ResponseEntity<>("Given HTTP status code is not valid.", BAD_REQUEST);
        }
        if (HttpMethod.resolve(endpointMockData.getHttpMethod()) == null) {
            return new ResponseEntity<>("Given HTTP method is not valid.", BAD_REQUEST);
        }

        storage.add(endpointMockData);
        return new ResponseEntity<>("Endpoint mocked successfully!", OK);
    }

    @PostMapping(value = "/setup/reset")
    public ResponseEntity<String> postSetup() {
        storage.reset();
        return new ResponseEntity<>("Mock service reset successfully.", OK);
    }
}
