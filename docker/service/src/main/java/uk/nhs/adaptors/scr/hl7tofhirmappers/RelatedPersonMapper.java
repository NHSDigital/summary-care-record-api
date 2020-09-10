package uk.nhs.adaptors.scr.hl7tofhirmappers;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.RelatedPerson;

import uk.nhs.adaptors.scr.models.hl7models.RelatedPersonObject;

public class RelatedPersonMapper {
    public RelatedPerson mapRelatedPerson(RelatedPersonObject relatedPersonObject) {
        RelatedPerson relatedPerson = new RelatedPerson();

        //NonAgent
        if (relatedPersonObject.getNonAgentRoleID() != null){
            relatedPerson.setId(relatedPersonObject.getNonAgentRoleID());
        }
        if (relatedPersonObject.getNonAgentRoleIDExtension() != null){
            relatedPerson.addIdentifier(getIdentifier(relatedPersonObject));
        }
        relatedPerson.addRelationship(getRelationship(relatedPersonObject));

        //NonAgentPerson
        if (relatedPersonObject.getNonAgentPersonName() != null){
            relatedPerson.addName(new HumanName().setText(relatedPersonObject.getNonAgentPersonName()));
        }

        return relatedPerson;
    }

    private CodeableConcept getRelationship(RelatedPersonObject relatedPersonObject) {
        CodeableConcept codeableConcept = new CodeableConcept();
        Coding coding = new Coding();
        if (relatedPersonObject.getNonAgentRoleCode() != null){
            coding.setCode(relatedPersonObject.getNonAgentRoleCode());
        }
        if (relatedPersonObject.getNonAgentRoleDisplayName() != null){
            coding.setDisplay(relatedPersonObject.getNonAgentRoleDisplayName());
        }
        if (relatedPersonObject.getNonAgentRoleCodeSystem() != null){
            coding.setSystem(relatedPersonObject.getNonAgentRoleCodeSystem());
        }
        codeableConcept.addCoding(coding);

        return codeableConcept;
    }

    private Identifier getIdentifier(RelatedPersonObject relatedPersonObject) {
        Identifier identifier = new Identifier();

        identifier.setValue(relatedPersonObject.getNonAgentRoleIDExtension());

        return identifier;
    }
}
