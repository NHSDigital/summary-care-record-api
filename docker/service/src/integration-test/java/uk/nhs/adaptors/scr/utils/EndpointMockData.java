package uk.nhs.adaptors.scr.utils;

import org.springframework.http.ResponseEntity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EndpointMockData {
    private String url;
    private Integer httpStatusCode;
    private String httpMethod;
    private String responseContent;
    private ResponseEntity responseEntity;
}
