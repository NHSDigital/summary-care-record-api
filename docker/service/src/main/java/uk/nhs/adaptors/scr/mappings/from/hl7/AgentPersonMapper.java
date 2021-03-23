package uk.nhs.adaptors.scr.mappings.from.hl7;

import lombok.RequiredArgsConstructor;
import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.ContactPoint;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.PractitionerRole;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Node;
import uk.nhs.adaptors.scr.utils.XmlUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;
import static uk.nhs.adaptors.scr.utils.FhirHelper.randomUUID;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AgentPersonMapper implements XmlToFhirMapper {

    private static final String CODE_XPATH = "./code/@code";
    private static final String CODE_DISPLAY_XPATH = "./code/@displayName";
    private static final String ADDRESS_XPATH = "./addr";
    private static final String TELECOM_XPATH = "./telecom";
    private static final String ORG_XPATH = "./representedOrganization";
    private static final String PERSON_XPATH = "./agentPerson";
    private static final String PERSON_SDS_XPATH = "./representedPersonSDS";
    private static final String PERSON_NAME_XPATH = "./name";
    private static final String ORG_SDS_XPATH = "./representedOrganizationSDS";
    private static final String JOB_ROLE_NAME_SYSTEM = "https://fhir.nhs.uk/CodeSystem/HL7v3-SDSJobRoleName";


    private final TelecomMapper telecomMapper;
    private final PersonSdsMapper personSdsMapper;
    private final OrganisationMapper organisationMapper;
    private final OrganisationSdsMapper organisationSdsMapper;
    private final XmlUtils xmlUtils;

    @Override
    public List<? extends Resource> map(Node agentPerson) {
        List<Resource> resources = new ArrayList<>();
        var role = mapPractitionerRole(agentPerson);
        var org = mapOrganization(agentPerson);
        role.setOrganization(new Reference(org));
        mapPerson(agentPerson, role, resources);
        mapPersonSds(agentPerson, role, resources);

        resources.add(role);
        resources.add(org);

        return resources;
    }

    private void mapPersonSds(Node agentPerson, PractitionerRole role, List<Resource> resources) {
        xmlUtils.getOptionalNodeByXpath(agentPerson, PERSON_SDS_XPATH)
            .ifPresent(personSds -> {
                var practitioner = personSdsMapper.mapPractitioner(personSds);
                role.setPractitioner(new Reference(practitioner));
                resources.add(practitioner);
            });
    }

    private void mapPerson(Node agentPerson, PractitionerRole role, List<Resource> resources) {
        xmlUtils.getOptionalNodeByXpath(agentPerson, PERSON_XPATH)
            .ifPresent(person -> {
                var practitioner = new Practitioner();
                practitioner.setId(randomUUID());
                practitioner.addName().setText(xmlUtils.getNodeText(person, PERSON_NAME_XPATH));
                role.setPractitioner(new Reference(practitioner));
                resources.add(practitioner);
            });
    }

    private PractitionerRole mapPractitionerRole(Node agentPerson) {
        var code = xmlUtils.getValueByXPath(agentPerson, CODE_XPATH);
        var display = xmlUtils.getValueByXPath(agentPerson, CODE_DISPLAY_XPATH);
        var role = new PractitionerRole()
            .addCode(new CodeableConcept(
                new Coding()
                    .setSystem(JOB_ROLE_NAME_SYSTEM)
                    .setCode(code)
                    .setDisplay(display)));
        role.setId(randomUUID());
        return role;
    }

    private List<ContactPoint> mapContactPoints(Node agentPerson) {
        return xmlUtils.getNodesByXPath(agentPerson, TELECOM_XPATH)
            .stream()
            .map(telecomMapper::mapTelecom)
            .collect(toList());
    }

    private Organization mapOrganization(Node agentPerson) {
        Organization org;
        Optional<Node> orgNode = xmlUtils.getOptionalNodeByXpath(agentPerson, ORG_XPATH);
        Optional<Node> orgSdsNode = xmlUtils.getOptionalNodeByXpath(agentPerson, ORG_SDS_XPATH);
        if (orgNode.isPresent()) {
            org = organisationMapper.mapOrganization(orgNode.get());
        } else if (orgSdsNode.isPresent()) {
            org = organisationSdsMapper.mapOrganizationSds(orgSdsNode.get());
        } else {
            org = new Organization();
            org.setId(randomUUID());
        }

        org.setTelecom(mapContactPoints(agentPerson));

        xmlUtils.getOptionalValueByXPath(agentPerson, ADDRESS_XPATH)
            .ifPresent(val -> org.addAddress(new Address().addLine(val)));

        return org;
    }

}
