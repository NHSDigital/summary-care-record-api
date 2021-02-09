package uk.nhs.adaptors.scr.mappings.from.fhir;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.ImmunizationRecommendation;
import uk.nhs.adaptors.scr.models.GpSummary;
import uk.nhs.adaptors.scr.models.xml.Finding;

import java.util.List;
import java.util.stream.Collectors;

import static uk.nhs.adaptors.scr.utils.DateUtil.formatDateToHl7;
import static uk.nhs.adaptors.scr.utils.FhirHelper.getDomainResourceList;

public class ImmunizationRecommendationMapper {
    public static void mapImmunizationRecommendations(GpSummary gpSummary, Bundle bundle) {
        gpSummary.getMedicationRecommendations()
            .addAll(mapMedicationRecommendations(bundle));
    }

    private static List<Finding> mapMedicationRecommendations(Bundle bundle) {
        return getDomainResourceList(bundle, ImmunizationRecommendation.class).stream()
            .map(ImmunizationRecommendationMapper::mapMedicationRecommendation)
            .collect(Collectors.toList());
    }

    private static Finding mapMedicationRecommendation(ImmunizationRecommendation immunizationRecommendation) {
        var coding = immunizationRecommendation
            .getRecommendationFirstRep()
            .getContraindicatedVaccineCodeFirstRep()
            .getCodingFirstRep();

        return new Finding()
            .setIdRoot(immunizationRecommendation.getIdentifierFirstRep().getValue())
            .setCodeCode(coding.getCode())
            .setCodeDisplayName(coding.getDisplay())
            .setStatusCodeCode("completed")
            .setEffectiveTimeLow(formatDateToHl7(immunizationRecommendation.getDateElement()));
    }
}
