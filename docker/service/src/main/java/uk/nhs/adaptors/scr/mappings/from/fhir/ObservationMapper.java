package uk.nhs.adaptors.scr.mappings.from.fhir;

import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Period;
import uk.nhs.adaptors.scr.exceptions.FhirValidationException;
import uk.nhs.adaptors.scr.models.GpSummary;
import uk.nhs.adaptors.scr.models.xml.Finding;
import uk.nhs.adaptors.scr.models.xml.PersonalPreference;
import uk.nhs.adaptors.scr.models.xml.RiskToPatient;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static uk.nhs.adaptors.scr.mappings.from.fhir.ParticipantAgentMapper.mapAuthor1;
import static uk.nhs.adaptors.scr.mappings.from.fhir.ParticipantAgentMapper.mapInformant;
import static uk.nhs.adaptors.scr.mappings.from.fhir.ParticipantAgentMapper.mapPerformer;
import static uk.nhs.adaptors.scr.utils.DateUtil.formatDateToHl7;
import static uk.nhs.adaptors.scr.utils.FhirHelper.getDomainResourceList;
import static uk.nhs.adaptors.scr.utils.FhirHelper.getResourceByReference;

@Slf4j
public class ObservationMapper {

    private static final Predicate<Observation> IS_CLINICAL_OBSERVATION_AND_FINDING =
        observation -> "163131000000108".equals(observation.getCategoryFirstRep().getCodingFirstRep().getCode());
    private static final Predicate<Observation> IS_INVESTIGATION_RESULT =
        observation -> "163141000000104".equals(observation.getCategoryFirstRep().getCodingFirstRep().getCode());
    private static final Predicate<Observation> IS_RISK_TO_PATIENT =
        observation -> "163231000000100".equals(observation.getCategoryFirstRep().getCodingFirstRep().getCode());
    private static final Predicate<Observation> IS_PERSONAL_PREFERENCE =
        observation -> "162961000000108".equals(observation.getCategoryFirstRep().getCodingFirstRep().getCode());

    public static void mapObservations(GpSummary gpSummary, Bundle bundle) {
        gpSummary.getClinicalObservationsAndFindings()
            .addAll(mapClinicalObservationsAndFindings(bundle));
        gpSummary.getInvestigationResults()
            .addAll(mapInvestigationResults(bundle));
        gpSummary.getRisksToPatient()
            .addAll(mapRisksToPatient(bundle));
        gpSummary.getPersonalPreferences()
            .addAll(mapPersonalPreferences(bundle));
    }

    private static List<Finding> mapClinicalObservationsAndFindings(Bundle bundle) {
        return getDomainResourceList(bundle, Observation.class).stream()
            .filter(IS_CLINICAL_OBSERVATION_AND_FINDING)
            .map(observation -> mapFinding(observation, bundle))
            .collect(Collectors.toList());
    }

    private static List<Finding> mapInvestigationResults(Bundle bundle) {
        return getDomainResourceList(bundle, Observation.class).stream()
            .filter(IS_INVESTIGATION_RESULT)
            .map(observation -> mapFinding(observation, bundle))
            .collect(Collectors.toList());
    }

    private static List<RiskToPatient> mapRisksToPatient(Bundle bundle) {
        return getDomainResourceList(bundle, Observation.class).stream()
            .filter(IS_RISK_TO_PATIENT)
            .map(ObservationMapper::mapRiskToPatient)
            .collect(Collectors.toList());
    }

    private static List<PersonalPreference> mapPersonalPreferences(Bundle bundle) {
        return getDomainResourceList(bundle, Observation.class).stream()
            .filter(IS_PERSONAL_PREFERENCE)
            .map(ObservationMapper::mapPersonalPreference)
            .collect(Collectors.toList());
    }

    private static PersonalPreference mapPersonalPreference(Observation observation) {
        return new PersonalPreference()
            .setIdRoot(observation.getIdElement().getIdPart())
            .setCodeCode(observation.getCode().getCodingFirstRep().getCode())
            .setCodeDisplayName(observation.getCode().getCodingFirstRep().getDisplay())
            .setStatusCodeCode(mapStatus(observation.getStatus()))
            .setEffectiveTimeLow(formatDateToHl7(observation.getEffectiveDateTimeType().getValue()));
    }

    private static RiskToPatient mapRiskToPatient(Observation observation) {
        return new RiskToPatient()
            .setIdRoot(observation.getIdElement().getIdPart())
            .setCodeCode(observation.getCode().getCodingFirstRep().getCode())
            .setCodeDisplayName(observation.getCode().getCodingFirstRep().getDisplay())
            .setStatusCodeCode(mapStatus(observation.getStatus()))
            .setEffectiveTimeLow(formatDateToHl7(observation.getEffectiveDateTimeType().getValue()));
    }

    private static Finding mapFinding(Observation observation, Bundle bundle) {
        var finding = new Finding();

        finding.setIdRoot(observation.getIdentifierFirstRep().getValue());
        finding.setCodeCode(observation.getCode().getCodingFirstRep().getCode());
        finding.setCodeDisplayName(observation.getCode().getCodingFirstRep().getDisplay());
        finding.setStatusCodeCode(mapStatus(observation.getStatus()));
        if (observation.getEffective() instanceof DateTimeType) {
            finding.setEffectiveTimeCenter(formatDateToHl7(observation.getEffectiveDateTimeType().getValue()));
        } else if (observation.getEffective() instanceof Period) {
            var period = observation.getEffectivePeriod();
            if (period.hasStart()) {
                finding.setEffectiveTimeLow(formatDateToHl7(period.getStart()));
            }
            if (period.hasEnd()) {
                finding.setEffectiveTimeHigh(formatDateToHl7(period.getEnd()));
            }
        } else {
            throw new FhirValidationException("Observation.effective must be of type DateTimeType or Period");
        }

        LOGGER.debug("Looking up Encounter for Condition.id={}", observation.getIdElement().getIdPart());
        var encounter = getResourceByReference(bundle, observation.getEncounter().getReference(), Encounter.class)
            .orElseThrow(() -> new FhirValidationException(String.format("Bundle is Missing Encounter %s that is linked to Condition %s", observation.getEncounter().getReference(), observation.getId())));

        for (var encounterParticipant : encounter.getParticipant()) {
            var code = encounterParticipant.getTypeFirstRep().getCodingFirstRep().getCode();
            if ("AUT".equals(code)) {
                var author = mapAuthor1(bundle, encounterParticipant);
                finding.setAuthor(author);
            } else if ("INF".equals(code)) {
                var informant = mapInformant(bundle, encounterParticipant);
                finding.setInformant(informant);
            } else if ("PRF".equals(code)) {
                var performer = mapPerformer(bundle, encounterParticipant);
                finding.setPerformer(performer);
            } else {
                throw new FhirValidationException(String.format("Invalid encounter %s participant code %s", encounter.getId(), code));
            }
        }

        return finding;
    }

    private static String mapStatus(Observation.ObservationStatus status) {
        switch (status) {
            case REGISTERED:
            case PRELIMINARY:
            case FINAL:
            case AMENDED:
            case CORRECTED:
            case CANCELLED:
                return "completed";
            case ENTEREDINERROR:
            case UNKNOWN:
            case NULL:
            default:
                return "nullified";
        }
    }
}
