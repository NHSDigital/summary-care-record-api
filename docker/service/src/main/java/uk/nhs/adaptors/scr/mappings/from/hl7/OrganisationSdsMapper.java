package uk.nhs.adaptors.scr.mappings.from.hl7;

import lombok.RequiredArgsConstructor;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Organization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Node;
import uk.nhs.adaptors.scr.utils.XmlUtils;

import static uk.nhs.adaptors.scr.utils.FhirHelper.randomUUID;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class OrganisationSdsMapper {

    private static final String ORG_SDS_ID_XPATH = "./id/@extension";
    private static final String ORG_SDS_ID_ROOT_XPATH = "./id/@root";
    private static final String ORG_SDS_SYSTEM = "https://fhir.nhs.uk/Id/ods-organization-code";
    private static final String HL7_ORG_OID = "1.2.826.0.1285.0.1.10";
    private static final String ORG_NAME_XPATH = "./name";

    private final XmlUtils xmlUtils;

    public Organization mapOrganizationSds(Node organisationSds) {
        var org = new Organization();
        org.setId(randomUUID());
        var orgIdentifier = new Identifier()
            .setValue(xmlUtils.getValueByXPath(organisationSds, ORG_SDS_ID_XPATH));
        var rootId = xmlUtils.getValueByXPath(organisationSds, ORG_SDS_ID_ROOT_XPATH);
        if (HL7_ORG_OID.equals(rootId)) {
            orgIdentifier.setSystem(ORG_SDS_SYSTEM);
        }
        org.addIdentifier(orgIdentifier);

        xmlUtils.getOptionalNodeByXpathAndDetach(organisationSds, ORG_NAME_XPATH)
            .ifPresent(nameNode -> org.setName(nameNode.getTextContent()));

        return org;
    }
}
