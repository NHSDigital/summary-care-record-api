package uk.nhs.adaptors.scr.fhirmappings;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Period;
import org.hl7.fhir.r4.model.Resource;
import uk.nhs.adaptors.scr.exceptions.FhirMappingException;
import uk.nhs.adaptors.scr.models.GpSummary;
import uk.nhs.adaptors.scr.models.gpsummarymodels.AllConditions;
import uk.nhs.adaptors.scr.models.gpsummarymodels.ConditionObject;
import uk.nhs.adaptors.scr.models.gpsummarymodels.EndTime;
import uk.nhs.adaptors.scr.utils.DateUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static uk.nhs.adaptors.scr.utils.DateUtil.formatDate;

public class ConditionMapper {
    public static void mapConditions(GpSummary gpSummary, List<Resource> conditions) throws FhirMappingException {
        List<ConditionObject> conditionObjectList = conditions.stream()
            .map(Condition.class::cast)
            .map(ConditionMapper::mapCondition)
            .collect(Collectors.toList());

        if (!conditionObjectList.isEmpty()) {
            AllConditions conditionListObject = new AllConditions();
            List<AllConditions> conditionParent = new ArrayList<>();

            conditionListObject.setConditionList(conditionObjectList);
            conditionParent.add(conditionListObject);
            gpSummary.setConditionParent(conditionParent);
        }
    }

    private static ConditionObject mapCondition(Condition condition) throws FhirMappingException {
        ConditionObject conditionObject = new ConditionObject();

        setConditionId(conditionObject, condition);
        setConditionCoding(conditionObject, condition);
        setConditionTime(conditionObject, condition);
        setConditionStatus(conditionObject, condition);

        return conditionObject;
    }

    private static void setConditionId(ConditionObject conditionObject, Condition condition) {
        if (condition.hasIdentifier()) {
            Identifier identifier = condition.getIdentifierFirstRep();
            if (identifier.hasValue()) {
                conditionObject.setConditionId(identifier.getValue());
            }
        }
    }

    private static void setConditionCoding(ConditionObject conditionObject, Condition condition) {
        if (condition.hasCode()) {
            if (condition.getCode().hasCoding()) {
                Coding coding = condition.getCode().getCodingFirstRep();
                if (coding.hasCode()) {
                    conditionObject.setConditionCode(coding.getCode());
                }
                if (coding.hasDisplay()) {
                    conditionObject.setConditionDisplay(coding.getDisplay());
                }
            }
        }
    }

    private static void setConditionTime(ConditionObject conditionObject, Condition condition) throws FhirMappingException {
        if (condition.hasOnsetDateTimeType()) {
            conditionObject.setConditionStartTime(formatDate(condition.getOnsetDateTimeType().getValue()));
        }
        if (condition.hasOnsetPeriod()) {
            Period period = condition.getOnsetPeriod();
            if (period.hasStart()) {
                conditionObject.setConditionStartTime(DateUtil.formatDate(period.getStart()));
            }
            if (period.hasEnd()) {
                String endDate = DateUtil.formatDate(period.getEnd());
                List<EndTime> endTimeList = new ArrayList<>();
                EndTime endTime = new EndTime();
                endTime.setEndTime(endDate);
                endTimeList.add(endTime);
                conditionObject.setConditionEndTimeList(endTimeList);
            }
        }
    }

    private static void setConditionStatus(ConditionObject conditionObject, Condition condition) throws FhirMappingException {
        if (condition.hasClinicalStatus()) {
            CodeableConcept codeableConcept = condition.getClinicalStatus();
            if (codeableConcept.hasCoding()) {
                Coding coding = codeableConcept.getCodingFirstRep();
                if (coding.hasCode()) {
                    conditionObject.setConditionStatus(getHL7Status(coding.getCode()));
                }
            }
        }
    }

    private static String getHL7Status(String status) {
        switch (status) {
            case "active":
                return "active";
            case "entered-in-error":
                return "nullified";
            case "confirmed":
                return "completed";
            default:
                throw new FhirMappingException("Condition status is not valid, can only be active, confirmed or entered-in-error.");
        }
    }
}
