package uk.nhs.adaptors.scr.mappings.from.fhir;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.ContactPoint;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.StringType;
import uk.nhs.adaptors.scr.exceptions.FhirMappingException;
import uk.nhs.adaptors.scr.models.GpSummary;
import uk.nhs.adaptors.scr.models.gpsummarymodels.OrganizationAddress;
import uk.nhs.adaptors.scr.models.gpsummarymodels.OrganizationCode;
import uk.nhs.adaptors.scr.models.gpsummarymodels.OrganizationId;
import uk.nhs.adaptors.scr.models.gpsummarymodels.OrganizationTelecom;
import uk.nhs.adaptors.scr.models.gpsummarymodels.OrganizationType;

import java.util.ArrayList;
import java.util.List;

public class OrganizationMapper {

    private static final String WORKGROUP_CODE = "1.2.826.0.1285.0.2.0.109";
    private static final String ORGANIZATION_CODE = "1.2.826.0.1285.0.1.10";
    private static final String ORGANIZATION_CODE_URL = "https://fhir.nhs.uk/Id/ods-organization-code";

    public static void mapOrganization(GpSummary gpSummary, Organization organization) throws FhirMappingException {
        setOrganizationAddresses(gpSummary, organization);
        setOrganizationTelecoms(gpSummary, organization);
        setOrganizationTypes(gpSummary, organization);
        setOrganizationName(gpSummary, organization);
        setOrganizationIds(gpSummary, organization);
    }

    private static void setOrganizationAddresses(GpSummary gpSummary, Organization organization) throws FhirMappingException {
        List<OrganizationAddress> organizationAddresses = new ArrayList<>();

        if (organization.hasAddress()) {
            for (Address address : organization.getAddress()) {
                organizationAddresses.add(parseAddressToOrganizationAddress(address));
            }
        }

        gpSummary.setOrganizationAddresses(organizationAddresses);
    }

    private static void setOrganizationTelecoms(GpSummary gpSummary, Organization organization) throws FhirMappingException {
        List<OrganizationTelecom> organizationTelecoms = new ArrayList<>();

        if (organization.hasTelecom()) {
            for (ContactPoint contactPoint : organization.getTelecom()) {
                if (contactPoint.hasValue()) {
                    OrganizationTelecom organizationTelecom = new OrganizationTelecom();
                    organizationTelecom.setOrganizationTelecoms(contactPoint.getValue());
                    organizationTelecoms.add(organizationTelecom);
                }
            }
        }

        gpSummary.setOrganizationTelecoms(organizationTelecoms);
    }

    private static void setOrganizationTypes(GpSummary gpSummary, Organization organization) throws FhirMappingException {
        List<OrganizationType> organizationTypes = new ArrayList<>();

        if (organization.hasType()) {
            for (CodeableConcept codeableConcept : organization.getType()) {
                OrganizationType organizationType = new OrganizationType();
                if (codeableConcept.hasCoding()) {
                    List<OrganizationCode> organizationCodes = new ArrayList<>();
                    for (Coding coding : codeableConcept.getCoding()) {
                        if (coding.hasCode()) {
                            OrganizationCode organizationCode = new OrganizationCode();
                            organizationCode.setOrganizationCode(coding.getCode());
                            organizationCodes.add(organizationCode);
                        }
                    }
                    organizationType.setOrganizationCodes(organizationCodes);
                }
                organizationTypes.add(organizationType);
            }
        }

        gpSummary.setOrganizationTypes(organizationTypes);
    }

    private static void setOrganizationName(GpSummary gpSummary, Organization organization) throws FhirMappingException {
        String value = StringUtils.EMPTY;

        if (organization.hasName()) {
            value = organization.getName();
        }

        gpSummary.setOrganizationName(value);
    }

    private static void setOrganizationIds(GpSummary gpSummary, Organization organization) throws FhirMappingException {
        List<OrganizationId> organizationIds = new ArrayList<>();

        if (organization.hasIdentifier()) {
            for (Identifier identifier : organization.getIdentifier()) {
                OrganizationId organizationId = new OrganizationId();

                if (identifier.hasValue()) {
                    organizationId.setOrganizationId(identifier.getValue());
                }

                if (identifier.hasSystem()) {
                    if (identifier.getSystem().equalsIgnoreCase(ORGANIZATION_CODE_URL)) {
                        organizationId.setOrganizationIdRoot(ORGANIZATION_CODE);
                    } else {
                        organizationId.setOrganizationIdRoot(WORKGROUP_CODE);
                    }
                } else {
                    organizationId.setOrganizationIdRoot(WORKGROUP_CODE);
                }

                organizationIds.add(organizationId);
            }
        }

        gpSummary.setOrganizationIds(organizationIds);
    }

    private static OrganizationAddress parseAddressToOrganizationAddress(Address address) throws FhirMappingException {
        List<String> addressList = new ArrayList<>();
        OrganizationAddress organizationAddress = new OrganizationAddress();

        if (address.hasTextElement()) {
            organizationAddress.setOrganizationAddress(address.getText());
        } else {
            if (address.hasLine()) {
                for (StringType line : address.getLine()) {
                    addressList.add(line.getValue());
                }
            }

            if (address.hasCity()) {
                addressList.add(address.getCity());
            }

            if (address.hasDistrict()) {
                addressList.add(address.getDistrict());
            }

            if (address.hasPostalCode()) {
                addressList.add(address.getPostalCode());
            }

            if (!addressList.isEmpty()) {
                organizationAddress.setOrganizationAddress(String.join(", ", addressList) + ".");
            }
        }

        return organizationAddress;
    }
}
