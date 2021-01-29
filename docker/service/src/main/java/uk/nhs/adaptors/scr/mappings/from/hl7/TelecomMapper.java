package uk.nhs.adaptors.scr.mappings.from.hl7;

import org.hl7.fhir.r4.model.ContactPoint;
import org.hl7.fhir.r4.model.ContactPoint.ContactPointSystem;
import org.springframework.stereotype.Component;
import org.w3c.dom.Node;

import static uk.nhs.adaptors.scr.utils.XmlUtils.getNodeText;

@Component
public class TelecomMapper {

    private static final String TELECOM_USE_XPATH = "./@use";
    private static final String TELECOM_VALUE_XPATH = "./@value";

    ContactPoint mapTelecom(Node telecom) {
        return new ContactPoint()
            .setSystem(mapSystem(getNodeText(telecom, TELECOM_USE_XPATH)))
            .setValue(getNodeText(telecom, TELECOM_VALUE_XPATH));
    }

    private ContactPointSystem mapSystem(String telecomUse) {
        switch (telecomUse) {
            case "HP":
            case "WP":
            case "HV":
            case "MC":
            case "AS":
                return ContactPointSystem.PHONE;
            case "PG":
                return ContactPointSystem.PAGER;
            default:
                return ContactPointSystem.OTHER;

        }
    }

}
