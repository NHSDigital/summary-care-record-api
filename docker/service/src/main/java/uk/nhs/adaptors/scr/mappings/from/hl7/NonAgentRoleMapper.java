package uk.nhs.adaptors.scr.mappings.from.hl7;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.RelatedPerson;
import org.springframework.stereotype.Component;
import org.w3c.dom.Node;

import static uk.nhs.adaptors.scr.utils.FhirHelper.randomUUID;
import static uk.nhs.adaptors.scr.utils.XmlUtils.getNodeText;
import static uk.nhs.adaptors.scr.utils.XmlUtils.getOptionalNodeByXpath;
import static uk.nhs.adaptors.scr.utils.XmlUtils.getValueByXPath;

@Component
public class NonAgentRoleMapper {

    private static final String NON_AGENT_PERSON_XPATH = "./playingNonAgentPerson";
    private static final String NON_AGENT_PERSON_NAME_XPATH = "./name";
    private static final String NON_AGENT_ROLE_CODE_XPATH = "./code/@code";
    private static final String NON_AGENT_ROLE_DISPLAY_XPATH = "./code/@displayName";
    private static final String RELATIONSHIP_TYPE_SYSTEM = "https://fhir.nhs.uk/STU3/ValueSet/PersonRelationshipType-1";

    public RelatedPerson mapRelatedPerson(Node nonAgentRole) {
        var relatedPerson = new RelatedPerson()
            .addRelationship(new CodeableConcept(new Coding()
                .setSystem(RELATIONSHIP_TYPE_SYSTEM)
                .setCode(getValueByXPath(nonAgentRole, NON_AGENT_ROLE_CODE_XPATH))
                .setDisplay(getValueByXPath(nonAgentRole, NON_AGENT_ROLE_DISPLAY_XPATH))));
        relatedPerson.setId(randomUUID());

        getOptionalNodeByXpath(nonAgentRole, NON_AGENT_PERSON_XPATH)
            .ifPresent(nonAgentPerson -> relatedPerson.addName(
                new HumanName()
                    .setText(getNodeText(nonAgentPerson, NON_AGENT_PERSON_NAME_XPATH))));

        return relatedPerson;
    }
}
