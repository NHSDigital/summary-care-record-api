package uk.nhs.adaptors.scr.mappings.from.hl7.common;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.Optional;

@Getter
@Setter
public class CodedEntry {
    private String id;
    private String codeValue;
    private String codeDisplay;
    private Optional<Date> effectiveTimeLow;
    private Optional<Date> effectiveTimeHigh;
    private String status;
}
