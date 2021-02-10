package uk.nhs.adaptors.scr.models.xml;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AgentDevice {
    private String idRoot;
    private Device device;
    private DeviceSDS deviceSDS;
    private Organization organization;
    private OrganizationSDS organizationSDS;
}
