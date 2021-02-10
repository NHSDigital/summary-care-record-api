package uk.nhs.adaptors.scr.mappings.from.hl7;

import lombok.RequiredArgsConstructor;
import org.hl7.fhir.r4.model.Device;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.PractitionerRole;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static uk.nhs.adaptors.scr.utils.FhirHelper.randomUUID;
import static uk.nhs.adaptors.scr.utils.XmlUtils.getOptionalNodeByXpath;
import static uk.nhs.adaptors.scr.utils.XmlUtils.getOptionalValueByXPath;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AgentDeviceMapper implements XmlToFhirMapper {

    private static final String ID_ROOT_XPATH = "./id/@root";
    private static final String AGENT_DEVICE_XPATH = "./agentDevice";
    private static final String AGENT_DEVICE_SDS_XPATH = "./agentDeviceSDS";
    private static final String ORG_SDS_XPATH = "./representedOrganizationSDS";
    private static final String ORG_XPATH = "./representedOrganization";

    private final DeviceMapper deviceMapper;
    private final DeviceSdsMapper deviceSdsMapper;
    private final OrganisationMapper organisationMapper;
    private final OrganisationSdsMapper organisationSdsMapper;

    @Override
    public List<? extends Resource> map(Node agentDevice) {
        List<Resource> resources = new ArrayList<>();

        var role = new PractitionerRole();
        role.setId(randomUUID());

        var device = mapDevice(agentDevice);
        var org = mapOrganization(agentDevice);
        device.setOwner(new Reference(org));
        role.setOrganization(new Reference(org));
        resources.add(device);
        resources.add(org);
        resources.add(role);
        return resources;
    }

    private Organization mapOrganization(Node agentDevice) {
        Organization org;
        Optional<Node> orgNode = getOptionalNodeByXpath(agentDevice, ORG_XPATH);
        Optional<Node> orgSdsNode = getOptionalNodeByXpath(agentDevice, ORG_SDS_XPATH);
        if (orgNode.isPresent()) {
            org = organisationMapper.mapOrganization(orgNode.get());
        } else if (orgSdsNode.isPresent()) {
            org = organisationSdsMapper.mapOrganizationSds(orgSdsNode.get());
        } else {
            org = new Organization();
            org.setId(randomUUID());
        }
        return org;
    }

    private Device mapDevice(Node agentDevice) {
        Device device;

        Optional<Node> deviceSds = getOptionalNodeByXpath(agentDevice, AGENT_DEVICE_SDS_XPATH);
        Optional<Node> deviceHl7 = getOptionalNodeByXpath(agentDevice, AGENT_DEVICE_XPATH);

        if (deviceSds.isPresent()) {
            device = deviceSdsMapper.mapDeviceSds(deviceSds.get());
        } else if (deviceHl7.isPresent()) {
            device = deviceMapper.mapDevice(deviceHl7.get());
        } else {
            device = new Device();
            device.setId(randomUUID());
        }

        getOptionalValueByXPath(agentDevice, ID_ROOT_XPATH)
            .ifPresent(id -> device.addIdentifier().setValue(id));

        return device;
    }
}
