package uk.nhs.adaptors.scr.mappings.from.fhir;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.ContactPoint;
import org.hl7.fhir.r4.model.Encounter.EncounterParticipantComponent;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.PractitionerRole;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.RelatedPerson;
import uk.nhs.adaptors.scr.exceptions.FhirValidationException;
import uk.nhs.adaptors.scr.models.xml.AgentDevice;
import uk.nhs.adaptors.scr.models.xml.AgentPerson;
import uk.nhs.adaptors.scr.models.xml.AgentPersonSDS;
import uk.nhs.adaptors.scr.models.xml.Device;
import uk.nhs.adaptors.scr.models.xml.DeviceSDS;
import uk.nhs.adaptors.scr.models.xml.NonAgentRole;
import uk.nhs.adaptors.scr.models.xml.Organization;
import uk.nhs.adaptors.scr.models.xml.OrganizationSDS;
import uk.nhs.adaptors.scr.models.xml.Participant;
import uk.nhs.adaptors.scr.models.xml.Person;
import uk.nhs.adaptors.scr.models.xml.PersonSDS;

import java.util.Optional;
import java.util.stream.Collectors;

import static uk.nhs.adaptors.scr.utils.DateUtil.formatDateToHl7;
import static uk.nhs.adaptors.scr.utils.FhirHelper.getDomainResourceList;
import static uk.nhs.adaptors.scr.utils.FhirHelper.getResourceByReference;

@Slf4j
public class ParticipantAgentMapper {

    private static final String MODE_CODE_URL = "https://fhir.nhs.uk/StructureDefinition/Extension-SCR-ModeCode";

    public static Participant.Author mapAuthor(Bundle bundle, EncounterParticipantComponent encounterParticipant) {
        var author = new Participant.Author();
        author.setTime(formatDateToHl7(encounterParticipant.getPeriod().getStart()));
        setParticipantAgents(bundle, encounterParticipant.getIndividual(), author);
        return author;
    }

    public static Participant.Author1 mapAuthor1(Bundle bundle, EncounterParticipantComponent encounterParticipant) {
        var author = new Participant.Author1();
        author.setTime(formatDateToHl7(encounterParticipant.getPeriod().getStart()));

        var practitionerRoleReference = encounterParticipant.getIndividual().getReference();
        var practitionerRole = getResourceByReference(bundle, practitionerRoleReference, PractitionerRole.class)
            .orElseThrow(() -> new FhirValidationException(String.format(
                "Bundle is missing PractitionerRole %s that is linked to Encounter", practitionerRoleReference)));

        if (StringUtils.isNotBlank(practitionerRole.getOrganization().getReference())) {
            setAgentDevice(bundle, practitionerRole.getOrganization(), author);
        } else if (StringUtils.isNotBlank(practitionerRole.getPractitioner().getReference())) {
            setParticipantAgents(bundle, practitionerRole.getPractitioner(), author);
        }
        return author;
    }

    private static void setAgentDevice(Bundle bundle, Reference individual, Participant.Author1 author) {
        var organizationReference = individual.getReference();
        var organization = getResourceByReference(bundle, organizationReference, org.hl7.fhir.r4.model.Organization.class)
            .orElseThrow(() -> new FhirValidationException("Bundle is missing Organization %s that is linked to PractitionerRole"));
        var device = getDomainResourceList(bundle, org.hl7.fhir.r4.model.Device.class).stream()
            .filter(dev -> organizationReference.equals(dev.getOwner().getReference()))
            .reduce((a, b) -> {
                throw new FhirValidationException(String.format("Bundle has more than 1 Device resource referencing %s",
                    organizationReference));
            })
            .orElseThrow(() -> new FhirValidationException(String.format("Bundle has no Device resource referencing %s",
                organization)));

        var agentDevice = new AgentDevice();
        agentDevice.setIdRoot(device.getIdentifierFirstRep().getValue());

        var code = organization.getTypeFirstRep().getCodingFirstRep().getCode();
        if (StringUtils.isNotBlank(code)) {
            var representedOrganization = new Organization("representedOrganization");
            representedOrganization.setIdRoot("1.2.826.0.1285.0.1.10");
            representedOrganization.setIdExtension(organization.getIdentifierFirstRep().getValue());
            representedOrganization.setCodeCode(code);
            representedOrganization.setName(organization.getName());
            representedOrganization.setTelecom(organization.getTelecomFirstRep().getValue());
            representedOrganization.setAddress(organization.getAddressFirstRep().getLine().get(0).getValue());

            agentDevice.setOrganization(representedOrganization);
        } else {
            var representedOrganizationSDS = new OrganizationSDS("representedOrganizationSDS");
            representedOrganizationSDS.setIdRoot("1.2.826.0.1285.0.1.10");
            representedOrganizationSDS.setIdExtension(organization.getIdentifierFirstRep().getValue());

            agentDevice.setOrganizationSDS(representedOrganizationSDS);
        }

        if (StringUtils.isNotBlank(device.getIdentifierFirstRep().getSystem())) {
            var agentDeviceSDS = new DeviceSDS("agentDeviceSDS");
            agentDeviceSDS.setIdRoot("1.2.826.0.1285.0.2.0.107");
            agentDeviceSDS.setIdExtension(device.getIdentifierFirstRep().getValue());
            agentDevice.setDeviceSDS(agentDeviceSDS);
        } else {
            var agentDevice1 = new Device("agentDevice");
            agentDevice1.setIdRoot("1.2.826.0.1285.0.2.0.107");
            agentDevice1.setIdExtension(device.getIdentifierFirstRep().getValue());
            agentDevice1.setCodeCode(device.getType().getCodingFirstRep().getCode());
            agentDevice1.setCodeDisplayName(device.getType().getCodingFirstRep().getDisplay());
            device.getDeviceName().stream()
                .filter(deviceName -> deviceName.getType() == org.hl7.fhir.r4.model.Device.DeviceNameType.OTHER)
                .findFirst()
                .map(org.hl7.fhir.r4.model.Device.DeviceDeviceNameComponent::getName)
                .ifPresent(agentDevice1::setName);
            device.getDeviceName().stream()
                .filter(deviceName -> deviceName.getType() == org.hl7.fhir.r4.model.Device.DeviceNameType.MANUFACTURERNAME)
                .findFirst()
                .map(org.hl7.fhir.r4.model.Device.DeviceDeviceNameComponent::getName)
                .ifPresent(agentDevice1::setManufacturerModelName);
            agentDevice1.setDescription(device.getNoteFirstRep().getText());

            agentDevice.setDevice(agentDevice1);
        }

        author.setAgentDevice(agentDevice);
    }

    public static Participant.Informant mapInformant(Bundle bundle, EncounterParticipantComponent encounterParticipant) {
        var informant = new Participant.Informant();
        informant.setTime(formatDateToHl7(encounterParticipant.getPeriod().getStart()));

        var participantType = encounterParticipant.getIndividual().getReference().split("/")[0];
        if (PractitionerRole.class.getSimpleName().equals(participantType)) {
            setParticipantAgents(bundle, encounterParticipant.getIndividual(), informant);
        } else if (RelatedPerson.class.getSimpleName().equals(participantType)) {
            var relatedPerson = getResourceByReference(bundle, encounterParticipant.getIndividual().getReference(),
                RelatedPerson.class)
                .orElseThrow(() -> new FhirValidationException(
                    String.format("Bundle is missing RelatedPerson %s that is linked to Encounter",
                        encounterParticipant.getIndividual().getReference())));
            var participantNonAgentRole = new NonAgentRole("participantNonAgentRole");
            participantNonAgentRole.setCodeCode(relatedPerson.getRelationshipFirstRep().getCodingFirstRep().getCode());
            participantNonAgentRole.setCodeDisplayName(relatedPerson.getRelationshipFirstRep().getCodingFirstRep().getDisplay());
            participantNonAgentRole.setName(relatedPerson.getNameFirstRep().getText());
            informant.setParticipantNonAgentRole(participantNonAgentRole);
        } else {
            throw new FhirValidationException(String.format("Invalid Encounter participant type %s", participantType));
        }
        return informant;
    }

    public static Participant.Performer mapPerformer(Bundle bundle, EncounterParticipantComponent encounterParticipant) {
        var performer = new Participant.Performer();
        performer.setTime(formatDateToHl7(encounterParticipant.getPeriod().getStart()));
        var modeCodeExtension = encounterParticipant.getExtensionByUrl(MODE_CODE_URL);
        if (modeCodeExtension != null) {
            performer.setModeCodeCode(((CodeableConcept) modeCodeExtension.getValue()).getCodingFirstRep().getCode());
        }

        setParticipantAgents(bundle, encounterParticipant.getIndividual(), performer);
        return performer;
    }

    public static void setParticipantAgents(Bundle bundle, Reference individual, Participant participant) {
        var practitionerRole = getResourceByReference(bundle, individual.getReference(), PractitionerRole.class)
            .orElseThrow(() -> new FhirValidationException(
                String.format("Bundle is missing PractitionerRole %s that is linked to Encounter", individual.getReference())));

        LOGGER.debug("Looking up Practitioner for PractitionerRole.id={}", practitionerRole.getIdElement().getIdPart());
        var practitioner = getResourceByReference(bundle, practitionerRole.getPractitioner().getReference(),
            Practitioner.class)
            .orElseThrow(() -> new FhirValidationException(
                String.format("Bundle is missing Practitioner %s that is linked to PractitionerRole %s",
                    practitionerRole.getPractitioner().getReference(), practitionerRole.getId())));

        if ("http://fhir.nhs.net/Id/sds-role-profile-id".equals(practitionerRole.getIdentifierFirstRep().getSystem())) {
            var agentPersonSDS = new AgentPersonSDS();
            agentPersonSDS.setIdExtension(practitionerRole.getIdentifierFirstRep().getValue());

            var personSDS = new PersonSDS("agentPersonSDS");
            personSDS.setIdExtension(practitioner.getIdentifierFirstRep().getValue());
            personSDS.setName(practitioner.getNameFirstRep().getText());
            agentPersonSDS.setAgentPersonSDS(personSDS);

            participant.setAgentPersonSDS(agentPersonSDS);
        } else if ("https://fhir.nhs.uk/CodeSystem/HL7v3-SDSJobRoleName".equals(practitionerRole.getCodeFirstRep().getCodingFirstRep().getSystem())) {
            var organization = getResourceByReference(bundle, practitionerRole.getOrganization().getReference(),
                org.hl7.fhir.r4.model.Organization.class)
                .orElseThrow(() -> new FhirValidationException(
                    String.format("Bundle is missing Organization %s that is linked to PractitionerRole %s",
                        practitionerRole.getOrganization().getReference(), practitionerRole.getId())));

            var agentPerson = new AgentPerson();
            agentPerson.setCodeCode(practitionerRole.getCodeFirstRep().getCodingFirstRep().getCode());
            agentPerson.setCodeDisplayName(practitionerRole.getCodeFirstRep().getCodingFirstRep().getDisplay());
            agentPerson.setAddress(organization.getAddressFirstRep().getLine().get(0).getValue());
            agentPerson.setTelecom(organization.getTelecom().stream()
                .map(telecom -> new AgentPerson.Telecom()
                    .setUse(AgentPerson.Telecom.mapUse(Optional
                        .ofNullable(telecom.getUse())
                        .orElse(ContactPoint.ContactPointUse.WORK)))
                    .setValue(telecom.getValue()))
                .collect(Collectors.toList()));

            var person = new Person("agentPerson");
            person.setName(practitioner.getNameFirstRep().getText());
            agentPerson.setAgentPerson(person);

            if (organization.getTypeFirstRep().getCodingFirstRep().getCode() != null) {
                var representedOrganization = new Organization("representedOrganization");
                representedOrganization.setIdRoot(organization.getIdentifierFirstRep().getSystem());
                representedOrganization.setIdExtension(organization.getIdentifierFirstRep().getValue());
                representedOrganization.setCodeCode(organization.getTypeFirstRep().getCodingFirstRep().getCode());
                representedOrganization.setName(organization.getName());
                agentPerson.setRepresentedOrganization(representedOrganization);
            } else {
                var representedOrganizationSDS = new OrganizationSDS("representedOrganizationSDS");
                if ("https://fhir.nhs.uk/Id/ods-organization-code".equals(organization.getIdentifierFirstRep().getSystem())) {
                    representedOrganizationSDS.setIdRoot("1.2.826.0.1285.0.1.10");
                } else {
                    representedOrganizationSDS.setIdRoot("1.2.826.0.1285.0.2.0.109");
                }
                representedOrganizationSDS.setIdExtension(organization.getIdentifierFirstRep().getValue());
                representedOrganizationSDS.setName(organization.getName());
                agentPerson.setRepresentedOrganizationSDS(representedOrganizationSDS);
            }

            participant.setAgentPerson(agentPerson);
        } else {
            throw new FhirValidationException(String.format("Invalid PractitionerRole %s identifier.system or code.coding.system",
                practitionerRole.getId()));
        }
    }
}
