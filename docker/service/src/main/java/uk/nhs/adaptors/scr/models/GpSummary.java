package uk.nhs.adaptors.scr.models;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import uk.nhs.adaptors.scr.models.gpsummarymodels.CompositionRelatesTo;
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

@Getter
@Setter
public class GpSummary {
    private String headerId;
    private String headerTimeStamp;
    private String compositionId;
    private String compositionDate;
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
}
