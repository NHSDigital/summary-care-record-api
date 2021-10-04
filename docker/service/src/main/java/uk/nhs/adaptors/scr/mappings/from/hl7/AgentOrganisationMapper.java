package uk.nhs.adaptors.scr.mappings.from.hl7;

import static java.util.stream.Collectors.toList;

import static uk.nhs.adaptors.scr.utils.FhirHelper.randomUUID;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.ContactPoint;
import org.hl7.fhir.r4.model.Organization;
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
public class AgentOrganisationMapper implements XmlToFhirMapper {

    private static final String CODE_XPATH = "./code/@code";
    private static final String CODE_DISPLAY_XPATH = "./code/@displayName";
    private static final String ADDRESS_XPATH = "./addr";
    private static final String TELECOM_XPATH = "./telecom";
    private static final String ORG_XPATH = "./agentOrganization";
    private static final String JOB_ROLE_NAME_SYSTEM = "https://fhir.nhs.uk/CodeSystem/HL7v3-SDSJobRoleName";


    private final TelecomMapper telecomMapper;
    private final OrganisationMapper organisationMapper;
    private final XmlUtils xmlUtils;

    @Override
    public List<? extends Resource> map(Node agentOrganisation) {
        List<Resource> resources = new ArrayList<>();
        var role = mapPractitionerRole(agentOrganisation);
        var org = mapOrganization(agentOrganisation);
        role.setOrganization(new Reference(org));

        resources.add(role);
        resources.add(org);

        return resources;
    }

    private PractitionerRole mapPractitionerRole(Node agentOrganisation) {
        var code = xmlUtils.getValueByXPath(agentOrganisation, CODE_XPATH);
        var display = xmlUtils.getValueByXPath(agentOrganisation, CODE_DISPLAY_XPATH);
        var role = new PractitionerRole()
            .addCode(new CodeableConcept(
                new Coding()
                    .setSystem(JOB_ROLE_NAME_SYSTEM)
                    .setCode(code)
                    .setDisplay(display)));
        role.setId(randomUUID());
        return role;
    }

    private List<ContactPoint> mapContactPoints(Node agentOrganisation) {
        return xmlUtils.getNodesByXPath(agentOrganisation, TELECOM_XPATH)
            .stream()
            .map(telecomMapper::mapTelecom)
            .collect(toList());
    }

    private Organization mapOrganization(Node agentOrganisation) {
        Organization org;
        Optional<Node> orgNode = xmlUtils.detachOptionalNodeByXPath(agentOrganisation, ORG_XPATH);
        if (orgNode.isPresent()) {
            org = organisationMapper.mapOrganization(orgNode.get());
        } else {
            org = new Organization();
            org.setId(randomUUID());
        }
        org.setTelecom(mapContactPoints(agentOrganisation));

        xmlUtils.getOptionalValueByXPath(agentOrganisation, ADDRESS_XPATH)
            .ifPresent(val -> org.addAddress(new Address().setText(val)));

        return org;
    }
}
