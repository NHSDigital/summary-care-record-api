package uk.nhs.adaptors.scr.mappings.from.hl7;

import lombok.RequiredArgsConstructor;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.PractitionerRole;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Node;
import uk.nhs.adaptors.scr.utils.XmlUtils;

import java.util.ArrayList;
import java.util.List;

import static uk.nhs.adaptors.scr.utils.FhirHelper.randomUUID;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AgentPersonSdsMapper implements XmlToFhirMapper {

    private static final String PERSON_SDS_XPATH = "./agentPersonSDS";
    private static final String ID_EXTENSION_XPATH = "./id/@extension";
    private static final String SDS_ROLE_PROFILE_ID = "http://fhir.nhs.net/Id/sds-role-profile-id";

    private final PersonSdsMapper personSdsMapper;
    private final XmlUtils xmlUtils;

    @Override
    public List<? extends Resource> map(Node agentPersonSds) {
        List<Resource> resources = new ArrayList<>();

        PractitionerRole role = mapPractitionerRole(agentPersonSds);
        var personSds = xmlUtils.getNodeByXpath(agentPersonSds, PERSON_SDS_XPATH);
        var practitioner = personSdsMapper.mapPractitioner(personSds);
        role.setPractitioner(new Reference(practitioner));

        resources.add(role);
        resources.add(practitioner);

        return resources;
    }

    private PractitionerRole mapPractitionerRole(Node agentPersonSds) {
        var role = new PractitionerRole();
        var roleProfileId = xmlUtils.getValueByXPath(agentPersonSds, ID_EXTENSION_XPATH);
        role.setId(randomUUID());

        role.addIdentifier(new Identifier()
            .setSystem(SDS_ROLE_PROFILE_ID)
            .setValue(roleProfileId));
        return role;
    }
}
