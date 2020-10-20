package uk.nhs.adaptors.scr.fhirmappings;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Period;

import uk.nhs.adaptors.scr.exceptions.FhirMappingException;
import uk.nhs.adaptors.scr.models.GpSummary;
import uk.nhs.adaptors.scr.models.gpsummarymodels.AllConditions;
import uk.nhs.adaptors.scr.models.gpsummarymodels.ConditionObject;
import uk.nhs.adaptors.scr.models.gpsummarymodels.EndTime;
import uk.nhs.adaptors.scr.utils.DateUtil;

public class ConditionMapper {
    public static void mapConditions(GpSummary gpSummary, List<Condition> conditions) throws FhirMappingException {
        AllConditions conditionListObject = new AllConditions();
        List<AllConditions> conditionParent = new ArrayList<>();

        List<ConditionObject> conditionObjectList = new ArrayList<>();
        for (Condition condition : conditions) {
            conditionObjectList.add(mapCondition(condition));
        }

        if (!conditionObjectList.isEmpty()) {
            conditionListObject.setConditionList(conditionObjectList);
            conditionParent.add(conditionListObject);
            if (!conditionParent.isEmpty()) {
                gpSummary.setConditionParent(conditionParent);
            }
        }
    }

    private static ConditionObject mapCondition(Condition condition) throws FhirMappingException {
        ConditionObject conditionObject = new ConditionObject();

        getConditionId(conditionObject, condition);
        getConditionCoding(conditionObject, condition);
        getConditionStartTime(conditionObject, condition);
        getConditionStatus(conditionObject, condition);

        return conditionObject;
    }

    private static void getConditionId(ConditionObject conditionObject, Condition condition) {
        if (condition.hasIdentifier()) {
            Identifier identifier = condition.getIdentifierFirstRep();
            if (identifier.hasValue()) {
                conditionObject.setConditionId(identifier.getValue());
            }
        }
    }

    private static void getConditionCoding(ConditionObject conditionObject, Condition condition) {
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

    private static void getConditionStartTime(ConditionObject conditionObject, Condition condition) throws FhirMappingException {
        if (condition.hasOnsetDateTimeType()) {
            String strDate = condition.getOnsetDateTimeType().getValueAsString();
            conditionObject.setConditionStartTime(DateUtil.formatDateFhirToHl7(strDate));
        }
        if (condition.hasOnsetPeriod()) {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            Period period = condition.getOnsetPeriod();
            if (period.hasStart()) {
                String strDate = dateFormat.format(period.getStart());
                conditionObject.setConditionStartTime(DateUtil.formatDate(strDate));
            }
            if (period.hasEnd()) {
                String strDate = dateFormat.format(period.getEnd());
                List<EndTime> endTimeList = new ArrayList<>();
                EndTime endTime = new EndTime();
                endTime.setEndTime(DateUtil.formatDate(strDate));
                endTimeList.add(endTime);
                conditionObject.setConditionEndTimeList(endTimeList);
            }
        }
    }

    private static void getConditionStatus(ConditionObject conditionObject, Condition condition) {
        if (condition.hasClinicalStatus()) {
            CodeableConcept codeableConcept = condition.getClinicalStatus();
            if (codeableConcept.hasCoding()) {
                Coding coding = codeableConcept.getCodingFirstRep();
                if (coding.hasCode()) {
                    Optional<String> status = getHL7Status(coding.getCode());
                    status.ifPresent(conditionObject::setConditionStatus);
                }
            }
        }
    }

    private static Optional<String> getHL7Status(String status) {
        switch (status) {
            case "active":
                return Optional.of("active");
            case "entered-in-error":
                return Optional.of("nullified");
            case "confirmed":
                return Optional.of("completed");
            default:
                return Optional.empty();
        }
    }
}
