package uk.nhs.adaptors.scr.mappings.from.hl7;

import lombok.SneakyThrows;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.Resource;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import uk.nhs.adaptors.scr.utils.FhirHelper;
import uk.nhs.adaptors.scr.utils.XmlUtils;

import java.util.ArrayList;
import java.util.List;

@Component
public class DiagnosisMapper implements XmlToFhirMapper {

    private static final String GP_SUMMARY_XPATH = "//QUPC_IN210000UK04/ControlActEvent/subject//GPSummary";
    private static final String DIAGNOSIS_BASE_PATH =
        GP_SUMMARY_XPATH + "/pertinentInformation2/pertinentCREType/component/UKCT_MT144042UK01.Diagnosis";

    private static final String DIAGNOSIS_ID_XPATH = "./id/@root";
    private static final String DIAGNOSIS_CODE_CODE_XPATH = "./code/@code";
    private static final String DIAGNOSIS_CODE_DISPLAY_XPATH = "./code/@displayName";
    private static final String DIAGNOSIS_STATUS_CODE_CODE_XPATH = "./statusCode/@code";
    private static final String DIAGNOSIS_EFFECTIVE_TIME_LOW_XPATH = "./effectiveTime/low/@value";
    private static final String DIAGNOSIS_EFFECTIVE_TIME_HIGH_XPATH = "./effectiveTime/high/@value";

    @SneakyThrows
    public List<Resource> map(Document document) {
        var resources = new ArrayList<Resource>();
        for (var node : XmlUtils.getNodesByXPath(document, DIAGNOSIS_BASE_PATH)) {
            var diagnosisId =
                XmlUtils.getValueByXPath(node, DIAGNOSIS_ID_XPATH);
            var diagnosisCodeCode =
                XmlUtils.getValueByXPath(node, DIAGNOSIS_CODE_CODE_XPATH);
            var diagnosisCodeDisplay =
                XmlUtils.getValueByXPath(node, DIAGNOSIS_CODE_DISPLAY_XPATH);
            var diagnosisStatusCodeCode =
                XmlUtils.getValueByXPath(node, DIAGNOSIS_STATUS_CODE_CODE_XPATH);
            var diagnosisEffectiveTimeLow =
                XmlToFhirMapper.parseDate(XmlUtils.getValueByXPath(node, DIAGNOSIS_EFFECTIVE_TIME_LOW_XPATH));
            var diagnosisEffectiveTimeHigh =
                XmlUtils.getOptionalValueByXPath(node, DIAGNOSIS_EFFECTIVE_TIME_HIGH_XPATH)
                .map(XmlToFhirMapper::parseDate);

            var condition = new Condition();
            condition.setId(FhirHelper.randomUUID());
            condition.addIdentifier()
                .setValue(diagnosisId);
            condition.setCode(new CodeableConcept().addCoding(new Coding()
                .setCode(diagnosisCodeCode)
                .setSystem(SNOMED_SYSTEM)
                .setDisplay(diagnosisCodeDisplay)));
            setConditionStatus(condition, diagnosisStatusCodeCode);

            var lowDateTime = new DateTimeType();
            lowDateTime.setValue(diagnosisEffectiveTimeLow);
            if (diagnosisEffectiveTimeHigh.isPresent()) {
                condition.setOnset(lowDateTime);
                condition.setAbatement(new DateTimeType().setValue(diagnosisEffectiveTimeHigh.get()));
            } else {
                condition.setOnset(lowDateTime);
            }

            resources.add(condition);
        }
        return resources;
    }

    private static void setConditionStatus(Condition condition, String statusCode) {
        switch (statusCode) {
            case "normal":
            case "active":
                setClinicalStatus(condition, "active");
                break;
            case "nullified":
                setVerificationStatus(condition, "entered-in-error");
                break;
            case "completed":
                setVerificationStatus(condition, "confirmed");
                break;
            default:
                throw new IllegalArgumentException(statusCode);
        }
    }

    private static void setClinicalStatus(Condition condition, String value) {
        condition.setClinicalStatus(new CodeableConcept().addCoding(new Coding()
            .setSystem("http://hl7.org/fhir/ValueSet/condition-clinical")
            .setCode(value)));
    }

    private static void setVerificationStatus(Condition condition, String value) {
        condition.setVerificationStatus(new CodeableConcept().addCoding(new Coding()
            .setSystem("http://hl7.org/fhir/ValueSet/condition-ver-status")
            .setCode(value)));
    }
}
