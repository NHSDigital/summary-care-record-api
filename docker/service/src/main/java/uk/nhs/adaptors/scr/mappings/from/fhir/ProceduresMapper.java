package uk.nhs.adaptors.scr.mappings.from.fhir;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Procedure;
import uk.nhs.adaptors.scr.models.GpSummary;
import uk.nhs.adaptors.scr.models.xml.Investigation;
import uk.nhs.adaptors.scr.models.xml.Treatment;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static uk.nhs.adaptors.scr.utils.DateUtil.formatDateToHl7;
import static uk.nhs.adaptors.scr.utils.FhirHelper.getDomainResourceList;

public class ProceduresMapper {
    private static final Predicate<Procedure> IS_INVESTIGATION =
        procedure -> "163081000000108".equals(procedure.getCategory().getCodingFirstRep().getCode());
    private static final Predicate<Procedure> IS_TREATMENT =
        procedure -> "163071000000106".equals(procedure.getCategory().getCodingFirstRep().getCode());

    public static void mapProcedures(GpSummary gpSummary, Bundle bundle) {
        gpSummary.getInvestigations()
            .addAll(mapInvestigations(bundle));
        gpSummary.getTreatments()
            .addAll(mapTreatments(bundle));
    }

    private static List<Investigation> mapInvestigations(Bundle bundle) {
        return getDomainResourceList(bundle, Procedure.class).stream()
            .filter(IS_INVESTIGATION)
            .map(ProceduresMapper::mapInvestigation)
            .collect(Collectors.toList());
    }

    private static List<Treatment> mapTreatments(Bundle bundle) {
        return getDomainResourceList(bundle, Procedure.class).stream()
            .filter(IS_TREATMENT)
            .map(ProceduresMapper::mapTreatment)
            .collect(Collectors.toList());
    }

    private static Investigation mapInvestigation(Procedure procedure) {
        return new Investigation()
            .setIdRoot(procedure.getIdentifierFirstRep().getValue())
            .setCodeCode(procedure.getCode().getCodingFirstRep().getCode())
            .setCodeDisplayName(procedure.getCode().getCodingFirstRep().getDisplay())
            .setStatusCodeCode(mapStatus(procedure.getStatus()))
            .setEffectiveTimeLow(formatDateToHl7(procedure.getPerformedDateTimeType().getValue()));
    }

    private static Treatment mapTreatment(Procedure procedure) {
        return new Treatment()
            .setIdRoot(procedure.getIdentifierFirstRep().getValue())
            .setCodeCode(procedure.getCode().getCodingFirstRep().getCode())
            .setCodeDisplayName(procedure.getCode().getCodingFirstRep().getDisplay())
            .setStatusCodeCode(mapStatus(procedure.getStatus()))
            .setEffectiveTimeLow(formatDateToHl7(procedure.getPerformedDateTimeType().getValue()));
    }

    private static String mapStatus(Procedure.ProcedureStatus status) {
        switch (status) {
            case PREPARATION:
            case INPROGRESS:
                return "active";
            case NOTDONE:
            case ONHOLD:
            case STOPPED:
                return "normal";
            case COMPLETED:
                return "completed";
            case ENTEREDINERROR:
            case UNKNOWN:
            case NULL:
            default:
                return "nullified";
        }
    }
}
