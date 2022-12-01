package uk.nhs.adaptors.scr.mappings.from.hl7.common;

import lombok.Getter;
import lombok.Setter;
import org.hl7.fhir.r4.model.DateTimeType;

import java.util.Optional;

@Getter
@Setter
public class CodedEntry {
    private String id;
    private String codeValue;
    private String codeDisplay;
    private Optional<DateTimeType> effectiveTimeLow;
    private Optional<DateTimeType> effectiveTimeHigh;
    private Optional<DateTimeType> effectiveTime;
    private String status;
}
