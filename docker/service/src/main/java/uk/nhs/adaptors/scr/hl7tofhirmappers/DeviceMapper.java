package uk.nhs.adaptors.scr.hl7tofhirmappers;

import java.util.ArrayList;
import java.util.List;

import org.hl7.fhir.r4.model.Annotation;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Device;
import org.hl7.fhir.r4.model.Device.DeviceDeviceNameComponent;
import org.hl7.fhir.r4.model.Device.DeviceVersionComponent;
import org.hl7.fhir.r4.model.Identifier;

import uk.nhs.adaptors.scr.models.hl7models.DeviceObject;

public class DeviceMapper {
    public Device mapDevice(DeviceObject deviceObject) {
        Device device = new Device();

        //device sds
        device.addIdentifier(getIdentifierDeviceSDS(deviceObject));

        //agentDevice
        device.addIdentifier(getIdentifierAgentDevice(deviceObject));

        //device
        device.addIdentifier(getIdentifierDevice(deviceObject));
        device.setType(getDeviceType(deviceObject));
        device.setDeviceName(getDeviceName(deviceObject));
        device.setNote(getNote(deviceObject));
        device.setVersion(getVersion(deviceObject));

        return device;
    }

    private List<DeviceVersionComponent> getVersion(DeviceObject deviceObject) {
        List<DeviceVersionComponent> versionList = new ArrayList<>();
        for (String version: deviceObject.getDeviceVersionList()){
            DeviceVersionComponent versionComponent = new DeviceVersionComponent();
            versionComponent.setValue(version);
        }

        return versionList;
    }

    private List<Annotation> getNote(DeviceObject deviceObject) {
        List<Annotation> annotationList = new ArrayList<>();
        for (String desc : deviceObject.getDescList()) {
            Annotation annotation = new Annotation();
            annotation.setText(desc);
            annotationList.add(annotation);
        }
        return annotationList;
    }

    private List<DeviceDeviceNameComponent> getDeviceName(DeviceObject deviceObject) {
        List<DeviceDeviceNameComponent> deviceNameComponentList = new ArrayList<>();
        if (deviceObject.getDeviceName() != null){
            DeviceDeviceNameComponent nameComponent = new DeviceDeviceNameComponent();
            nameComponent.setName("other");
            deviceNameComponentList.add(nameComponent);
        }
        if (deviceObject.getManufacturerModelName() != null){
            DeviceDeviceNameComponent nameComponent = new DeviceDeviceNameComponent();
            nameComponent.setName("manufacturer-name");
            deviceNameComponentList.add(nameComponent);
        }

        return deviceNameComponentList;
    }

    private CodeableConcept getDeviceType(DeviceObject deviceObject) {
        CodeableConcept codeableConcept = new CodeableConcept();
        Coding coding = new Coding();
        if (deviceObject.getDeviceCode() != null){
            coding.setCode(deviceObject.getDeviceCode());
        }

        return codeableConcept;
    }

    private Identifier getIdentifierDevice(DeviceObject deviceObject) {
        Identifier identifier = new Identifier();
        if (deviceObject.getDeviceIdExtension() != null){
            identifier.setValue(deviceObject.getDeviceIdExtension());
        }

        return identifier;
    }

    private Identifier getIdentifierAgentDevice(DeviceObject deviceObject) {
        Identifier identifier = new Identifier();
        if (deviceObject.getAgentDeviceIDRoot() != null){
            identifier.setSystem(deviceObject.getAgentDeviceIDRoot());
        }
        if (deviceObject.getAgentDeviceIDExtension() != null){
            identifier.setValue(deviceObject.getAgentDeviceIDExtension());
        }

        return identifier;
    }

    private Identifier getIdentifierDeviceSDS(DeviceObject deviceObject) {
        Identifier identifier = new Identifier();
        if (deviceObject.getDeviceSDSIdExtension() != null){
            identifier.setValue(deviceObject.getDeviceSDSIdExtension());
        }

        return identifier;
    }
}
