package uk.nhs.adaptors.scr.models.requests;

import lombok.Getter;

@Getter
public class EndpointMockData {
    private String url;
    private String httpMethod;
    private Integer httpStatusCode;
    private String responseContent;
}
