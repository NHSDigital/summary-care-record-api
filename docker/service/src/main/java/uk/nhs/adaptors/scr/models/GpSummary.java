package uk.nhs.adaptors.scr.models;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Composition;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.PractitionerRole;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.ResourceType;
import uk.nhs.adaptors.scr.exceptions.FhirMappingException;
import uk.nhs.adaptors.scr.models.gpsummarymodels.CompositionRelatesTo;
import uk.nhs.adaptors.scr.models.gpsummarymodels.AllConditions;
import uk.nhs.adaptors.scr.models.gpsummarymodels.ObservationObject;
import uk.nhs.adaptors.scr.models.gpsummarymodels.OrganizationAddress;
import uk.nhs.adaptors.scr.models.gpsummarymodels.OrganizationId;
import uk.nhs.adaptors.scr.models.gpsummarymodels.OrganizationTelecom;
import uk.nhs.adaptors.scr.models.gpsummarymodels.OrganizationType;
import uk.nhs.adaptors.scr.models.gpsummarymodels.PatientId;
import uk.nhs.adaptors.scr.models.gpsummarymodels.PractitionerId;
import uk.nhs.adaptors.scr.models.gpsummarymodels.PractitionerName;
import uk.nhs.adaptors.scr.models.gpsummarymodels.PractitionerRoleCode;
import uk.nhs.adaptors.scr.models.gpsummarymodels.PractitionerRoleIdentifier;
import uk.nhs.adaptors.scr.models.gpsummarymodels.Presentation;

import static uk.nhs.adaptors.scr.fhirmappings.CompositionMapper.mapComposition;
import static uk.nhs.adaptors.scr.fhirmappings.ConditionMapper.mapConditions;
import static uk.nhs.adaptors.scr.fhirmappings.ObservationMapper.mapObservations;
import static uk.nhs.adaptors.scr.fhirmappings.OrganizationMapper.mapOrganization;
import static uk.nhs.adaptors.scr.fhirmappings.PatientMapper.mapPatient;
import static uk.nhs.adaptors.scr.fhirmappings.PractitionerMapper.mapPractitioner;
import static uk.nhs.adaptors.scr.fhirmappings.PractitionerRoleMapper.mapPractitionerRole;
import static uk.nhs.adaptors.scr.utils.DateUtil.formatDate;
import static uk.nhs.adaptors.scr.utils.FhirHelper.getDomainResource;
import static uk.nhs.adaptors.scr.utils.FhirHelper.getDomainResourceList;

@Getter
@Setter
public class GpSummary {
    private String headerId;
    private String headerTimeStamp;
    private String compositionId;
    private String compositionDate;
    private String nhsdAsidFrom;
    private String nhsdAsidTo;
    private String partyIdFrom;
    private String partyIdTo;
    private List<CompositionRelatesTo> compositionRelatesTos;
    private List<PractitionerRoleIdentifier> practitionerRoleIdentifiers;
    private List<PractitionerRoleCode> practitionerRoleCodes;
    private List<PractitionerId> practitionerIds;
    private List<PractitionerName> practitionerNames;
    private List<OrganizationAddress> organizationAddresses;
    private List<OrganizationTelecom> organizationTelecoms;
    private List<OrganizationType> organizationTypes;
    private String organizationName;
    private List<OrganizationId> organizationIds;
    private List<PatientId> patientIds;
    private List<Presentation> presentations;
    private List<ObservationObject> observationList;
    private List<AllConditions> conditionParent;

    public static GpSummary fromRequestData(RequestData requestData) throws FhirMappingException {
        GpSummary gpSummary = new GpSummary();

        mapBundle(gpSummary, requestData.getBundle());

        gpSummary.setNhsdAsidFrom(requestData.getNhsdAsid());
        return gpSummary;
    }

    private static void mapBundle(GpSummary gpSummary, Bundle bundle) {
        Composition composition = getDomainResource(bundle, Composition.class);
        PractitionerRole practitionerRole = getDomainResource(bundle, PractitionerRole.class);
        Organization organization = getDomainResource(bundle, Organization.class);
        Practitioner practitioner = getDomainResource(bundle, Practitioner.class);
        Patient patient = getDomainResource(bundle, Patient.class);
        List<Resource> conditionList = getDomainResourceList(bundle, ResourceType.Condition);
        List<Resource> observationList = getDomainResourceList(bundle, ResourceType.Observation);

        gpSummarySetHeaderId(bundle, gpSummary);
        gpSummarySetHeaderTimeStamp(bundle, gpSummary);

        mapComposition(gpSummary, composition);
        mapPractitionerRole(gpSummary, practitionerRole);
        mapOrganization(gpSummary, organization);
        mapPractitioner(gpSummary, practitioner);
        mapPatient(gpSummary, patient);
        mapObservations(gpSummary, observationList);
        mapConditions(gpSummary, conditionList);
    }

    private static void gpSummarySetHeaderTimeStamp(Bundle bundle, GpSummary gpSummary) {
        if (bundle.hasIdentifier()) {
            if (bundle.getIdentifier().hasValue()) {
                gpSummary.setHeaderId(bundle.getIdentifier().getValue().toUpperCase());
            } else {
                throw new FhirMappingException("Bundle Idenifier Value missing from payload");
            }
        } else {
            throw new FhirMappingException("Bundle Identifier missing from payload");
        }
    }

    private static void gpSummarySetHeaderId(Bundle bundle, GpSummary gpSummary) {
        if (bundle.hasTimestampElement()){
            gpSummary.setHeaderTimeStamp(formatDate(bundle.getTimestampElement().asStringValue()));
        } else {
            throw new FhirMappingException("Bundle timestamp Value missing from payload");
        }
    }
}
