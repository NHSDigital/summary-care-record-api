package uk.nhs.adaptors.scr.hl7tofhirmappers;

import static uk.nhs.adaptors.scr.utils.DateUtil.parseDateHL7ToFhir;

import org.hl7.fhir.r4.model.Attachment;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DocumentReference;
import org.hl7.fhir.r4.model.DocumentReference.DocumentReferenceContentComponent;
import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.InstantType;
import org.hl7.fhir.r4.model.Reference;

import lombok.AllArgsConstructor;
import uk.nhs.adaptors.scr.models.hl7models.DocumentReferenceObject;

@AllArgsConstructor
public class DocumentReferenceMapper {
    private final PatientMapper patientMapper;

    public DocumentReference mapDocumentReference(DocumentReferenceObject documentReferenceObject) {
        DocumentReference documentReference = new DocumentReference();

        documentReference.setDocStatus(DocumentReference.ReferredDocumentStatus.NULL);
        documentReference.setId(documentReferenceObject.getEventID());
        documentReference.addContent(addContent(documentReferenceObject));
        documentReference.setMasterIdentifier(addIdentifier(documentReferenceObject));
        documentReference.setType(getType(documentReferenceObject));
        documentReference.setStatus(Enumerations.DocumentReferenceStatus.CURRENT); // TODO: is this hardcoded?

        if (documentReferenceObject.getPatientObject() != null) {
            documentReference.setSubject(new Reference(patientMapper.mapPatient(documentReferenceObject.getPatientObject())));
        }

        documentReference.addCategory(new CodeableConcept().setText(documentReferenceObject.getMessageCategory()));
        documentReference.setDateElement(InstantType.now()); //TODO: issue in response mapping this is null, throwing error for instant type
        documentReference.addRelatesTo(new DocumentReference.DocumentReferenceRelatesToComponent().setCode(DocumentReference.DocumentRelationshipType.NULL));

        return documentReference;
    }

    private DocumentReferenceContentComponent addContent(DocumentReferenceObject documentReferenceObject) {
        DocumentReferenceContentComponent documentReferenceContent = new DocumentReferenceContentComponent();

        Attachment attachment = new Attachment();

        attachment.setContentType("application/fhir+json"); // TODO: check how to get Mimetype of any in-line data
        // attachment.setData(new byte[]); // TODO: add all hl7 data converted to fhir in 64 byte
        attachment.setTitle(documentReferenceObject.getMessageType());
        attachment.setCreation(parseDateHL7ToFhir(documentReferenceObject.getDateDocumentCreated()));

        documentReferenceContent.setAttachment(attachment);

        return documentReferenceContent;
    }

    private Identifier addIdentifier(DocumentReferenceObject documentReferenceObject) {
        Identifier identifier = new Identifier();

        identifier.setSystem("https://fhir.nhs.uk/Id/nhsSCRUUID");//TODO: is this hardcoded or where do we get this value
        identifier.setValue(documentReferenceObject.getLatestSCRUuid()); // TODO: parse latest scr id get

        return identifier;
    }

    private CodeableConcept getType(DocumentReferenceObject documentReferenceObject) {
        CodeableConcept codeableConcept = new CodeableConcept();

        codeableConcept.addCoding(
            new Coding()
                .setSystem("http://snomed.info/sct") //TODO: find where this url comes from for parser
                .setCode("")
                .setDisplay(""));

        return codeableConcept;
    }

    public DocumentReference mapDocumentReferenceMinimal() {
        DocumentReference documentReference = new DocumentReference();

        return documentReference;
    }
}
