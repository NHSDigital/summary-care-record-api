package uk.nhs.adaptors.scr.models.hl7models;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeviceObject {
    private List<String> deviceVersionList;
    private String manufacturerModelName;
    private String deviceName;
    private List<String> descList;
    private String deviceCode;
    private String deviceSDSIdExtension;
    private String agentDeviceIDRoot;
    private String agentDeviceIDExtension;
    private String deviceIdExtension;
}
