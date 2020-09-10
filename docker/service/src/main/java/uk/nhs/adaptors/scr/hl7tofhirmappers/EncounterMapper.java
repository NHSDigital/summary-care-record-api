package uk.nhs.adaptors.scr.hl7tofhirmappers;

import java.util.Date;
import java.util.List;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.Encounter.EncounterParticipantComponent;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Period;

import uk.nhs.adaptors.scr.models.hl7models.EncounterObject;

public class EncounterMapper {
    public Encounter mapEncounter(EncounterObject encounterObject){
        Encounter encounter = new Encounter();

        encounter.addParticipant(getParticipantAuthor(encounterObject));

        return encounter;
    }

    private EncounterParticipantComponent getParticipantPerformer(EncounterObject encounterObject) {
        EncounterParticipantComponent participantComponent = new EncounterParticipantComponent();

        if (encounterObject.getPerformerTypeCode() != null){
            CodeableConcept codeableConcept = new CodeableConcept();
            Coding coding = new Coding();
            coding.setCode(encounterObject.getPerformerTypeCode());
            codeableConcept.addCoding(coding);
            participantComponent.addType(codeableConcept);
        }

        if (encounterObject.getPerformerTime() != null) {
            Period period = new Period();
            Date date = new Date();
            date.setTime(Long.parseLong(encounterObject.getPerformerTime()));
            period.setStart(date);
            participantComponent.setPeriod(period);
        }

        if (encounterObject.getPerformerModeCode() != null && encounterObject.getPerformerModeDisplay() != null){
            CodeableConcept codeableConcept = new CodeableConcept();
            Coding coding = new Coding();
            coding.setCode(encounterObject.getPerformerModeCode());
            coding.setDisplay(encounterObject.getPerformerModeDisplay());
            codeableConcept.addCoding(coding);
            participantComponent.setExtension((List<Extension>) new Extension().castToCodeableConcept(codeableConcept));
        }

        return participantComponent;
    }

    private EncounterParticipantComponent getParticipantAuthor(EncounterObject encounterObject) {
        EncounterParticipantComponent participantComponent = new EncounterParticipantComponent();

        if (encounterObject.getAuthorTypeCode() != null){
            CodeableConcept codeableConcept = new CodeableConcept();
            Coding coding = new Coding();
            coding.setCode(encounterObject.getAuthorTypeCode());
            coding.setSystem("http://terminology.hl7.org/CodeSystem/v3-ParticipationType");
            coding.setDisplay("author");
            codeableConcept.addCoding(coding);
            participantComponent.addType(codeableConcept);
        }

        if (encounterObject.getAuthorTime() != null) {
            Period period = new Period();
            Date date = new Date();
            date.setTime(Long.parseLong(encounterObject.getAuthorTime()));
            period.setStart(date);
            participantComponent.setPeriod(period);
        }

        return participantComponent;
    }

    private EncounterParticipantComponent getParticipantInformant(EncounterObject encounterObject) {
        EncounterParticipantComponent participantComponent = new EncounterParticipantComponent();

        if (encounterObject.getInformantTypeCode() != null){
            CodeableConcept codeableConcept = new CodeableConcept();
            Coding coding = new Coding();
            coding.setCode(encounterObject.getInformantTypeCode());
            coding.setSystem("http://terminology.hl7.org/CodeSystem/v3-ParticipationType");
            coding.setDisplay("informant");
            codeableConcept.addCoding(coding);
            participantComponent.addType(codeableConcept);
        }

        if (encounterObject.getInformantTime() != null) {
            Period period = new Period();
            Date date = new Date();
            date.setTime(Long.parseLong(encounterObject.getInformantTime()));
            period.setStart(date);
            participantComponent.setPeriod(period);
        }

        return participantComponent;
    }
}
