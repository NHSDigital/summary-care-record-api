package uk.nhs.adaptors.scr.mappings.from.hl7;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.InstantType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import uk.nhs.adaptors.scr.utils.XmlUtils;

import static org.hl7.fhir.r4.model.Bundle.BundleType.SEARCHSET;
import static uk.nhs.adaptors.scr.utils.FhirHelper.randomUUID;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class InteractionMapper {
    private static final String INTERACTION_ID_XPATH = "//QUPC_IN210000UK04/id/@root";
    private static final String INTERACTION_CREATION_TIME_XPATH = "//QUPC_IN210000UK04/creationTime/@value";

    private final XmlUtils xmlUtils;

    public Bundle mapToEmpty() {
        return buildBundle()
            .setTotal(0);
    }

    @SneakyThrows
    public Bundle map(Document document) {
        var interactionId =
            xmlUtils.getValueByXPath(document, INTERACTION_ID_XPATH);
        var interactionCreationTime =
            XmlToFhirMapper.parseDate(xmlUtils.getValueByXPath(document, INTERACTION_CREATION_TIME_XPATH), InstantType.class);

        var bundle = buildBundle();

        bundle.setIdentifier(new Identifier()
            .setValue(interactionId)
            .setSystem("https://tools.ietf.org/html/rfc4122"));
        bundle.setTimestampElement(interactionCreationTime);

        return bundle;
    }

    private Bundle buildBundle() {
        Bundle bundle = new Bundle();
        bundle.setType(SEARCHSET);
        bundle.setId(randomUUID());
        return bundle;
    }
}
