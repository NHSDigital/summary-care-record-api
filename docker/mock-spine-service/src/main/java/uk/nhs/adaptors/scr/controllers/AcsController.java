package uk.nhs.adaptors.scr.controllers;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.TEXT_XML_VALUE;

import java.io.StringReader;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.nhs.adaptors.scr.utils.ResourcesUtils;

@RestController
@AllArgsConstructor
@Slf4j
public class AcsController {
    private static final String GET_RESOURCE_PERMISSION_SAMPLE_MESSAGE_PATH = "responses/get_resource_permissions.xml";
    private static final String GET_RESOURCE_PERMISSIONS_NODE = "//*[local-name()='GET_RESOURCE_PERMISSIONS_INUK01']";
    private static final String SET_RESOURCE_PERMISSIONS_NODE = "//*[local-name()='SET_RESOURCE_PERMISSIONS_INUK01']";

    @PostMapping(value = "/acs",
        produces = TEXT_XML_VALUE)
    public ResponseEntity<String> postAcs(@RequestBody String request) {
        try {
            SAXReader reader = new SAXReader();
            Document document = reader.read(new StringReader(request));

            if (isNodeExists(document, GET_RESOURCE_PERMISSIONS_NODE)) {
                String sampleResponse = ResourcesUtils.getResourceAsString(GET_RESOURCE_PERMISSION_SAMPLE_MESSAGE_PATH);
                return new ResponseEntity<>(sampleResponse, OK);
            } else if (isNodeExists(document, SET_RESOURCE_PERMISSIONS_NODE)) {
                return new ResponseEntity<>(OK);
            }

            return new ResponseEntity<>(BAD_REQUEST);

        } catch (DocumentException e) {
            throw new ResponseStatusException(BAD_REQUEST, "This is not a valid XML message");
        }
    }

    private boolean isNodeExists(Document document, String xpath) {
        return document.selectSingleNode(xpath) != null;
    }
}
