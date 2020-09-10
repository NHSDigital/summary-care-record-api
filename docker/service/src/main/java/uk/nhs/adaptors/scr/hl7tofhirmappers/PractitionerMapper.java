package uk.nhs.adaptors.scr.hl7tofhirmappers;

import static org.hl7.fhir.r4.model.IdType.newRandomUuid;

import java.util.ArrayList;
import java.util.List;

import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Practitioner;

import uk.nhs.adaptors.scr.models.hl7models.PractitionerObject;

public class PractitionerMapper {
    public Practitioner mapPractitioner(PractitionerObject practitionerObject){
        Practitioner practitioner = new Practitioner();

        practitioner.setId(newRandomUuid());

        practitioner.setIdentifier(getIdentifierPersonSDS(practitionerObject)); //personsSDS only

        if (practitionerObject.getPersonSDSName() != null){
            practitioner.addName(getName(practitionerObject)); //personsSDS only
        }

        return practitioner;
    }

    private HumanName getName(PractitionerObject practitionerObject) {
        HumanName humanName = new HumanName();

        humanName.setText(practitionerObject.getPersonSDSName());


        return humanName;
    }

    private List<Identifier> getIdentifierPersonSDS(PractitionerObject practitionerObject) {
        List<Identifier> identifierList = new ArrayList<>();

        if (practitionerObject.getPersonSDSID() != null){
            Identifier identifier = new Identifier();
            identifier
                .setSystem("https://fhir.nhs.uk/Id/sds-user-id")
                .setValue(practitionerObject.getPersonSDSID());
            identifierList.add(identifier);
        }

        return identifierList;
    }
}
