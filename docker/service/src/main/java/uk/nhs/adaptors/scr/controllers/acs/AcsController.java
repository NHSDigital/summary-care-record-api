package uk.nhs.adaptors.scr.controllers.acs;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.Header;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import uk.nhs.adaptors.scr.clients.SpineHttpClient;
import uk.nhs.adaptors.scr.models.ACSPayload;
import uk.nhs.adaptors.scr.services.AcsService;

import static org.springframework.http.HttpStatus.PRECONDITION_FAILED;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/summary-care-record")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class AcsController {
    private final AcsService acsService;

    @PostMapping(
        path = "/consent",
        consumes = {APPLICATION_JSON_VALUE},
        produces = {APPLICATION_JSON_VALUE})
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ResponseEntity<?> setResourcePermissions(@RequestBody ACSPayload acsSetResourceObject) {
        try {
            var spineResponse = acsService.setResourcePermissions(acsSetResourceObject);
            return mapSpineResponse(spineResponse);
        } catch (HttpClientErrorException e) {
            return new ResponseEntity<>(PRECONDITION_FAILED);
        }
    }

    private ResponseEntity<?> mapSpineResponse(SpineHttpClient.Response spineResponse) {
        var responseHeaders = new HttpHeaders();
        for (Header header : spineResponse.getHeaders()) {
            responseHeaders.add(header.getName(), header.getValue());
        }
        return new ResponseEntity<>(
            //TODO: we don't know what spine response looks like. I guess we should wrap it in our object and serialize
            spineResponse.getBody(),
            responseHeaders,
            HttpStatus.valueOf(spineResponse.getStatusCode()));
    }
}
