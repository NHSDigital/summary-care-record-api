package uk.nhs.adaptors.scr.mappings.from.fhir;

import io.micrometer.core.instrument.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.Period;
import uk.nhs.adaptors.scr.exceptions.FhirMappingException;
import uk.nhs.adaptors.scr.exceptions.FhirValidationException;
import uk.nhs.adaptors.scr.models.GpSummary;
import uk.nhs.adaptors.scr.models.xml.Diagnosis;
import uk.nhs.adaptors.scr.models.xml.Problem;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static uk.nhs.adaptors.scr.mappings.from.fhir.ParticipantAgentMapper.mapAuthor;
import static uk.nhs.adaptors.scr.mappings.from.fhir.ParticipantAgentMapper.mapInformant;
import static uk.nhs.adaptors.scr.mappings.from.hl7.XmlToFhirMapper.SNOMED_SYSTEM;
import static uk.nhs.adaptors.scr.utils.DateUtil.formatDateToHl7;
import static uk.nhs.adaptors.scr.utils.FhirHelper.getDomainResourceList;
import static uk.nhs.adaptors.scr.utils.FhirHelper.getResourceByReference;

@Slf4j
/**
 * Mapping from FHIR to HL7 of health conditions of a patient.
 * These are Condition "resourceTypes" within FHIR.
 * Health conditions may be diagnoses or problems.
 * So this class acts as a parent container for these two.
 *
 * See: src/test/resources/problem/example-1.json
 */
public class ConditionMapper {

    private static final String PARTICIPATION_TYPE_SYSTEM = "http://terminology.hl7.org/CodeSystem/v3-ParticipationType";
    private static final Predicate<Condition> IS_DIAGNOSES =
        condition -> "163001000000103".equals(condition.getCategoryFirstRep().getCodingFirstRep().getCode());
    private static final Predicate<Condition> IS_PROBLEM =
        condition -> "162991000000102".equals(condition.getCategoryFirstRep().getCodingFirstRep().getCode());

    public static void mapConditions(GpSummary gpSummary, Bundle bundle) {
        validate(bundle);

        gpSummary.getDiagnoses()
            .addAll(mapDiagnoses(bundle));

        gpSummary.getProblems()
            .addAll(mapProblems(bundle));
    }

    private static void validate(Bundle bundle) {
        getDomainResourceList(bundle, Condition.class).stream()
            .forEach(it -> {
                if (!it.getIdentifierFirstRep().hasValue()) {
                    throw new FhirValidationException("Condition.identifier.value is missing");
                }

                Coding coding = it.getCategoryFirstRep().getCodingFirstRep();
                if (!coding.getSystem().equals(SNOMED_SYSTEM)) {
                    throw new FhirValidationException("Invalid Condition.category.coding.system: " + coding.getSystem());
                }

                if (!coding.hasCode()) {
                    throw new FhirValidationException("Condition.category.coding.code is missing");
                }
            });
    }

    /**
     * List many diagnoses, that you may map each individual diagnosis.
     * @param bundle
     * @return
     */
    private static List<Diagnosis> mapDiagnoses(Bundle bundle) {
        return getDomainResourceList(bundle, Condition.class).stream()
            .filter(IS_DIAGNOSES)
            .map(condition -> mapDiagnosis(condition, bundle))
            .collect(Collectors.toList());
    }

    /**
     * List many problems, that you may map each individual problem.
     * @param bundle
     * @return
     */
    private static List<Problem> mapProblems(Bundle bundle) {
        var mapper = new ProblemMapper();
        return getDomainResourceList(bundle, Condition.class).stream()
            .filter(IS_PROBLEM)
            .map(condition -> mapper.mapProblem(condition))
            .collect(Collectors.toList());
    }

    /**
     * Map an individual diagnosis
     * @param condition
     * @param bundle
     * @return
     * @throws FhirMappingException
     */
    private static Diagnosis mapDiagnosis(Condition condition, Bundle bundle) throws FhirMappingException {
        var diagnosis = new Diagnosis();

        diagnosis.setIdRoot(condition.getIdentifierFirstRep().getValue());
        diagnosis.setCodeCode(condition.getCode().getCodingFirstRep().getCode());
        diagnosis.setCodeDisplayName(condition.getCode().getCodingFirstRep().getDisplay());
        diagnosis.setStatusCodeCode(mapStatus(condition));
        if (condition.hasOnsetDateTimeType()) {
            diagnosis.setEffectiveTimeLow(formatDateToHl7(condition.getOnsetDateTimeType()));
        }
        if (condition.hasOnsetPeriod()) {
            Period period = condition.getOnsetPeriod();
            diagnosis.setEffectiveTimeLow(formatDateToHl7(period.getStartElement()));
            diagnosis.setEffectiveTimeHigh(formatDateToHl7(period.getEndElement()));
        }
        Optional.ofNullable(condition.getEvidenceFirstRep().getDetailFirstRep().getReference())
            .map(reference -> reference.split("/")[1])
            .ifPresent(diagnosis::setFindingId);
        diagnosis.setSupportingInformation(condition.getNoteFirstRep().getText());

        LOGGER.debug("Looking up Encounter for Condition.id={}", condition.getIdElement().getIdPart());
        var encounterReference = condition.getEncounter().getReference();
        if (StringUtils.isNotBlank(encounterReference)) {
            var encounter = getResourceByReference(bundle, encounterReference, Encounter.class)
                .orElseThrow(() ->
                    new FhirValidationException(String.format("Bundle is Missing Encounter %s that is linked to Condition %s",
                        condition.getEncounter().getReference(), condition.getId())));

            for (var encounterParticipant : encounter.getParticipant()) {
                Coding coding = encounterParticipant.getTypeFirstRep().getCodingFirstRep();
                if (!PARTICIPATION_TYPE_SYSTEM.equals(coding.getSystem())) {
                    throw new FhirValidationException("Unsupported encounter participant system: " + coding.getSystem());
                }
                var code = coding.getCode();
                if ("AUT".equals(code)) {
                    var author = mapAuthor(bundle, encounterParticipant);
                    diagnosis.setAuthor(author);
                } else if ("INF".equals(code)) {
                    var informant = mapInformant(bundle, encounterParticipant);
                    diagnosis.setInformant(informant);
                } else {
                    throw new FhirValidationException(String.format("Invalid encounter %s participant code %s", encounter.getId(), code));
                }
            }
        }

        return diagnosis;
    }

    private static String mapStatus(Condition condition) {
        CodeableConcept status = getStatus(condition);
        switch (status.getCodingFirstRep().getCode()) {
            case "confirmed":
                return "completed";
            case "normal":
            case "active:":
                return "active";
            default:
                return "nullified";
        }
    }

    private static CodeableConcept getStatus(Condition condition) {
        if (condition.hasClinicalStatus()) {
            return condition.getClinicalStatus();
        } else if (condition.hasVerificationStatus()) {
            return condition.getVerificationStatus();
        } else {
            throw new FhirValidationException("Condition should have either clinicalStatus or verificationStatus");
        }
    }
}
