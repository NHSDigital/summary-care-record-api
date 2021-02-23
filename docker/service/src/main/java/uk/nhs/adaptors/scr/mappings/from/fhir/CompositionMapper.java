package uk.nhs.adaptors.scr.mappings.from.fhir;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Composition;
import uk.nhs.adaptors.scr.exceptions.FhirMappingException;
import uk.nhs.adaptors.scr.models.GpSummary;
import uk.nhs.adaptors.scr.models.xml.Presentation;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static uk.nhs.adaptors.scr.mappings.from.hl7.HtmlParser.createNewDocument;
import static uk.nhs.adaptors.scr.mappings.from.hl7.HtmlParser.parseDocument;
import static uk.nhs.adaptors.scr.mappings.from.hl7.HtmlParser.removeEmptyNodes;
import static uk.nhs.adaptors.scr.mappings.from.hl7.HtmlParser.serialize;
import static uk.nhs.adaptors.scr.utils.DateUtil.formatDateToHl7;
import static uk.nhs.adaptors.scr.utils.FhirHelper.getDomainResource;
import static uk.nhs.adaptors.scr.utils.FhirHelper.randomUUID;

public class CompositionMapper {
    public static void mapComposition(GpSummary gpSummary, Bundle bundle) throws FhirMappingException {
        var composition = getDomainResource(bundle, Composition.class);

        setCompositionRelatesToId(gpSummary, composition);
        setCompositionId(gpSummary, composition);
        setCompositionDate(gpSummary, composition);
        setPresentation(gpSummary, composition);
    }

    private static void setCompositionRelatesToId(GpSummary gpSummary, Composition composition) throws FhirMappingException {
        var id = composition.getRelatesToFirstRep().getTargetIdentifier().getValue();
        if (StringUtils.isNotBlank(id)) {
            gpSummary.setCompositionRelatesToId(id.toUpperCase());
        }
    }

    private static void setCompositionId(GpSummary gpSummary, Composition composition) throws FhirMappingException {
        String value = EMPTY;

        if (composition.hasIdentifier()) {
            if (composition.getIdentifier().hasValue()) {
                value = composition.getIdentifier().getValue().toUpperCase();
            }
        }

        gpSummary.setCompositionId(value);
    }

    private static void setCompositionDate(GpSummary gpSummary, Composition composition) throws FhirMappingException {
        if (composition.hasDateElement()) {
            gpSummary.setCompositionDate(formatDateToHl7(composition.getDateElement()));
        } else {
            throw new FhirMappingException("Composition Date Element missing from payload");
        }
    }

    private static void setPresentation(GpSummary gpSummary, Composition composition) throws FhirMappingException {
        if (!composition.hasSection()) {
            throw new FhirMappingException("Missing mandatory Composition.section");
        }

        Presentation presentation = new Presentation();

        var htmlDocument = createNewDocument("html", "xhtml:NPfIT:PresentationText");
        var bodyNode = htmlDocument.createElement("body");
        htmlDocument.getDocumentElement().appendChild(bodyNode);

        for (Composition.SectionComponent section : composition.getSection()) {
            var h2Node = htmlDocument.createElement("h2");
            h2Node.setAttribute("id", section.getCode().getCodingFirstRep().getCode());
            h2Node.setNodeValue(section.getTitle());
            bodyNode.appendChild(h2Node);

            var divDocument = parseDocument(section.getText().getDiv().getValueAsString());
            removeEmptyNodes(divDocument);
            var divChildNodes = divDocument.getDocumentElement().getChildNodes();
            for (int i = 0; i < divChildNodes.getLength(); i++) {
                bodyNode.appendChild(htmlDocument.importNode(divChildNodes.item(i), true));
            }
        }
        presentation.setPresentationId(randomUUID());
        presentation.setPresentationText(serialize(htmlDocument));

        gpSummary.setPresentation(presentation);
    }
}
