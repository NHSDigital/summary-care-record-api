package uk.nhs.adaptors.scr.mappings.from.hl7.common;

import lombok.RequiredArgsConstructor;
import org.hl7.fhir.r4.model.DateTimeType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Node;
import uk.nhs.adaptors.scr.utils.XmlUtils;

import static uk.nhs.adaptors.scr.mappings.from.hl7.XmlToFhirMapper.parseDate;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CodedEntryMapper {

    private static final String CODE_VALUE_XPATH = "./code/@code";
    private static final String CODE_DISPLAY_XPATH = "./code/@displayName";
    private static final String EFFECTIVE_TIME_LOW_XPATH = "./effectiveTime/low/@value";
    private static final String EFFECTIVE_TIME_HIGH_XPATH = "./effectiveTime/high/@value";
    private static final String EFFECTIVE_TIME_XPATH = "./effectiveTime/@value";
    private static final String STATUS_CODE_XPATH = "./statusCode/@code";
    private static final String ID_XPATH = "./id/@root";

    private final XmlUtils xmlUtils;

    public CodedEntry getCommonCodedEntryValues(Node node) {
        var id = xmlUtils.getValueByXPath(node, ID_XPATH);
        var codeValue = xmlUtils.getValueByXPath(node, CODE_VALUE_XPATH);
        var codeDisplayName = xmlUtils.getValueByXPath(node, CODE_DISPLAY_XPATH);
        var effectiveTimeLow =
            xmlUtils.getOptionalValueByXPath(node, EFFECTIVE_TIME_LOW_XPATH).map(it -> parseDate(it, DateTimeType.class));
        var effectiveTimeHigh =
            xmlUtils.getOptionalValueByXPath(node, EFFECTIVE_TIME_HIGH_XPATH).map(it -> parseDate(it, DateTimeType.class));
        var effectiveTime =
            xmlUtils.getOptionalValueByXPath(node, EFFECTIVE_TIME_XPATH).map(it -> parseDate(it, DateTimeType.class));
        var statusCode = xmlUtils.getValueByXPath(node, STATUS_CODE_XPATH);

        return new CodedEntry()
            .setId(id)
            .setCodeValue(codeValue)
            .setCodeDisplay(codeDisplayName)
            .setEffectiveTimeHigh(effectiveTimeHigh)
            .setEffectiveTimeLow(effectiveTimeLow)
            .setEffectiveTime(effectiveTime)
            .setStatus(statusCode);
    }

    public CodedEntry getEssentialCodedEntryValues(Node node) {
        var id = xmlUtils.getValueByXPath(node, ID_XPATH);

        return new CodedEntry().setId(id);
    }
}
