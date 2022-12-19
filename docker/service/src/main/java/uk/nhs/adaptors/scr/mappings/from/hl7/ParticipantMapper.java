package uk.nhs.adaptors.scr.mappings.from.hl7;

import lombok.RequiredArgsConstructor;
import org.hl7.fhir.r4.model.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Node;
import uk.nhs.adaptors.scr.utils.XmlUtils;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ParticipantMapper implements XmlToFhirMapper {

    private static final String AGENT_DEVICE_XPATH = "./UKCT_MT120601UK02.AgentDevice";
    private static final String AGENT_PERSON_SDS_XPATH = "./UKCT_MT160018UK01.AgentPersonSDS";
    private static final String AGENT_PERSON_XPATH = "./UKCT_MT160018UK01.AgentPerson";
    private static final String NON_AGENT_ROLE_XPATH = "./participantNonAgentRole";

    private final AgentDeviceMapper agentDeviceMapper;
    private final AgentPersonSdsMapper agentPersonSdsMapper;
    private final AgentPersonMapper agentPersonMapper;
    private final NonAgentRoleMapper nonAgentRoleMapper;
    private final XmlUtils xmlUtils;

    @Override
    public List<? extends Resource> map(Node informant) {
        List<Resource> resources = new ArrayList<>();

        xmlUtils.detachOptionalNodeByXPath(informant, AGENT_DEVICE_XPATH)
            .ifPresent(agentDevice -> resources.addAll(agentDeviceMapper.map(agentDevice)));

        xmlUtils.detachOptionalNodeByXPath(informant, AGENT_PERSON_SDS_XPATH)
            .ifPresent(agentPersonSds -> resources.addAll(agentPersonSdsMapper.map(agentPersonSds)));

        xmlUtils.detachOptionalNodeByXPath(informant, AGENT_PERSON_XPATH)
            .ifPresent(agentPerson -> resources.addAll(agentPersonMapper.map(agentPerson)));

        xmlUtils.detachOptionalNodeByXPath(informant, NON_AGENT_ROLE_XPATH)
            .ifPresent(nonAgentRole -> resources.add(nonAgentRoleMapper.mapRelatedPerson(nonAgentRole)));

        return resources;
    }
}
