package uk.nhs.adaptors.scr.uat.common;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class TestData {
    private final String fhir;
    private final String hl7v3;
}