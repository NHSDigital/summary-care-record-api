package uk.nhs.adaptors.scr.fhirmappings;

import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Period;
import org.hl7.fhir.r4.model.Resource;
import uk.nhs.adaptors.scr.exceptions.FhirMappingException;
import uk.nhs.adaptors.scr.models.GpSummary;
import uk.nhs.adaptors.scr.models.gpsummarymodels.EndTime;
import uk.nhs.adaptors.scr.models.gpsummarymodels.ObservationObject;
import uk.nhs.adaptors.scr.utils.DateUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static uk.nhs.adaptors.scr.utils.DateUtil.formatDate;

public class ObservationMapper {

    public static void mapObservations(GpSummary gpSummary, List<Resource> observations) throws FhirMappingException {
        List<ObservationObject> observationList = observations.stream()
            .map(Observation.class::cast)
            .map(ObservationMapper::getObservationObject)
            .collect(Collectors.toList());

        gpSummary.setObservationList(observationList);
    }

    private static ObservationObject getObservationObject(Observation observation) throws FhirMappingException {
        ObservationObject observationObject = new ObservationObject();

        setObservationCoding(observationObject, observation);
        setObservationId(observationObject, observation);
        setObservationStatus(observationObject, observation);
        setObservationStartTime(observationObject, observation);

        return observationObject;
    }

    private static void setObservationCoding(ObservationObject observationObject, Observation observation) {
        if (observation.hasCode()) {
            if (observation.getCode().hasCoding()) {
                Coding coding = observation.getCode().getCodingFirstRep();
                if (coding.hasCode()) {
                    observationObject.setObservationCode(coding.getCode());
                }
                if (coding.hasDisplay()) {
                    observationObject.setObservationDisplay(coding.getDisplay());
                }
            }
        }
    }

    private static void setObservationId(ObservationObject observationObject, Observation observation) {
        if (observation.hasIdentifier()) {
            Identifier identifier = observation.getIdentifierFirstRep();
            if (identifier.hasValue()) {
                observationObject.setObservationId(identifier.getValue());
            }
        }
    }

    private static void setObservationStatus(ObservationObject observationObject, Observation observation) throws FhirMappingException {
        if (observation.hasStatus()) {
            observationObject.setObservationStatus(getHL7Status(observation.getStatus().toString().toLowerCase()));
        }
    }

    private static void setObservationStartTime(ObservationObject observationObject, Observation observation) throws FhirMappingException {
        if (observation.hasEffectiveDateTimeType()) {
            observationObject.setObservationStartTime(formatDate(observation.getEffectiveDateTimeType().getValue()));
        }
        if (observation.hasEffectivePeriod()) {
            Period period = observation.getEffectivePeriod();
            if (period.hasStart()) {
                observationObject.setObservationStartTime(formatDate(period.getStart()));
            }
            if (period.hasEnd()) {
                List<EndTime> endTimeList = new ArrayList<>();
                EndTime endTime = new EndTime();
                endTime.setEndTime(DateUtil.formatDate(period.getEnd()));
                endTimeList.add(endTime);
                observationObject.setObservationEndTimeList(endTimeList);
            }
        }
    }

    private static String getHL7Status(String status) {
        switch (status) {
            case "entered-in-error":
                return "nullified";
            case "final":
                return "completed";
            default:
                throw new FhirMappingException("Observation status is not valid, can only be final or entered-in-error.");
        }
    }
}
