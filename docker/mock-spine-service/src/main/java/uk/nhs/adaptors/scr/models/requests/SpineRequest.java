package uk.nhs.adaptors.scr.models.requests;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SpineRequest {
    private String url;
    private String httpMethod;
    private String body;
}
