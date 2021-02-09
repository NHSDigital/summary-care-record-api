package uk.nhs.adaptors.scr.mappings.from.fhir;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Composition;
import uk.nhs.adaptors.scr.models.GpSummary;
import uk.nhs.adaptors.scr.models.xml.Participant;

import static uk.nhs.adaptors.scr.mappings.from.fhir.ParticipantAgentMapper.setParticipantAgents;
import static uk.nhs.adaptors.scr.utils.DateUtil.formatTimestampToHl7;
import static uk.nhs.adaptors.scr.utils.FhirHelper.getDomainResource;

public class AuthorMapper {
    public static void mapAuthor(GpSummary gpSummary, Bundle bundle) {
        var composition = getDomainResource(bundle, Composition.class);

        var author = new Participant.Author();

        author.setTime(formatTimestampToHl7(composition.getMeta().getLastUpdatedElement()));
        setParticipantAgents(bundle, composition.getAuthorFirstRep(), author);

        gpSummary.setAuthor(author);
    }
}
