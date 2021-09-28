package uk.nhs.adaptors.scr.mappings.from.hl7;

import static uk.nhs.adaptors.scr.utils.FhirHelper.randomUUID;

import java.util.ArrayList;
import java.util.List;

import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.PractitionerRole;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Node;

import lombok.RequiredArgsConstructor;
import uk.nhs.adaptors.scr.utils.XmlUtils;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AgentOrganisationSdsMapper implements XmlToFhirMapper {

    private static final String ID_EXTENSION_XPATH = "./id/@extension";
    private static final String SDS_ROLE_PROFILE_ID = "http://fhir.nhs.net/Id/sds-role-profile-id";

    private final OrganisationSdsMapper organisationSdsMapper;
    private final XmlUtils xmlUtils;

    @Override
    public List<? extends Resource> map(Node agentOrganisationSds) {
        List<Resource> resources = new ArrayList<>();

        var organisation = organisationSdsMapper.mapOrganizationSds(agentOrganisationSds);
        PractitionerRole role = mapPractitionerRole(agentOrganisationSds);
        role.setOrganization(new Reference(organisation));

        resources.add(role);
        resources.add(organisation);

        return resources;
    }

    private PractitionerRole mapPractitionerRole(Node agentOrganisationSds) {
        var role = new PractitionerRole();
        var roleProfileId = xmlUtils.getValueByXPath(agentOrganisationSds, ID_EXTENSION_XPATH);
        role.setId(randomUUID());

        role.addIdentifier(new Identifier()
            .setSystem(SDS_ROLE_PROFILE_ID)
            .setValue(roleProfileId));
        return role;
    }
}
