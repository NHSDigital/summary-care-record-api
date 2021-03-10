package uk.nhs.adaptors.scr.mappings.from.hl7;

import lombok.RequiredArgsConstructor;
import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Organization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Node;
import uk.nhs.adaptors.scr.utils.XmlUtils;

import static uk.nhs.adaptors.scr.utils.FhirHelper.randomUUID;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class OrganisationMapper {

    private static final String ORG_CODE_XPATH = "./code/@code";
    private static final String ORG_NAME_XPATH = "./name";
    private static final String ADDRESS_XPATH = "./addr";
    private static final String TELECOM_XPATH = "./telecom";

    private final TelecomMapper telecomMapper;
    private final XmlUtils xmlUtils;

    public Organization mapOrganization(Node organisation) {
        var org = new Organization();
        org.setId(randomUUID());
        xmlUtils.getOptionalValueByXPath(organisation, ORG_CODE_XPATH)
            .ifPresent(it -> org.addType(
                new CodeableConcept(new Coding()
                    .setCode(it))));
        xmlUtils.getOptionalNodeByXpath(organisation, ORG_NAME_XPATH)
            .ifPresent(name -> org.setName(name.getTextContent()));

        xmlUtils.getOptionalNodeByXpath(organisation, ADDRESS_XPATH)
            .ifPresent(node -> org.addAddress(new Address().addLine(node.getTextContent())));

        xmlUtils.getOptionalNodeByXpath(organisation, TELECOM_XPATH)
            .ifPresent(telecom -> org.addTelecom(telecomMapper.mapTelecom(telecom)));

        return org;
    }
}
