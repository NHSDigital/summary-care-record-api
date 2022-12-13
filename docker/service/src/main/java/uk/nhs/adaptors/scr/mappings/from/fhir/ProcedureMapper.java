package uk.nhs.adaptors.scr.mappings.from.fhir;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Procedure;
import uk.nhs.adaptors.scr.exceptions.FhirValidationException;
import uk.nhs.adaptors.scr.mappings.from.common.UuidWrapper;
import uk.nhs.adaptors.scr.models.GpSummary;
import uk.nhs.adaptors.scr.models.xml.Treatment;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static uk.nhs.adaptors.scr.utils.FhirHelper.getDomainResourceList;

public class ProcedureMapper {
    private static final Predicate<Procedure> IS_TREATMENT = procedure -> "163071000000106".equals(procedure.getCategory().getCodingFirstRep().getCode());
    public static void mapProcedures(GpSummary gpSummary, Bundle bundle) {
        validate(bundle);
        gpSummary.getTreatments()
            .addAll(mapTreatments(bundle));
    }

    private static List<Treatment> mapTreatments(Bundle bundle) {
        var treatmentMapper = new TreatmentMapper(new UuidWrapper());
        return getDomainResourceList(bundle, Procedure.class).stream()
            .filter(IS_TREATMENT)
            .map(procedure -> treatmentMapper.mapTreatment(procedure))
            .collect(Collectors.toList());
    }

    private static void validate(Bundle bundle) {
        getDomainResourceList(bundle, Procedure.class).stream()
            .forEach(it -> {
                if (!it.getIdentifierFirstRep().hasValue()) {
                    throw new FhirValidationException("Procedure.identifier.value is missing");
                }
                if (!it.hasCode()) {
                    throw new FhirValidationException("Procedure.code is missing");
                }
            });
    }
}
