package uk.nhs.adaptors.scr.mappings.from.fhir;

import org.hl7.fhir.r4.model.Composition;
import org.hl7.fhir.r4.model.Composition.CompositionRelatesToComponent;
import uk.nhs.adaptors.scr.exceptions.FhirMappingException;
import uk.nhs.adaptors.scr.models.GpSummary;
import uk.nhs.adaptors.scr.models.gpsummarymodels.CompositionRelatesTo;
import uk.nhs.adaptors.scr.models.gpsummarymodels.Presentation;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static uk.nhs.adaptors.scr.utils.DateUtil.formatDate;
import static uk.nhs.adaptors.scr.utils.FhirHelper.randomUUID;

public class CompositionMapper {
    public static void mapComposition(GpSummary gpSummary, Composition composition) throws FhirMappingException {
        setCompositionRelatesToIds(gpSummary, composition);
        setCompositionId(gpSummary, composition);
        setCompositionDate(gpSummary, composition);
        setPresentations(gpSummary, composition);
    }

    private static void setCompositionRelatesToIds(GpSummary gpSummary, Composition composition) throws FhirMappingException {
        List<CompositionRelatesTo> compositionRelatesTos = new ArrayList<>();

        if (composition.hasRelatesTo()) {
            for (CompositionRelatesToComponent relatesTo : composition.getRelatesTo()) {
                if (relatesTo.hasTargetIdentifier()) {
                    if (relatesTo.getTargetIdentifier().hasValue()) {
                        CompositionRelatesTo compositionRelatesTo = new CompositionRelatesTo();
                        compositionRelatesTo.setCompositionRelatesToId(relatesTo.getTargetIdentifier().getValue().toUpperCase());
                        compositionRelatesTos.add(compositionRelatesTo);
                    } else {
                        throw new FhirMappingException("Composition RelatesTo TargetIdentifier Value missing from payload");
                    }
                } else {
                    throw new FhirMappingException("Composition RelatesTo TargetIdentifier missing from payload");
                }
            }
        }

        gpSummary.setCompositionRelatesTos(compositionRelatesTos);
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
            gpSummary.setCompositionDate(formatDate(composition.getDateElement().asStringValue()));
        } else {
            throw new FhirMappingException("Composition Date Element missing from payload");
        }
    }

    private static void setPresentations(GpSummary gpSummary, Composition composition) throws FhirMappingException {
        List<Presentation> presentationList = new ArrayList<>();

        if (composition.hasSection()) {
            for (Composition.SectionComponent section : composition.getSection()) {
                Presentation presentation = new Presentation();
                String value = EMPTY;

                if (section.hasText()) {
                    if (section.getText().hasDiv()) {
                        value = section.getText().getDiv().toString();
                    }
                }

                presentation.setPresentationId(randomUUID());
                presentation.setPresentationText(value);
                presentationList.add(presentation);
            }
        }

        gpSummary.setPresentations(presentationList);
    }
}
