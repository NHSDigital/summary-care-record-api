package uk.nhs.adaptors.scr.utils;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SpineRequest {
    private String url;
    private String httpMethod;
    private String body;
}
