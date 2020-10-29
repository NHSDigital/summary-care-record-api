package uk.nhs.adaptors.scr.models;

import lombok.Getter;
import lombok.Setter;
import org.hl7.fhir.r4.model.Bundle;

@Getter
@Setter
public class RequestData {
    private Bundle bundle;
    private String nhsdAsid;
}
