package uk.nhs.adaptors.scr.hl7tofhirmappers;

import static java.lang.Long.parseLong;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Period;
import org.hl7.fhir.r4.model.Type;

import uk.nhs.adaptors.scr.models.hl7models.ObservationObject;

public class ObservationMapper {
    public Observation mapObservation(ObservationObject observationObject){
        Observation observation = new Observation();

        observation.setIdentifier(getIdentifier(observationObject));

        observation.setCode(getCode(observationObject));

        observation.setStatus(getStatusCode(observationObject)); //done

        observation.setEffective(getDateTime(observationObject)); //done


        return observation;
    }

    private Type getDateTime(ObservationObject observationObject) {
        Period period = new Period();

        if (observationObject.getEffectiveTimeLow() != null){
            Date date = new Date();
            date.setTime(
                Long.parseLong(observationObject.getEffectiveTimeLow())
            );
            period.setStart(date);
        }

        if (observationObject.getEffectiveTimeHigh() != null){
            Date date = new Date();
            date.setTime(
                Long.parseLong(observationObject.getEffectiveTimeHigh())
            );
            period.setEnd(date);
        }

        if (period == null && observationObject.getCentreTimeStamp() != null){
            DateTimeType dateTimeType = new DateTimeType();
            Date date = new Date();
            date.setTime(Long.parseLong(observationObject.getCentreTimeStamp()));
            dateTimeType.setValue(date);
            return dateTimeType;
        }

        return period;
    }

    // to be completed from email confirmation
    private Observation.ObservationStatus getStatusCode(ObservationObject observationObject) {
        switch (observationObject.getStatusCode()){
            case "normal":
                return Observation.ObservationStatus.FINAL;
            case "nullified":
                return Observation.ObservationStatus.ENTEREDINERROR;
            case "active":
                return Observation.ObservationStatus.CORRECTED;
            case "completed":
                return Observation.ObservationStatus.FINAL;
            default:
                return Observation.ObservationStatus.NULL;
        }
    }

    private CodeableConcept getCode(ObservationObject observationObject) {
        CodeableConcept codeableConcept = new CodeableConcept();
        Coding coding = new Coding();

        if (observationObject.getFindingCode() != null){
            coding.setCode(observationObject.getFindingCode());
        }
        if (observationObject.getFindingCodeSystem() != null){
            coding.setSystem("http://snomed.info/sct");
        }
        if (observationObject.getFindingDisplay() != null) {
            coding.setDisplay(observationObject.getFindingDisplay());
        }

        return codeableConcept;
    }

    //may get list of identifiers, add for each..
    private List<Identifier> getIdentifier(ObservationObject observationObject) {
        List<Identifier> identifierList = new ArrayList<>();
        Identifier identifier = new Identifier();
        if (observationObject.getFindingID() != null){
            identifier.setValue(observationObject.getFindingID());
            identifierList.add(identifier);
        }

        return identifierList;
    }
}
