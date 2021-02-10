package uk.nhs.adaptors.scr.mappings.from.hl7;

import org.hl7.fhir.r4.model.Annotation;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Device;
import org.hl7.fhir.r4.model.Device.DeviceDeviceNameComponent;
import org.hl7.fhir.r4.model.Device.DeviceNameType;
import org.hl7.fhir.r4.model.Device.DeviceVersionComponent;
import org.springframework.stereotype.Component;
import org.w3c.dom.Node;

import static org.hl7.fhir.r4.model.Device.DeviceNameType.MANUFACTURERNAME;
import static org.hl7.fhir.r4.model.Device.DeviceNameType.OTHER;
import static uk.nhs.adaptors.scr.utils.FhirHelper.randomUUID;
import static uk.nhs.adaptors.scr.utils.XmlUtils.getOptionalNodeByXpath;
import static uk.nhs.adaptors.scr.utils.XmlUtils.getOptionalValueByXPath;
import static uk.nhs.adaptors.scr.utils.XmlUtils.getValueByXPath;

@Component
public class DeviceMapper {

    private static final String ID_EXTENSION_XPATH = "./id/@extension";
    private static final String NAME_XPATH = "./name";
    private static final String MANUFACTURER_MODE_NAME_XPATH = "./manufacturerModelName";
    private static final String DESCRIPTION_XPATH = "./desc";
    private static final String SOFTWARE_NAME_XPATH = "./softwareName";
    private static final String CODE_ELEMENT_XPATH = "./code";
    private static final String CODE_ATTRIBUTE_XPATH = "./@code";
    private static final String CODE_DISPLAY_XPATH = "./@displayName";

    public Device mapDevice(Node deviceHl7) {
        Device deviceFhir = new Device();
        deviceFhir.setId(randomUUID());

        getOptionalValueByXPath(deviceHl7, ID_EXTENSION_XPATH)
            .ifPresent(id -> deviceFhir.addIdentifier()
                .setValue(id)
            );

        getOptionalNodeByXpath(deviceHl7, CODE_ELEMENT_XPATH)
            .ifPresent(code -> deviceFhir.setType(new CodeableConcept(
                    new Coding()
                        .setCode(getValueByXPath(code, CODE_ATTRIBUTE_XPATH))
                        .setDisplay(getValueByXPath(code, CODE_DISPLAY_XPATH)))
                )
            );

        addName(deviceHl7, deviceFhir, NAME_XPATH, OTHER);
        addName(deviceHl7, deviceFhir, MANUFACTURER_MODE_NAME_XPATH, MANUFACTURERNAME);

        getOptionalNodeByXpath(deviceHl7, DESCRIPTION_XPATH)
            .ifPresent(descNode -> deviceFhir.addNote(
                new Annotation()
                    .setText(descNode.getTextContent())
                )
            );

        getOptionalNodeByXpath(deviceHl7, SOFTWARE_NAME_XPATH)
            .ifPresent(software -> deviceFhir.addVersion(
                new DeviceVersionComponent()
                    .setValue(software.getTextContent())
                )
            );


        return deviceFhir;
    }

    private void addName(Node deviceHl7, Device deviceFhir, String xpath, DeviceNameType nameValue) {
        getOptionalNodeByXpath(deviceHl7, xpath)
            .ifPresent(nameNode -> deviceFhir.addDeviceName(
                new DeviceDeviceNameComponent()
                    .setType(nameValue)
                    .setName(nameNode.getTextContent())
                )
            );
    }
}
