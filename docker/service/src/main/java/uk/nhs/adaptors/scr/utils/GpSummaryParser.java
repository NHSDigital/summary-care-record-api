package uk.nhs.adaptors.scr.utils;

import static uk.nhs.adaptors.scr.fhirmappings.CompositionMapper.mapComposition;
import static uk.nhs.adaptors.scr.fhirmappings.OrganizationMapper.mapOrganization;
import static uk.nhs.adaptors.scr.fhirmappings.PatientMapper.mapPatient;
import static uk.nhs.adaptors.scr.fhirmappings.PractitionerMapper.mapPractitioner;
import static uk.nhs.adaptors.scr.fhirmappings.PractitionerRoleMapper.mapPractitionerRole;
import static uk.nhs.adaptors.scr.utils.DateUtil.formatDate;
import static uk.nhs.adaptors.scr.utils.FhirHelper.getDomainResource;
import static uk.nhs.adaptors.scr.utils.FhirHelper.getDomainResourceList;

import java.util.List;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Composition;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.PractitionerRole;

import lombok.extern.slf4j.Slf4j;
import uk.nhs.adaptors.scr.exceptions.FhirMappingException;
import uk.nhs.adaptors.scr.models.GpSummary;

@Slf4j
public class GpSummaryParser {
    public static GpSummary parseFromBundle(Bundle bundle) throws FhirMappingException {
        Composition composition = getDomainResource(bundle, Composition.class);
        PractitionerRole practitionerRole = getDomainResource(bundle, PractitionerRole.class);
        Organization organization = getDomainResource(bundle, Organization.class);
        Practitioner practitioner = getDomainResource(bundle, Practitioner.class);
        Patient patient = getDomainResource(bundle, Patient.class);
        List<Condition> conditionList = getDomainResourceList(bundle, Condition.class);
        List<Observation> observationList = getDomainResourceList(bundle, Observation.class);

        GpSummary gpSummary = new GpSummary();

        gpSummary.setHeaderId(bundle.getIdentifier().getValue());
        gpSummary.setHeaderTimeStamp(formatDate(bundle.getTimestampElement().asStringValue()));

        mapComposition(gpSummary, composition);
        mapPractitionerRole(gpSummary, practitionerRole);
        mapOrganization(gpSummary, organization);
        mapPractitioner(gpSummary, practitioner);
        mapPatient(gpSummary, patient);
        //TODO: create mappers for observation and condition

        return gpSummary;
    }
}
