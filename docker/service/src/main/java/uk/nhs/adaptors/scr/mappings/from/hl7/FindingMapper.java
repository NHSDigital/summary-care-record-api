package uk.nhs.adaptors.scr.mappings.from.hl7;

import lombok.SneakyThrows;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Period;
import org.hl7.fhir.r4.model.Resource;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import uk.nhs.adaptors.scr.utils.FhirHelper;
import uk.nhs.adaptors.scr.utils.XmlUtils;

import java.util.ArrayList;
import java.util.List;

@Component
public class FindingMapper implements XmlToFhirMapper {

    private static final String BASE_PATH =
        "//QUPC_IN210000UK04/ControlActEvent/subject//GPSummary/pertinentInformation2/pertinentCREType/component/UKCT_MT144043UK02.Finding";

    private static final String FINDING_ID_XPATH = "./id/@root";
    private static final String FINDING_CODE_CODE_XPATH = "./code/@code";
    private static final String FINDING_CODE_DISPLAY_NAME_XPATH = "./code/@displayName";
    private static final String FINDING_STATUS_CODE_XPATH = "./statusCode/@code";
    private static final String FINDING_EFFECTIVE_TIME_LOW_XPATH = "./effectiveTime/low/@value";
    private static final String FINDING_EFFECTIVE_TIME_HIGH_XPATH = "./effectiveTime/low/@value";
    private static final String FINDING_EFFECTIVE_TIME_CENTRE_XPATH = "./effectiveTime/centre/@value";

    @SneakyThrows
    public List<Resource> map(Document document) {
        var resources = new ArrayList<Resource>();
        for (var node : XmlUtils.getNodesByXPath(document, BASE_PATH)) {
            var findingId =
                XmlUtils.getValueByXPath(node, FINDING_ID_XPATH);
            var findingCodeCode =
                XmlUtils.getValueByXPath(node, FINDING_CODE_CODE_XPATH);
            var findingCodeDisplayName =
                XmlUtils.getValueByXPath(node, FINDING_CODE_DISPLAY_NAME_XPATH);
            var findingStatusCode =
                XmlUtils.getValueByXPath(node, FINDING_STATUS_CODE_XPATH);
            var findingEffectiveTimeLow =
                XmlUtils.getOptionalValueByXPath(node, FINDING_EFFECTIVE_TIME_LOW_XPATH)
                .map(XmlToFhirMapper::parseDate);
            var findingEffectiveTimeHigh =
                XmlUtils.getOptionalValueByXPath(node, FINDING_EFFECTIVE_TIME_HIGH_XPATH)
                .map(XmlToFhirMapper::parseDate);
            var findingEffectiveTimeCentre =
                XmlUtils.getOptionalValueByXPath(node, FINDING_EFFECTIVE_TIME_CENTRE_XPATH)
                .map(XmlToFhirMapper::parseDate);

            var observation = new Observation();
            observation.setId(FhirHelper.randomUUID());
            observation.addIdentifier(new Identifier().setValue(findingId));
            observation.setCode(new CodeableConcept().addCoding(new Coding()
                .setCode(findingCodeCode)
                .setSystem(SNOMED_SYSTEM)
                .setDisplay(findingCodeDisplayName)));
            observation.setStatus(mapStatus(findingStatusCode));
            if (findingEffectiveTimeLow.isPresent() || findingEffectiveTimeHigh.isPresent()) {
                var period = new Period();
                findingEffectiveTimeLow.ifPresent(period::setStart);
                findingEffectiveTimeHigh.ifPresent(period::setEnd);
                observation.setEffective(period);
            } else {
                findingEffectiveTimeCentre
                    .map(DateTimeType::new)
                    .ifPresent(observation::setEffective);
            }

            resources.add(observation);
        }
        return resources;
    }

    private Observation.ObservationStatus mapStatus(String statusCode) {
        switch (statusCode) {
            case "normal":
            case "active":
            case "completed":
                return Observation.ObservationStatus.FINAL;
            case "nullified":
                return Observation.ObservationStatus.ENTEREDINERROR;
            default:
                throw new IllegalArgumentException(statusCode);
        }
    }
}
