package uk.nhs.adaptors.scr.mappings.from.hl7;

import lombok.RequiredArgsConstructor;
import org.hl7.fhir.r4.model.ContactPoint;
import org.hl7.fhir.r4.model.ContactPoint.ContactPointSystem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Node;
import uk.nhs.adaptors.scr.utils.XmlUtils;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TelecomMapper {

    private static final String TELECOM_USE_XPATH = "./@use";
    private static final String TELECOM_VALUE_XPATH = "./@value";

    private final XmlUtils xmlUtils;

    public ContactPoint mapTelecom(Node telecom) {
        return new ContactPoint()
            .setSystem(mapSystem(xmlUtils.getNodeText(telecom, TELECOM_USE_XPATH)))
            .setValue(xmlUtils.getNodeText(telecom, TELECOM_VALUE_XPATH));
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
