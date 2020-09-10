package uk.nhs.adaptors.scr.hl7tofhirmappers;

import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.ContactPoint;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Organization;

import uk.nhs.adaptors.scr.models.hl7models.OrganizationObject;

public class OrganizationMapper {
    public Organization mapOrganization(OrganizationObject organizationObject){
        Organization organization = new Organization();

        //organization
        organization.setId("https://fhir.nhs.uk/Id/ods-organization-code");
        organization
            .addIdentifier(getIdentifierSDS(organizationObject)); //organizationsds


        organization.addType(getType(organizationObject)); //organization
        organization.addTelecom(getContactPoint(organizationObject)); // agent person
        organization.addAddress(getAddress(organizationObject)); // agent person & organization

        if (organizationObject.getName() != null){
            organization.setName(organizationObject.getName()); //organization
        }

        return organization;
    }

    private CodeableConcept getType(OrganizationObject organizationObject) {
        CodeableConcept codeableConcept = new CodeableConcept();
        if (organizationObject.getCode() != null){
                codeableConcept.addCoding(new Coding().setCode(
                    organizationObject.getCode()));
        }

        return codeableConcept;
    }

    private ContactPoint getContactPoint(OrganizationObject organizationObject) {
        ContactPoint contactPoint = new ContactPoint();
        if (organizationObject.getTelecom() != null){
            contactPoint
                .setSystem(ContactPoint.ContactPointSystem.PHONE)
                .setValue(organizationObject.getTelecom());
        }

        return contactPoint;
    }

    private Address getAddress(OrganizationObject organizationObject) {
        Address address = new Address();

        if (organizationObject.getAddress() != null){
            address.setText(organizationObject.getAddress()); //done
        }

        return address;
    }

    //organization
    private Identifier getIdentifierOrganization(OrganizationObject organizationObject) {
        Identifier identifier = new Identifier();

        identifier
            .setUse(Identifier.IdentifierUse.OFFICIAL);

        if (organizationObject.getIdRoot() != null){
            identifier.setSystem("https://fhir.nhs.uk/Id/ods-organization-code"); //done
        }
        if (organizationObject.getIdExtension() != null){
            identifier.setValue(organizationObject.getIdExtension()); //done
        }

        return identifier;
    }

    // only for organizationSDS
    private Identifier getIdentifierSDS(OrganizationObject organizationObject) {
        Identifier identifier = new Identifier();

        identifier
            .setUse(Identifier.IdentifierUse.OFFICIAL)
            .setSystem("https://fhir.nhs.uk/Id/ods-organization-code") //done
            .setValue(organizationObject.getSdsIDExtension()); //done

        return identifier;
    }
}
