package uk.nhs.adaptors.scr.mappings.from.fhir;

import static uk.nhs.adaptors.scr.mappings.from.fhir.ParticipantAgentMapper.setParticipantAgentOrganisation;
import static uk.nhs.adaptors.scr.mappings.from.fhir.ParticipantAgentMapper.setParticipantAgents;
import static uk.nhs.adaptors.scr.utils.DateUtil.formatTimestampToHl7;
import static uk.nhs.adaptors.scr.utils.FhirHelper.getDomainResource;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Composition;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.PractitionerRole;
import org.hl7.fhir.r4.model.Resource;

import uk.nhs.adaptors.scr.models.GpSummary;
import uk.nhs.adaptors.scr.models.xml.Participant;

public class AuthorMapper {
    public static void mapAuthor(GpSummary gpSummary, Bundle bundle) {
        var composition = getDomainResource(bundle, Composition.class);

        var author = new Participant.Author();

        author.setTime(formatTimestampToHl7(composition.getMeta().getLastUpdatedElement()));

        var isOrgPresent = isReferencePresent(composition.getAuthorFirstRep().getReference(), Organization.class);
        var isPractitionerRolePresent = isReferencePresent(composition.getAuthorFirstRep().getReference(), PractitionerRole.class);
        if (isOrgPresent && !isPractitionerRolePresent) {
            setParticipantAgentOrganisation(bundle, composition.getAuthorFirstRep(), author);
        } else {
            setParticipantAgents(bundle, composition.getAuthorFirstRep(), author);
        }

        gpSummary.setAuthor(author);
    }

    private static <T extends Resource> boolean isReferencePresent(String reference, Class<T> resourceType) {
        var expectedResourceReference = resourceType.getSimpleName();
        return expectedResourceReference.equals(reference.split("/")[0]);
    }
}
