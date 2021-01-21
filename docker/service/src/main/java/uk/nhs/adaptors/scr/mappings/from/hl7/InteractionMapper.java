package uk.nhs.adaptors.scr.mappings.from.hl7;

import lombok.SneakyThrows;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Identifier;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import uk.nhs.adaptors.scr.utils.XmlUtils;

import java.text.SimpleDateFormat;

import static org.hl7.fhir.r4.model.Bundle.BundleType.SEARCHSET;
import static uk.nhs.adaptors.scr.utils.FhirHelper.randomUUID;

@Component
public class InteractionMapper {

    private static final String DATE_TIME_PATTERN = "yyyyMMddHHmmss";

    private static final String INTERACTION_ID_XPATH = "//QUPC_IN210000UK04/id/@root";
    private static final String INTERACTION_CREATION_TIME_XPATH = "//QUPC_IN210000UK04/creationTime/@value";

    public Bundle mapToEmpty() {
        return buildBundle()
            .setTotal(0);
    }

    @SneakyThrows
    public Bundle map(Document document) {
        var simpleDateFormat = new SimpleDateFormat(DATE_TIME_PATTERN);

        var interactionId =
            XmlUtils.getValueByXPath(document, INTERACTION_ID_XPATH);
        var interactionCreationTime =
            simpleDateFormat.parse(XmlUtils.getValueByXPath(document, INTERACTION_CREATION_TIME_XPATH));

        var bundle = buildBundle();

        bundle.setIdentifier(new Identifier()
            .setValue(interactionId)
            .setSystem("https://tools.ietf.org/html/rfc4122"));
        bundle.setTimestamp(interactionCreationTime);

        return bundle;
    }

    private Bundle buildBundle() {
        Bundle bundle = new Bundle();
        bundle.setType(SEARCHSET);
        bundle.setId(randomUUID());
        return bundle;
    }
}
