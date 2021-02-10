package uk.nhs.adaptors.scr.models.xml;

import lombok.Getter;
import lombok.Setter;
import org.hl7.fhir.r4.model.ContactPoint;

import java.util.List;

@Getter
@Setter
public class AgentPerson {
    private String codeCode;
    private String codeDisplayName;
    private String address;
    private List<Telecom> telecom;
    private Person agentPerson;
    private Organization representedOrganization;
    private OrganizationSDS representedOrganizationSDS;

    @Getter
    @Setter
    public static class Telecom {
        private String use;
        private String value;

        public static String mapUse(ContactPoint.ContactPointUse contactPointUse) {
            if (contactPointUse == null) {
                return "HV";
            }

            // https://data.developer.nhs.uk/dms/mim/6.3.01/Data%20Types/DataTypes.htm#TEL
            switch (contactPointUse) {
                case HOME:
                    return "HP";
                case WORK:
                    return "WP";
                case MOBILE:
                    return "MC";
                case TEMP:
                case OLD:
                case NULL:
                    return "HV";
                default:
                    throw new IllegalArgumentException();
            }
        }
    }
}
