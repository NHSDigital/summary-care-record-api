package uk.nhs.adaptors.scr.mappings.from.hl7;

import lombok.RequiredArgsConstructor;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Practitioner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Node;
import uk.nhs.adaptors.scr.utils.XmlUtils;

import static uk.nhs.adaptors.scr.utils.FhirHelper.randomUUID;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
/**
 * Mapping a Person on the Spine Directory Service from HL7 to FHIR.
 */
public class PersonSdsMapper {

    private static final String PERSON_NAME_XPATH = "./name";
    private static final String SDS_USER_ID = "https://fhir.nhs.uk/Id/sds-user-id";
    private static final String ID_EXTENSION_XPATH = "./id/@extension";

    private final XmlUtils xmlUtils;

    public Practitioner mapPractitioner(Node personSds) {
        var practitioner = new Practitioner();
        practitioner.setId(randomUUID());
        practitioner.addIdentifier(new Identifier()
            .setSystem(SDS_USER_ID)
            .setValue(xmlUtils.getValueByXPath(personSds, ID_EXTENSION_XPATH)));

        var name = xmlUtils.getOptionalValueByXPath(personSds, PERSON_NAME_XPATH);
        if (name.isPresent()) {
            // Make the name human-readable.
            var nameStr = humanReadableName(name.get());

            practitioner.addName(new HumanName().setText(nameStr));
        }

        return practitioner;
    }

    /**
     * Replace extra spaces in names with single space, and remove line breaks.
     * @param nameWithSpaces
     * @return String
     */
    private String humanReadableName(String nameWithSpaces) {
        nameWithSpaces = nameWithSpaces.replaceAll("\\n", "");
        nameWithSpaces = nameWithSpaces.replaceAll("                            ", " ");
        return nameWithSpaces.trim();
    }
}
