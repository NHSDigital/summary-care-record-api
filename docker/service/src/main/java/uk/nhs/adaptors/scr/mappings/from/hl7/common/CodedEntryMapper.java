package uk.nhs.adaptors.scr.mappings.from.hl7.common;

import org.hl7.fhir.r4.model.DateTimeType;
import org.springframework.stereotype.Component;
import org.w3c.dom.Node;

import static uk.nhs.adaptors.scr.mappings.from.hl7.XmlToFhirMapper.parseDate;
import static uk.nhs.adaptors.scr.utils.XmlUtils.getOptionalValueByXPath;
import static uk.nhs.adaptors.scr.utils.XmlUtils.getValueByXPath;

@Component
public class CodedEntryMapper {

    private static final String CODE_VALUE_XPATH = "./code/@code";
    private static final String CODE_DISPLAY_XPATH = "./code/@displayName";
    private static final String EFFECTIVE_TIME_LOW_XPATH = "./effectiveTime/low/@value";
    private static final String EFFECTIVE_TIME_HIGH_XPATH = "./effectiveTime/high/@value";
    private static final String STATUS_CODE_XPATH = "./statusCode/@code";
    private static final String ID_XPATH = "./id/@root";

    public CodedEntry getCommonCodedEntryValues(Node node) {
        var id = getValueByXPath(node, ID_XPATH);
        var codeValue = getValueByXPath(node, CODE_VALUE_XPATH);
        var codeDisplayName = getValueByXPath(node, CODE_DISPLAY_XPATH);
        var effectiveTimeLow =
            getOptionalValueByXPath(node, EFFECTIVE_TIME_LOW_XPATH).map(it -> parseDate(it, DateTimeType.class));
        var effectiveTimeHigh =
            getOptionalValueByXPath(node, EFFECTIVE_TIME_HIGH_XPATH).map(it -> parseDate(it, DateTimeType.class));
        var statusCode = getValueByXPath(node, STATUS_CODE_XPATH);

        return new CodedEntry()
            .setId(id)
            .setCodeValue(codeValue)
            .setCodeDisplay(codeDisplayName)
            .setEffectiveTimeHigh(effectiveTimeHigh)
            .setEffectiveTimeLow(effectiveTimeLow)
            .setStatus(statusCode);
    }
}
