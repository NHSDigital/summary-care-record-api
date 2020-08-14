package uk.nhs.adaptors.scr.controllers;

import static java.lang.System.lineSeparator;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.io.IOException;
import java.util.Stack;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import lombok.extern.slf4j.Slf4j;
import uk.nhs.adaptors.scr.models.requests.EndpointMockData;
import uk.nhs.adaptors.scr.models.requests.SpineRequest;
import uk.nhs.adaptors.scr.utils.MockedEndpointsStorage;

@RestController
@Slf4j
public class MockedEndpointController {

    @Autowired
    private MockedEndpointsStorage storage;

    private Stack<SpineRequest> requests = new Stack<>();

    @GetMapping("latest-request")
    public ResponseEntity<SpineRequest> getLatestRequest() {
        return requests.isEmpty() ? new ResponseEntity<>(NOT_FOUND) : new ResponseEntity<>(requests.pop(), OK);
    }

    @RequestMapping(value = "**", method = {GET, POST})
    public ResponseEntity<String> handleAny(HttpServletRequest request) throws IOException {
        String requestBody = getRequestBody(request);
        LOGGER.info("Incoming request: {} {} {}", request.getMethod(), request.getRequestURI(), requestBody);
        requests.push(new SpineRequest(request.getRequestURI(), request.getMethod(), requestBody));

        EndpointMockData endpointMockData = storage.get(request.getMethod(), request.getRequestURI());
        if (endpointMockData == null) {
            throw new ResponseStatusException(NOT_FOUND);
        }
        return new ResponseEntity(endpointMockData.getResponseContent(), HttpStatus.valueOf(endpointMockData.getHttpStatusCode()));
    }

    private String getRequestBody(HttpServletRequest request) throws IOException {
        if (asList("POST", "PUT", "PATCH").contains(request.getMethod())) {
            return request.getReader().lines().collect(joining(lineSeparator()));
        }

        return "";
    }
}
