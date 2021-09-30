package uk.nhs.adaptors.scr.mappings.from.fhir;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.ContactPoint;
import org.hl7.fhir.r4.model.Encounter.EncounterParticipantComponent;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Identifier;
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

import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.hl7.fhir.r4.model.Device.DeviceNameType.MANUFACTURERNAME;
import static org.hl7.fhir.r4.model.Device.DeviceNameType.OTHER;
import static uk.nhs.adaptors.scr.utils.DateUtil.formatDateToHl7;
import static uk.nhs.adaptors.scr.utils.FhirHelper.getDomainResourceList;
import static uk.nhs.adaptors.scr.utils.FhirHelper.getResourceByReference;

@Slf4j
public class ParticipantAgentMapper {

    private static final String MODE_CODE_URL = "https://fhir.nhs.uk/StructureDefinition/Extension-SCR-ModeCode";
    private static final String SDS_DEVICE_SYSTEM = "https://fhir.nhs.uk/Id/SDSDevice";
    private static final String RELATIONSHIP_TYPE_SYSTEM = "https://fhir.nhs.uk/STU3/ValueSet/PersonRelationshipType-1";
    private static final String ORG_SDS_SYSTEM = "https://fhir.nhs.uk/Id/ods-organization-code";
    private static final String USER_SDS_SYSTEM = "https://fhir.nhs.uk/Id/sds-user-id";

    public static Participant.Author mapAuthor(Bundle bundle, EncounterParticipantComponent encounterParticipant) {
        var author = new Participant.Author();
        if (!encounterParticipant.getPeriod().hasStart()) {
            throw new FhirValidationException("Encounter.participant.period.start element is missing");
        }
        author.setTime(formatDateToHl7(encounterParticipant.getPeriod().getStartElement()));
        setParticipantAgents(bundle, encounterParticipant.getIndividual(), author);
        return author;
    }

    public static Participant.Author1 mapAuthor1(Bundle bundle, EncounterParticipantComponent encounterParticipant) {
        var author = new Participant.Author1();
        author.setTime(formatDateToHl7(encounterParticipant.getPeriod().getStartElement()));

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
                });

        var agentDevice = new AgentDevice();

        var code = organization.getTypeFirstRep().getCodingFirstRep().getCode();
        if (StringUtils.isNotBlank(code)) {
            var representedOrganization = new Organization("representedOrganization");
            representedOrganization.setIdRoot("1.2.826.0.1285.0.1.10");
            representedOrganization.setIdExtension(organization.getIdentifierFirstRep().getValue());
            representedOrganization.setCodeCode(code);
            representedOrganization.setName(organization.getName());
            representedOrganization.setTelecom(organization.getTelecomFirstRep().getValue());
            representedOrganization.setAddress(organization.getAddressFirstRep().getText());

            agentDevice.setOrganization(representedOrganization);
        } else {
            Identifier identifier = organization.getIdentifierFirstRep();
            if (ORG_SDS_SYSTEM.equals(identifier.getSystem()) && !identifier.hasValue()) {
                throw new FhirValidationException("Organization.identifier.value element is missing");
            }
            var representedOrganizationSDS = new OrganizationSDS("representedOrganizationSDS");
            representedOrganizationSDS.setIdRoot("1.2.826.0.1285.0.1.10");
            representedOrganizationSDS.setIdExtension(identifier.getValue());

            agentDevice.setOrganizationSDS(representedOrganizationSDS);
        }

        device.ifPresent(it -> {
            agentDevice.setIdRoot(it.getIdentifierFirstRep().getValue());
            if (SDS_DEVICE_SYSTEM.equals(it.getIdentifierFirstRep().getSystem())) {
                var agentDeviceSDS = new DeviceSDS("agentDeviceSDS");
                agentDeviceSDS.setIdRoot("1.2.826.0.1285.0.2.0.107");
                if (it.getIdentifierFirstRep().hasValue()) {
                    agentDeviceSDS.setIdExtension(it.getIdentifierFirstRep().getValue());
                } else {
                    throw new FhirValidationException("Device.identifier.value is missing");
                }
                agentDevice.setDeviceSDS(agentDeviceSDS);
            } else {
                var agentDevice1 = new Device("agentDevice");
                agentDevice1.setIdRoot("1.2.826.0.1285.0.2.0.107");
                agentDevice1.setIdExtension(it.getIdentifierFirstRep().getValue());
                setDeviceCoding(it, agentDevice1);
                it.getDeviceName().stream()
                        .filter(deviceName -> deviceName.getType() == OTHER)
                        .findFirst()
                        .map(org.hl7.fhir.r4.model.Device.DeviceDeviceNameComponent::getName)
                        .ifPresent(agentDevice1::setName);
                it.getDeviceName().stream()
                        .filter(deviceName -> deviceName.getType() == MANUFACTURERNAME)
                        .findFirst()
                        .map(org.hl7.fhir.r4.model.Device.DeviceDeviceNameComponent::getName)
                        .ifPresent(agentDevice1::setManufacturerModelName);
                agentDevice1.setDescription(it.getNoteFirstRep().getText());
                agentDevice1.setSoftwareName(it.getVersionFirstRep().getValue());

                agentDevice.setDevice(agentDevice1);
            }
        });

        author.setAgentDevice(agentDevice);
    }

    private static void setDeviceCoding(org.hl7.fhir.r4.model.Device fhirDevice, Device agentDevice1) {
        CodeableConcept type = fhirDevice.getType();
        Coding coding = type.getCodingFirstRep();
        if (!type.isEmpty() && isNotEmpty(coding.getCode()) && isNotEmpty(coding.getDisplay())) {
            agentDevice1.setCodeCode(coding.getCode());
            agentDevice1.setCodeDisplayName(coding.getDisplay());
        } else {
            throw new FhirValidationException("Missing mandatory elements: Device.type");
        }

    }

    public static Participant.Informant mapInformant(Bundle bundle, EncounterParticipantComponent encounterParticipant) {
        var informant = new Participant.Informant();
        informant.setTime(formatDateToHl7(encounterParticipant.getPeriod().getStartElement()));

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
            setRelationship(relatedPerson, participantNonAgentRole);
            setRelatedPersonName(relatedPerson, participantNonAgentRole);
            informant.setParticipantNonAgentRole(participantNonAgentRole);
        } else {
            throw new FhirValidationException(String.format("Invalid Encounter participant type %s", participantType));
        }
        return informant;
    }

    private static void setRelationship(RelatedPerson relatedPerson, NonAgentRole participantNonAgentRole) {
        Coding relationshipCoding = relatedPerson.getRelationshipFirstRep().getCodingFirstRep();
        if (!RELATIONSHIP_TYPE_SYSTEM.equals(relationshipCoding.getSystem())) {
            throw new FhirValidationException("Unsupported RelatedPerson.relationship.coding.system: " + relationshipCoding.getSystem());
        }
        if (!relationshipCoding.hasCode()) {
            throw new FhirValidationException("Missing RelatedPerson.relationship.coding.code element");
        }
        if (!relationshipCoding.hasDisplay()) {
            throw new FhirValidationException("Missing RelatedPerson.relationship.coding.display element");
        }
        participantNonAgentRole.setCodeCode(relationshipCoding.getCode());
        participantNonAgentRole.setCodeDisplayName(relationshipCoding.getDisplay());
    }

    private static void setRelatedPersonName(RelatedPerson relatedPerson, NonAgentRole participantNonAgentRole) {
        HumanName nameFirstRep = relatedPerson.getNameFirstRep();
        if (nameFirstRep.hasText()) {
            participantNonAgentRole.setName(nameFirstRep.getText());
        } else {
            throw new FhirValidationException("Missing RelatedPerson.name element");
        }
    }

    public static Participant.Performer mapPerformer(Bundle bundle, EncounterParticipantComponent encounterParticipant) {
        var performer = new Participant.Performer();
        performer.setTime(formatDateToHl7(encounterParticipant.getPeriod().getStartElement()));
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
        var practitioner = getResourceByReference(bundle, practitionerRole.getPractitioner().getReference(), Practitioner.class);

        if ("http://fhir.nhs.net/Id/sds-role-profile-id".equals(practitionerRole.getIdentifierFirstRep().getSystem())) {
            if (practitioner.isEmpty()) {
                throw new FhirValidationException(String.format("Bundle is missing Practitioner %s that is linked to PractitionerRole %s",
                    practitionerRole.getPractitioner().getReference(), practitionerRole.getId()));
            }
            Identifier practitionerIdentifier = practitioner.get().getIdentifierFirstRep();
            if (!USER_SDS_SYSTEM.equals(practitionerIdentifier.getSystem())) {
                throw new FhirValidationException("Invalid practitioner identifier system: " + practitionerIdentifier.getSystem());
            }
            if (!practitionerIdentifier.hasValue()) {
                throw new FhirValidationException("Missing practitioner identifier value");
            }
            var agentPersonSDS = new AgentPersonSDS();
            agentPersonSDS.setIdExtension(practitionerRole.getIdentifierFirstRep().getValue());

            var personSDS = new PersonSDS("agentPersonSDS");
            personSDS.setIdExtension(practitionerIdentifier.getValue());
            personSDS.setName(practitioner.get().getNameFirstRep().getText());
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
            agentPerson.setAddress(organization.getAddressFirstRep().getText());
            agentPerson.setTelecom(organization.getTelecom().stream()
                    .map(telecom -> new AgentPerson.Telecom()
                            .setUse(AgentPerson.Telecom.mapUse(Optional
                                    .ofNullable(telecom.getUse())
                                    .orElse(ContactPoint.ContactPointUse.WORK)))
                            .setValue(telecom.getValue()))
                    .collect(Collectors.toList()));

            var person = new Person("agentPerson");
            var name = practitioner.isPresent() ? practitioner.get().getNameFirstRep().getText() : organization.getName();
            person.setName(name);
            agentPerson.setAgentPerson(person);

            Identifier identifier = organization.getIdentifierFirstRep();
            if (organization.getTypeFirstRep().getCodingFirstRep().getCode() != null) {
                var representedOrganization = new Organization("representedOrganization");
                representedOrganization.setIdRoot(identifier.getSystem());
                representedOrganization.setIdExtension(identifier.getValue());
                representedOrganization.setCodeCode(organization.getTypeFirstRep().getCodingFirstRep().getCode());
                representedOrganization.setName(organization.getName());
                agentPerson.setRepresentedOrganization(representedOrganization);
            } else if (organization.hasIdentifier()) {
                var representedOrganizationSDS = new OrganizationSDS("representedOrganizationSDS");
                if (ORG_SDS_SYSTEM.equals(identifier.getSystem())) {
                    if (!identifier.hasValue()) {
                        throw new FhirValidationException("Organization.identifier.value element is missing");
                    }
                    representedOrganizationSDS.setIdRoot("1.2.826.0.1285.0.1.10");
                } else {
                    representedOrganizationSDS.setIdRoot("1.2.826.0.1285.0.2.0.109");
                }

                representedOrganizationSDS.setIdExtension(identifier.getValue());
                representedOrganizationSDS.setName(organization.getName());
                agentPerson.setRepresentedOrganizationSDS(representedOrganizationSDS);
            }

            participant.setAgentPerson(agentPerson);
        } else {
            throw new FhirValidationException(String.format("Invalid PractitionerRole %s identifier.system or code.coding.system",
                    practitionerRole.getId()));
        }
    }

    public static void setParticipantAgentOrganisation(Bundle bundle, Reference individual, Participant participant) {
        var organization = getResourceByReference(bundle, individual.getReference(), org.hl7.fhir.r4.model.Organization.class)
            .orElseThrow(() -> new FhirValidationException(
                String.format("Bundle is missing Organization %s that is linked to Author %s",
                    individual.getReference(), individual.getId())));
        Identifier identifier = organization.getIdentifierFirstRep();

        var representedOrganizationSDS = new OrganizationSDS("representedOrganizationSDS");
        if (ORG_SDS_SYSTEM.equals(identifier.getSystem())) {
            if (!identifier.hasValue()) {
                throw new FhirValidationException("Organization.identifier.value element is missing");
            }
            representedOrganizationSDS.setIdRoot("1.2.826.0.1285.0.1.10");
        } else {
            representedOrganizationSDS.setIdRoot("1.2.826.0.1285.0.2.0.109");
        }

        representedOrganizationSDS.setIdExtension(identifier.getValue());
        representedOrganizationSDS.setName(organization.getName());

        var agentPerson = new AgentPerson();
        agentPerson.setRepresentedOrganizationSDS(representedOrganizationSDS);
        participant.setAgentPerson(agentPerson);
    }
}
