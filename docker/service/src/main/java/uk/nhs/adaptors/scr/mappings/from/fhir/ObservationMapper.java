package uk.nhs.adaptors.scr.mappings.from.fhir;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Observation.ObservationStatus;
import org.hl7.fhir.r4.model.Period;
import uk.nhs.adaptors.scr.exceptions.FhirValidationException;
import uk.nhs.adaptors.scr.models.GpSummary;
import uk.nhs.adaptors.scr.models.xml.Finding;
import uk.nhs.adaptors.scr.models.xml.RiskToPatient;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static uk.nhs.adaptors.scr.mappings.from.fhir.ParticipantAgentMapper.mapAuthor1;
import static uk.nhs.adaptors.scr.mappings.from.fhir.ParticipantAgentMapper.mapInformant;
import static uk.nhs.adaptors.scr.mappings.from.fhir.ParticipantAgentMapper.mapPerformer;
import static uk.nhs.adaptors.scr.mappings.from.hl7.XmlToFhirMapper.SNOMED_SYSTEM;
import static uk.nhs.adaptors.scr.utils.DateUtil.formatDateToHl7;
import static uk.nhs.adaptors.scr.utils.FhirHelper.getDomainResourceList;
import static uk.nhs.adaptors.scr.utils.FhirHelper.getResourceByReference;

@Slf4j
public class ObservationMapper {
    private static final String PARTICIPATION_TYPE_SYSTEM = "http://terminology.hl7.org/CodeSystem/v3-ParticipationType";
    private static final Predicate<Observation> IS_CLINICAL_OBSERVATION_AND_FINDING =
        observation -> "163131000000108".equals(observation.getCategoryFirstRep().getCodingFirstRep().getCode());
    private static final Predicate<Observation> IS_INVESTIGATION_RESULT =
        observation -> "163141000000104".equals(observation.getCategoryFirstRep().getCodingFirstRep().getCode());
    private static final Predicate<Observation> IS_RISK_TO_PATIENT =
        observation -> "163231000000100".equals(observation.getCategoryFirstRep().getCodingFirstRep().getCode());

    public static void mapObservations(GpSummary gpSummary, Bundle bundle) {
        validate(bundle);
        gpSummary.getClinicalObservationsAndFindings()
            .addAll(mapClinicalObservationsAndFindings(bundle));
        gpSummary.getInvestigationResults()
            .addAll(mapInvestigationResults(bundle));
        gpSummary.getRisksToPatient()
            .addAll(mapRisksToPatient(bundle));
    }

    private static void validate(Bundle bundle) {
        getDomainResourceList(bundle, Observation.class).stream()
            .forEach(it -> {
                if (!it.getIdentifierFirstRep().hasValue()) {
                    throw new FhirValidationException("Observation.identifier.value is missing");
                }

                Coding coding = it.getCategoryFirstRep().getCodingFirstRep();
                if (!coding.getSystem().equals(SNOMED_SYSTEM)) {
                    throw new FhirValidationException("Invalid Observation.category.coding.system: " + coding.getSystem());
                }

                if (!coding.hasCode()) {
                    throw new FhirValidationException("Observation.category.coding.code is missing");
                }
            });
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
        var riskToPatientMapper = new RiskToPatientMapper();
        return getDomainResourceList(bundle, Observation.class).stream()
            .filter(IS_RISK_TO_PATIENT)
            .map(observation -> riskToPatientMapper.map(observation))
            .collect(Collectors.toList());
    }

    private static Finding mapFinding(Observation observation, Bundle bundle) {
        var finding = new Finding();

        finding.setIdRoot(observation.getIdentifierFirstRep().getValue());
        finding.setCodeCode(observation.getCode().getCodingFirstRep().getCode());
        finding.setCodeDisplayName(observation.getCode().getCodingFirstRep().getDisplay());
        finding.setStatusCodeCode(mapStatus(observation.getStatus()));
        if (observation.getEffective() instanceof DateTimeType) {
            finding.setEffectiveTimeCenter(formatDateToHl7(observation.getEffectiveDateTimeType()));
        } else if (observation.getEffective() instanceof Period) {
            var period = observation.getEffectivePeriod();
            if (period.hasStart()) {
                finding.setEffectiveTimeLow(formatDateToHl7(period.getStartElement()));
            }
            if (period.hasEnd()) {
                finding.setEffectiveTimeHigh(formatDateToHl7(period.getEndElement()));
            }
        } else {
            throw new FhirValidationException("Observation.effective must be of type DateTimeType or Period");
        }

        var encounterReference = observation.getEncounter().getReference();
        if (StringUtils.isNotBlank(encounterReference)) {
            var encounter = getResourceByReference(bundle, encounterReference, Encounter.class)
                .orElseThrow(() ->
                    new FhirValidationException(String.format("Bundle is Missing Encounter %s that is linked to Condition %s",
                        observation.getEncounter().getReference(), observation.getId())));

            for (var encounterParticipant : encounter.getParticipant()) {
                Coding coding = encounterParticipant.getTypeFirstRep().getCodingFirstRep();
                if (!PARTICIPATION_TYPE_SYSTEM.equals(coding.getSystem())) {
                    throw new FhirValidationException("Unsupported encounter participant system: " + coding.getSystem());
                }
                var code = coding.getCode();
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
        }

        return finding;
    }

    private static String mapStatus(ObservationStatus status) {
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
