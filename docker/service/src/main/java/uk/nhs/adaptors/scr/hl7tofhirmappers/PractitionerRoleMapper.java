package uk.nhs.adaptors.scr.hl7tofhirmappers;

import static org.hl7.fhir.r4.model.IdType.newRandomUuid;

import java.util.ArrayList;
import java.util.List;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.PractitionerRole;
import org.hl7.fhir.r4.model.Reference;

import uk.nhs.adaptors.scr.models.hl7models.PractitionerRoleObject;

public class PractitionerRoleMapper {
    public PractitionerRole mapPractitionerRole(PractitionerRoleObject practitionerRoleObject, Reference practitioner, Reference organization){
        PractitionerRole practitionerRole = new PractitionerRole();

        practitionerRole.setId(newRandomUuid());

        practitionerRole
            .setIdentifier(getIdentifierAgentPersonSDS(practitionerRoleObject)) //agentpersonSDS only
            .setPractitioner(practitioner)
            .setOrganization(organization) // agent person
            .setCode(getCodeAgentPerson(practitionerRoleObject)); //agentperson

        return practitionerRole;
    }

    private List<CodeableConcept> getCodeAgentPerson(PractitionerRoleObject practitionerRoleObject) {
        List<CodeableConcept> codeableConceptList = new ArrayList<>();

        if (practitionerRoleObject.getAgentPersonCode() != null && practitionerRoleObject.getCodeDisplayName() != null) {
            CodeableConcept codeableConcept = new CodeableConcept();
            codeableConcept.addCoding(
                new Coding()
                    .setSystem("https://fhir.nhs.uk/R4/CodeSystem/UKCore-SDSJobRoleName")
                    .setCode(practitionerRoleObject.getAgentPersonCode())
                    .setDisplay(practitionerRoleObject.getCodeDisplayName()));
            codeableConceptList.add(codeableConcept);
        }

        return codeableConceptList;
    }

    private List<Identifier> getIdentifierAgentPersonSDS(PractitionerRoleObject practitionerRoleObject) {
        List<Identifier> identifierList = new ArrayList<>();

        if (practitionerRoleObject.getAgentPersonSDSID() != null) {
            Identifier identifier = new Identifier();
            identifier
                .setSystem("http://fhir.nhs.net/Id/sds-role-profile-id")
                .setValue(practitionerRoleObject.getAgentPersonSDSID());
            identifierList.add(identifier);
        }

        return identifierList;
    }
}
