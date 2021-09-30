package uk.nhs.adaptors.scr.mappings.from.fhir;

import static uk.nhs.adaptors.scr.mappings.from.fhir.ParticipantAgentMapper.setParticipantAgentOrganisation;
import static uk.nhs.adaptors.scr.mappings.from.fhir.ParticipantAgentMapper.setParticipantAgents;
import static uk.nhs.adaptors.scr.utils.DateUtil.formatTimestampToHl7;
import static uk.nhs.adaptors.scr.utils.FhirHelper.getDomainResource;
import static uk.nhs.adaptors.scr.utils.FhirHelper.getResourceByReference;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Composition;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.PractitionerRole;

import uk.nhs.adaptors.scr.models.GpSummary;
import uk.nhs.adaptors.scr.models.xml.Participant;

public class AuthorMapper {
    public static void mapAuthor(GpSummary gpSummary, Bundle bundle) {
        var composition = getDomainResource(bundle, Composition.class);

        var author = new Participant.Author();

        author.setTime(formatTimestampToHl7(composition.getMeta().getLastUpdatedElement()));

        var organization = getResourceByReference(bundle, composition.getAuthorFirstRep().getReference(), Organization.class);
        var practitionerRole = getResourceByReference(bundle, composition.getAuthorFirstRep().getReference(), PractitionerRole.class);
        if (organization.isPresent() && practitionerRole.isEmpty()) {
            setParticipantAgentOrganisation(organization.get(), author);
        } else {
            setParticipantAgents(bundle, composition.getAuthorFirstRep(), author);
        }

        gpSummary.setAuthor(author);
    }
}
