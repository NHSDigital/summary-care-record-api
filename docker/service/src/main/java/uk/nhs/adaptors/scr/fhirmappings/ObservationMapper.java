package uk.nhs.adaptors.scr.fhirmappings;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

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

public class ObservationMapper {

    public static void mapObservations(GpSummary gpSummary, List<Resource> observations) throws FhirMappingException {
        List<ObservationObject> observationList = new ArrayList<>();
        for (Resource observation : observations) {
            observationList.add(getObservationObject((Observation) observation));
        }

        gpSummary.setObservationList(observationList);
    }

    private static ObservationObject getObservationObject(Observation observation) throws FhirMappingException {
        ObservationObject observationObject = new ObservationObject();

        getObservationCoding(observationObject, observation);
        getObservationId(observationObject, observation);
        getObservationStatus(observationObject, observation);
        getObservationStartTime(observationObject, observation);

        return observationObject;
    }

    private static void getObservationCoding(ObservationObject observationObject, Observation observation) {
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

    private static void getObservationId(ObservationObject observationObject, Observation observation) {
        if (observation.hasIdentifier()) {
            Identifier identifier = observation.getIdentifierFirstRep();
            if (identifier.hasValue()) {
                observationObject.setObservationId(identifier.getValue());
            }
        }
    }

    private static void getObservationStatus(ObservationObject observationObject, Observation observation) throws FhirMappingException {
        if (observation.hasStatus()) {
            observationObject.setObservationStatus(getHL7Status(observation.getStatus().toString().toLowerCase()));
        }
    }

    private static void getObservationStartTime(ObservationObject observationObject, Observation observation) throws FhirMappingException {
        if (observation.hasEffectiveDateTimeType()) {
            String strDate = observation.getEffectiveDateTimeType().getValueAsString();
            observationObject.setObservationStartTime(DateUtil.formatDateFhirToHl7(strDate));
        }
        if (observation.hasEffectivePeriod()) {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            Period period = observation.getEffectivePeriod();
            if (period.hasStart()) {
                String strDate = dateFormat.format(period.getStart());
                observationObject.setObservationStartTime(DateUtil.formatDate(strDate));
            }
            if (period.hasEnd()) {
                String strDate = dateFormat.format(period.getEnd());
                List<EndTime> endTimeList = new ArrayList<>();
                EndTime endTime = new EndTime();
                endTime.setEndTime(DateUtil.formatDate(strDate));
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
