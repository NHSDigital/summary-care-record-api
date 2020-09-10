package uk.nhs.adaptors.scr.models.hl7models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DocumentReferenceObject {
    private String nhsNumber;
    private String eventID;
    private String dateDocumentCreated;
    private String messageCategory;
    private String eventStatus;
    private String messageType;
    private String latestSCRUuid;
    private PatientObject patientObject;
}
