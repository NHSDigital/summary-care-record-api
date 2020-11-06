package uk.nhs.adaptors.scr.exceptions;

import org.hl7.fhir.r4.model.Coding;

public enum NHSCodings {
    ACCESS_DENIED("Access has been denied to process this request"),
    ACCESS_DENIED_SSL("SSL Protocol or Cipher requirements not met"),
    ASID_CHECK_FAILED("The sender or receiver's ASID is not authorised for this interaction"),
    AUTHOR_CREDENTIALS_ERROR("Author credentials error"),
    BAD_REQUEST("Bad request"),
    CONFLICTING_VALUES("Conflicting values have been specified in different fields"),
    DUPLICATE_REJECTED("Create would lead to creation of a duplicate resource"),
    FHIR_CONSTRAINT_VIOLATION("FHIR constraint violated"),
    FLAG_ALREADY_SET("Flag value was already set"),
    INTERNAL_SERVER_ERROR("Unexpected internal server error"),
    INVALID_CODE_SYSTEM("Invalid code system"),
    INVALID_CODE_VALUE("Invalid code value"),
    INVALID_ELEMENT("Invalid element"),
    INVALID_IDENTIFIER_SYSTEM("Invalid identifier system"),
    INVALID_IDENTIFIER_VALUE("Invalid identifier value"),
    INVALID_NHS_NUMBER("Invalid NHS number"),
    INVALID_PARAMETER("Invalid parameter"),
    INVALID_PATIENT_DEMOGRAPHICS("Invalid patient demographics"),
    INVALID_REQUEST_MESSAGE("Invalid request message"),
    INVALID_REQUEST_STATE("The request exists but is not in an appropriate state for the call to succeed"),
    INVALID_REQUEST_TYPE("The type of request is not supported by the API call"),
    INVALID_RESOURCE("Invalid validation of resource"),
    INVALID_VALUE("An input field has an invalid value for its type"),
    MESSAGE_NOT_WELL_FORMED("Message not well formed"),
    MISSING_OR_INVALID_HEADER("There is a required header missing or invalid"),
    MSG_RESOURCE_ID_FAIL("Client is not permitted to assign an id"),
    NOT_IMPLEMENTED("Not implemented"),
    NO_ORGANISATIONAL_CONSENT("Organisation has not provided consent to share data"),
    NO_PATIENT_CONSENT("Patient has not provided consent to share data"),
    NO_RECORD_FOUND("No record found"),
    NO_RELATIONSHIP("No legitimate relationship exists with this patient"),
    ORGANISATION_NOT_FOUND("Organisation not found"),
    PATIENT_NOT_FOUND("Patient not found"),
    PATIENT_SENSITIVE("Patient sensitive"),
    PRACTITIONER_NOT_FOUND("Practitioner not found"),
    REFERENCE_NOT_FOUND("Reference not found"),
    REQUEST_UNMATCHED("Request does not match authorisation token"),
    RESOURCE_CREATED("New resource created"),
    RESOURCE_DELETED("Resource removed"),
    RESOURCE_UPDATED("Resource has been successfully updated");

    private static final String SYSTEM = "https://fhir.nhs.uk/STU3/ValueSet/Spine-ErrorOrWarningCode-1";
    private final String display;

    NHSCodings(String display) {
        this.display = display;
    }

    public Coding asCoding() {
        return new Coding()
            .setSystem(SYSTEM)
            .setCode(this.name())
            .setDisplay(this.display);
    }
}
