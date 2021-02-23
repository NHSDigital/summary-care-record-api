package uk.nhs.adaptors.scr.mappings.from.hl7;

import lombok.RequiredArgsConstructor;
import org.hl7.fhir.r4.model.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.List;

import static uk.nhs.adaptors.scr.utils.XmlUtils.getOptionalNodeByXpath;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ParticipantMapper implements XmlToFhirMapper {

    private static final String AGENT_PERSON_SDS_XPATH = "./UKCT_MT160018UK01.AgentPersonSDS";
    private static final String AGENT_PERSON_XPATH = "./UKCT_MT160018UK01.AgentPerson";
    private static final String AGENT_DEVICE_XPATH = "./UKCT_MT120601UK02.AgentDevice";
    private static final String NON_AGENT_ROLE_XPATH = "./participantNonAgentRole";

    private final AgentPersonSdsMapper agentPersonSdsMapper;
    private final AgentPersonMapper agentPersonMapper;
    private final NonAgentRoleMapper nonAgentRoleMapper;
    private final AgentDeviceMapper agentDeviceMapper;

    @Override
    public List<? extends Resource> map(Node informant) {
        List<Resource> resources = new ArrayList<>();

        getOptionalNodeByXpath(informant, NON_AGENT_ROLE_XPATH)
            .ifPresent(nonAgentRole -> resources.add(nonAgentRoleMapper.mapRelatedPerson(nonAgentRole)));

        getOptionalNodeByXpath(informant, AGENT_PERSON_SDS_XPATH)
            .ifPresent(agentPersonSds -> resources.addAll(agentPersonSdsMapper.map(agentPersonSds)));

        getOptionalNodeByXpath(informant, AGENT_PERSON_XPATH)
            .ifPresent(agentPerson -> resources.addAll(agentPersonMapper.map(agentPerson)));

        getOptionalNodeByXpath(informant, AGENT_DEVICE_XPATH)
            .ifPresent(agentDevice -> resources.addAll(agentDeviceMapper.map(agentDevice)));

        return resources;
    }
}
