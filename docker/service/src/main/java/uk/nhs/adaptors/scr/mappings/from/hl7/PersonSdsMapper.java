package uk.nhs.adaptors.scr.mappings.from.hl7;

import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Practitioner;
import org.springframework.stereotype.Component;
import org.w3c.dom.Node;

import static uk.nhs.adaptors.scr.utils.FhirHelper.randomUUID;
import static uk.nhs.adaptors.scr.utils.XmlUtils.getOptionalValueByXPath;
import static uk.nhs.adaptors.scr.utils.XmlUtils.getValueByXPath;

@Component
public class PersonSdsMapper {

    private static final String PERSON_NAME_XPATH = "./name";
    private static final String SDS_USER_ID = "https://fhir.nhs.uk/Id/sds-user-id";
    private static final String ID_EXTENSION_XPATH = "./id/@extension";

    public Practitioner mapPractitioner(Node personSds) {
        var practitioner = new Practitioner();
        practitioner.setId(randomUUID());
        practitioner.addIdentifier(new Identifier()
            .setSystem(SDS_USER_ID)
            .setValue(getValueByXPath(personSds, ID_EXTENSION_XPATH)));

        var name = getOptionalValueByXPath(personSds, PERSON_NAME_XPATH);
        if (name.isPresent()) {
            practitioner.addName(new HumanName().setText(name.get()));
        }

        return practitioner;
    }
}
