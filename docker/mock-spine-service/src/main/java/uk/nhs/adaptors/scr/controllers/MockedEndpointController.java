package uk.nhs.adaptors.scr.controllers;

import static org.springframework.http.HttpStatus.NOT_FOUND;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.nhs.adaptors.scr.models.requests.EndpointMockData;
import uk.nhs.adaptors.scr.utils.MockedEndpointsStorage;

@RestController
@AllArgsConstructor
@Slf4j
public class MockedEndpointController {
    @Autowired
    private final MockedEndpointsStorage storage;

    @RequestMapping("/*")
    public ResponseEntity get(HttpServletRequest request) {
        EndpointMockData endpointMockData = storage.get(request.getMethod(), request.getRequestURI());
        if (endpointMockData == null) {
            throw new ResponseStatusException(NOT_FOUND);
        }
        return new ResponseEntity(endpointMockData.getResponseContent(), HttpStatus.valueOf(endpointMockData.getHttpStatusCode()));
    }
}
