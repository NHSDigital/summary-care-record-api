package uk.nhs.adaptors.scr.mappings.from.hl7;

import org.hl7.fhir.r4.model.Device;
import org.hl7.fhir.r4.model.Identifier;
import org.springframework.stereotype.Component;
import org.w3c.dom.Node;

import static uk.nhs.adaptors.scr.utils.FhirHelper.randomUUID;
import static uk.nhs.adaptors.scr.utils.XmlUtils.getValueByXPath;

@Component
public class DeviceSdsMapper {

    private static final String SDS_DEVICE_SYSTEM = "https://fhir.nhs.uk/Id/SDSDevice";
    private static final String ID_EXTENSION_XPATH = "./id/@extension";

    public Device mapDeviceSds(Node deviceSdsHl7) {
        Device deviceFhir = new Device();
        deviceFhir.setId(randomUUID());
        deviceFhir.addIdentifier(new Identifier()
            .setSystem(SDS_DEVICE_SYSTEM)
            .setValue(getValueByXPath(deviceSdsHl7, ID_EXTENSION_XPATH))
        );

        return deviceFhir;
    }
}
