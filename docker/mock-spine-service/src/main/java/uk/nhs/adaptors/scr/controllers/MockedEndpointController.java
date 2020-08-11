package uk.nhs.adaptors.scr.controllers;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import uk.nhs.adaptors.scr.models.requests.EndpointMockData;
import uk.nhs.adaptors.scr.utils.MockedEndpointsStorage;

import javax.servlet.http.HttpServletRequest;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

@RestController
@AllArgsConstructor
@Slf4j
public class MockedEndpointController {
    @Autowired
    private final MockedEndpointsStorage storage;

    @PostMapping("/summary-care-record/consent")
    public ResponseEntity acsSetResourcePermissions(@RequestBody String body, HttpServletRequest request) {
        EndpointMockData endpointMockData = storage.get(request.getMethod(), request.getRequestURI());
        if (body.isEmpty()) {
            throw new ResponseStatusException(BAD_REQUEST);
        }
        //return new ResponseEntity(OK);
        return new ResponseEntity(endpointMockData.getResponseContent(), HttpStatus.valueOf(endpointMockData.getHttpStatusCode()));
    }

    @RequestMapping("/*")
    public ResponseEntity get(HttpServletRequest request) {
        EndpointMockData endpointMockData = storage.get(request.getMethod(), request.getRequestURI());
        if (endpointMockData == null) {
            throw new ResponseStatusException(NOT_FOUND);
        }
        return new ResponseEntity(endpointMockData.getResponseContent(), HttpStatus.valueOf(endpointMockData.getHttpStatusCode()));
    }

}
