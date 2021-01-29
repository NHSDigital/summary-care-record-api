package uk.nhs.adaptors.scr.mappings.from.hl7;

import lombok.SneakyThrows;
import org.hl7.fhir.r4.model.Resource;
import org.w3c.dom.Node;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public interface XmlToFhirMapper {
    String DATE_TIME_PATTERN = "yyyyMMddHHmmss";
    String SNOMED_SYSTEM = "http://snomed.info/sct";

    List<? extends Resource> map(Node document);

    @SneakyThrows
    static Date parseDate(String date) {
        return new SimpleDateFormat(DATE_TIME_PATTERN).parse(date);
    }

}
