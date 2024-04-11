package uk.nhs.adaptors.scr.mappings.from.fhir;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Procedure;
import uk.nhs.adaptors.scr.exceptions.FhirValidationException;
import uk.nhs.adaptors.scr.models.GpSummary;
import uk.nhs.adaptors.scr.models.xml.Treatment;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static uk.nhs.adaptors.scr.utils.FhirHelper.getDomainResourceList;

/**
 * Many of the non-core CREs have a parent category (the basis of the CRE).
 * "Procedure" is the parent category of:
 * - Treatments    (UKCT_MT144055UK01)
 * Consider this mapper a parent or container for the sub-mappers.
 * In SNOMED terms, the Communication is a "qualifier value".
 * In FHIR terms, the Communication is a "resourceType".
 * This method uses the Java "Predicate" interface to help with comparisons.
 *
 * CMET: UKCT_MT144055UK01
 * Snomed: 163071000000106
 * @see: NIAD-2312
 * @see: src/test/resources/treatments/example.html
 */
public class ProcedureMapper {
    private static final Predicate<Procedure> IS_TREATMENT =
        procedure -> "163071000000106".equals(procedure.getCategory().getCodingFirstRep().getCode());

    public static void mapProcedures(GpSummary gpSummary, Bundle bundle) {
        validate(bundle);
        gpSummary.getTreatments()
            .addAll(mapTreatments(bundle));
    }

    private static List<Treatment> mapTreatments(Bundle bundle) {
        var treatmentMapper = new TreatmentMapper();
        return getDomainResourceList(bundle, Procedure.class).stream()
            .filter(IS_TREATMENT)
            .map(procedure -> treatmentMapper.mapTreatment(procedure))
            .collect(Collectors.toList());
    }

    private static void validate(Bundle bundle) {
        getDomainResourceList(bundle, Procedure.class).stream()
            .forEach(it -> {
                if (!it.hasId()) {
                    throw new FhirValidationException("Procedure.id is missing");
                }
                if (!it.hasCode()) {
                    throw new FhirValidationException("Procedure.code is missing");
                }
            });
    }
}
