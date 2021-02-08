package uk.nhs.adaptors.scr.mappings.from.fhir;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Immunization;
import uk.nhs.adaptors.scr.models.GpSummary;
import uk.nhs.adaptors.scr.models.xml.Finding;

import java.util.List;
import java.util.stream.Collectors;

import static uk.nhs.adaptors.scr.utils.DateUtil.formatDateToHl7;
import static uk.nhs.adaptors.scr.utils.FhirHelper.getDomainResourceList;

public class ImmunizationMapper {
    private static final String EXTENSION_URL = "https://fhir.hl7.org.uk/StructureDefinition/Extension-UKCore-VaccinationProcedure";

    public static void mapImmunizations(GpSummary gpSummary, Bundle bundle) {
        gpSummary.getMedicationRecords()
            .addAll(mapMedicationRecords(bundle));
    }

    private static List<Finding> mapMedicationRecords(Bundle bundle) {
        return getDomainResourceList(bundle, Immunization.class).stream()
            .map(ImmunizationMapper::mapImmunization)
            .collect(Collectors.toList());
    }

    private static Finding mapImmunization(Immunization immunization) {
        var codeableConcept = (CodeableConcept) immunization.getExtensionByUrl(EXTENSION_URL).getValue();

        //TODO is this the correct type? missing from https://data.developer.nhs.uk/dms/mim/6.3.01/Domains/Templates/Document%20files/cre_types_and_templates.htm
        return new Finding()
            .setIdRoot(immunization.getIdentifierFirstRep().getValue())
            .setCodeCode(codeableConcept.getCodingFirstRep().getCode())
            .setCodeDisplayName(codeableConcept.getCodingFirstRep().getDisplay())
            .setStatusCodeCode(mapStatus(immunization.getStatus()))
            .setEffectiveTimeLow(formatDateToHl7(immunization.getOccurrenceDateTimeType().getValue()));
    }

    private static String mapStatus(Immunization.ImmunizationStatus status) {
        switch (status) {
            case COMPLETED:
                return "completed";
            case ENTEREDINERROR:
            case NOTDONE:
            case NULL:
            default:
                return "nullified";
        }
    }
}
