package uk.nhs.adaptors.scr.controllers.validation.alert;

import lombok.RequiredArgsConstructor;
import org.hl7.fhir.r4.model.AuditEvent;
import org.hl7.fhir.r4.model.AuditEvent.AuditEventAgentComponent;
import org.hl7.fhir.r4.model.AuditEvent.AuditEventEntityComponent;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Identifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.adaptors.scr.components.FhirParser;
import uk.nhs.adaptors.scr.exceptions.FhirValidationException;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Date;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;
import static org.springframework.util.StringUtils.hasText;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AlertRequestValidator implements ConstraintValidator<AlertRequest, String> {

    private static final String EXTENSION_URL = "https://fhir.nhs.uk/StructureDefinition/Extension-SCR-NotificationMessage";
    private static final String TYPE_SYSTEM = "http://terminology.hl7.org/CodeSystem/iso-21089-lifecycle";
    private static final String SUBTYPE_SYSTEM = "https://fhir.nhs.uk/CodeSystem/SCR-AlertReason";
    private static final List<String> TYPE_CODES = asList("1", "2");
    private static final List<String> SUBTYPE_CODES = asList("1", "2", "3", "4", "5", "6");
    private static final String PATIENT_SYSTEM = "https://fhir.nhs.uk/Id/nhs-number";
    private static final String ORGANIZATION_SYSTEM = "https://fhir.nhs.uk/Id/ods-organization-code";
    private static final String PERSON_SYSTEM = "https://fhir.nhs.uk/Id/sds-user-id";

    private final FhirParser fhirParser;

    @Override
    public void initialize(AlertRequest contactNumber) {
    }

    @Override
    public boolean isValid(String alert, ConstraintValidatorContext context) {
        try {
            AuditEvent auditEvent = fhirParser.parseResource(alert, AuditEvent.class);
            checkId(auditEvent);
            checkExtension(auditEvent);
            checkType(auditEvent.getType());
            checkSubtype(auditEvent.getSubtype());
            checkRecorded(auditEvent.getRecorded());
            checkEntity(auditEvent.getEntity());
            checkPatient(auditEvent.getAgent());
            checkOrganization(auditEvent.getAgent());
            checkPerson(auditEvent.getAgent());
        } catch (FhirValidationException exc) {
            setErrorMessage(context, exc.getMessage());
            return false;
        }

        return true;
    }

    private void checkId(AuditEvent auditEvent) {
        checkNotEmpty(auditEvent.getId(), "'id' element is missing'");
    }

    private void checkPerson(List<AuditEventAgentComponent> agents) {
        AuditEventAgentComponent agent = checkAgent(agents, PERSON_SYSTEM);

        if (agent.getRole().isEmpty()) {
            throw new FhirValidationException("Expecting at least one 'role' for 'agent' entry with system " + PERSON_SYSTEM);
        } else {
            agent.getRole().stream()
                .forEach(role -> {
                    if (!role.hasText() && role.getCodingFirstRep().isEmpty()) {
                        throw new FhirValidationException("Expecting at least one non empty 'role' for 'agent' entry with system "
                            + PERSON_SYSTEM);
                    }
                });
        }
    }

    private void checkPatient(List<AuditEventAgentComponent> agents) {
        checkAgent(agents, PATIENT_SYSTEM);
    }

    private void checkOrganization(List<AuditEventAgentComponent> agents) {
        checkAgent(agents, ORGANIZATION_SYSTEM);
    }

    private AuditEventAgentComponent checkAgent(List<AuditEventAgentComponent> agents, String fhirSystem) {
        return agents.stream()
            .filter(it -> {
                Identifier id = it.getWho().getIdentifier();
                return fhirSystem.equals(id.getSystem()) && id.hasValue();
            })
            .peek(it -> {
                if (!it.hasRequestor() || it.getRequestor()) {
                    throw new FhirValidationException(String.format("Missing or unsupported 'requestor' value for 'agent' with "
                        + "'who.identifier.system' '%s'", fhirSystem));
                }
            })
            .reduce((x, y) -> {
                throw new FhirValidationException(String.format("Expecting exactly one 'agent' entry with 'who.identifier.system' '%s' "
                    + "and non-empty 'value'", fhirSystem));
            })
            .orElseThrow(() -> {
                throw new FhirValidationException(String.format("Missing 'agent' entry with 'who.identifier.system' '%s' "
                    + "and non-empty 'value'", fhirSystem));
            });
    }

    private void checkEntity(List<AuditEventEntityComponent> entities) {
        if (entities.size() != 1) {
            throw new FhirValidationException("Expecting exactly one 'entity' element");
        }
        AuditEventEntityComponent entity = entities.get(0);
        if (!entity.getWhat().getIdentifier().hasValue()) {
            throw new FhirValidationException("Invalid or missing value in field 'entity.what.identifier.value'");
        }
    }

    private void checkRecorded(Date recorded) {
        if (recorded == null) {
            throw new FhirValidationException("'recorded' element missing");
        }
    }

    private void checkSubtype(List<Coding> subtypes) {
        if (subtypes.size() != 1) {
            throw new FhirValidationException("Expecting exactly one 'subtype' element");
        }
        Coding subtype = subtypes.get(0);
        if (!SUBTYPE_SYSTEM.equals(subtype.getSystem())) {
            throw new FhirValidationException("Invalid or missing value in field 'subtype.system'. Supported value is: "
                + SUBTYPE_SYSTEM);
        }
        if (!SUBTYPE_CODES.contains(subtype.getCode())) {
            throw new FhirValidationException("Invalid or missing value in field 'subtype.code'. Supported values are: "
                + SUBTYPE_CODES.stream().collect(joining(", ")));
        }

        checkNotEmpty(subtype.getDisplay(), "Missing value 'subtype.display'");
    }

    private void checkType(Coding type) {
        if (!TYPE_SYSTEM.equals(type.getSystem())) {
            throw new FhirValidationException(String.format("'Type' element for '%s' system missing", TYPE_SYSTEM));
        }
        if (!TYPE_CODES.contains(type.getCode())) {
            throw new FhirValidationException("Invalid or missing value in field 'type.code'. Supported values are: "
                + TYPE_CODES.stream().collect(joining(", ")));
        }

        checkNotEmpty(type.getDisplay(), "Missing value 'type.display'");
    }

    private void checkExtension(AuditEvent auditEvent) {
        List<Extension> extensions = auditEvent.getExtensionsByUrl(EXTENSION_URL);
        if (extensions.size() != 1) {
            throw new FhirValidationException(String.format("Expecting exactly one 'extension' element with URL: '%s'",
                EXTENSION_URL));
        }
        if (!extensions.get(0).hasValue()) {
            throw new FhirValidationException(String.format("'valueString' for 'extension' '%s' is missing", EXTENSION_URL));
        }
    }

    private void setErrorMessage(ConstraintValidatorContext cxt, String message) {
        cxt.disableDefaultConstraintViolation();
        cxt.buildConstraintViolationWithTemplate(message)
            .addConstraintViolation();
    }

    private void checkNotEmpty(String value, String s) {
        if (!hasText(value)) {
            throw new FhirValidationException(s);
        }
    }
}
