package uk.nhs.adaptors.scr.mappings.from.fhir;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.Observation;
import uk.nhs.adaptors.scr.models.GpSummary;
import uk.nhs.adaptors.scr.models.xml.CareEvent;

import java.util.List;
import java.util.stream.Collectors;

import static uk.nhs.adaptors.scr.utils.DateUtil.formatDateToHl7;
import static uk.nhs.adaptors.scr.utils.FhirHelper.getDomainResourceList;

public class EncounterMapper {
    public static void mapEncounters(GpSummary gpSummary, Bundle bundle) {
        gpSummary.getCareEvents()
            .addAll(mapCareEvents(bundle));
    }

    private static List<CareEvent> mapCareEvents(Bundle bundle) {
        return getDomainResourceList(bundle, Encounter.class).stream()
            .filter(encounter -> !isReferenced(encounter, bundle))
            .map(EncounterMapper::mapCareEvent)
            .collect(Collectors.toList());
    }

    private static CareEvent mapCareEvent(Encounter encounter) {
        var careEvent = new CareEvent();
        careEvent.setIdRoot(encounter.getIdentifierFirstRep().getValue());
        careEvent.setCodeCode(encounter.getTypeFirstRep().getCodingFirstRep().getCode());
        careEvent.setCodeDisplayName(encounter.getTypeFirstRep().getCodingFirstRep().getDisplay());
        careEvent.setStatusCodeCode(mapStatusCode(encounter.getStatus()));
        careEvent.setEffectiveTimeLow(formatDateToHl7(encounter.getPeriod().getStartElement()));
        careEvent.setEffectiveTimeHigh(formatDateToHl7(encounter.getPeriod().getEndElement()));
        return careEvent;
    }

    private static String mapStatusCode(Encounter.EncounterStatus status) {
        switch (status) {
            case PLANNED:
            case ONLEAVE:
                return "normal";
            case ARRIVED:
            case TRIAGED:
            case INPROGRESS:
                return "active";
            case FINISHED:
            case CANCELLED:
                return "completed";
            case ENTEREDINERROR:
            case UNKNOWN:
            case NULL:
            default:
                return "nullified";
        }
    }

    private static boolean isReferenced(Encounter encounter, Bundle bundle) {
        return
            getDomainResourceList(bundle, Condition.class).stream()
                .anyMatch(condition -> isReferenced(encounter, condition))
            ||
            getDomainResourceList(bundle, Observation.class).stream()
                .anyMatch(condition -> isReferenced(encounter, condition));
    }

    private static boolean isReferenced(Encounter encounter, Condition condition) {
        return isReferenced(
            encounter.getIdElement().getIdPart(),
            condition.getEncounter().getReference());
    }

    private static boolean isReferenced(Encounter encounter, Observation observation) {
        return isReferenced(
            encounter.getIdElement().getIdPart(),
            observation.getEncounter().getReference());
    }

    private static boolean isReferenced(String id, String reference) {
        if (reference == null) {
            return false;
        }
        return id.equals(reference.split("/")[1]);
    }

}
