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

import java.util.Optional;

import static uk.nhs.adaptors.scr.mappings.from.fhir.ParticipantAgentMapper.mapAuthor;
import static uk.nhs.adaptors.scr.mappings.from.fhir.ParticipantAgentMapper.mapInformant;
import static uk.nhs.adaptors.scr.utils.DateUtil.formatDateToHl7;
import static uk.nhs.adaptors.scr.utils.FhirHelper.getDomainResourceList;
import static uk.nhs.adaptors.scr.utils.FhirHelper.getResourceByReference;

@Slf4j
public class ConditionMapper {

    private static final String PARTICIPATION_TYPE_SYSTEM = "http://terminology.hl7.org/CodeSystem/v3-ParticipationType";

    public static void mapConditions(GpSummary gpSummary, Bundle bundle) {
        getDomainResourceList(bundle, Condition.class).stream()
            .map(condition -> mapDiagnosis(condition, bundle))
            .forEach(diagnosis -> gpSummary.getDiagnoses().add(diagnosis));
    }

    private static Diagnosis mapDiagnosis(Condition condition, Bundle bundle) throws FhirMappingException {
        var diagnosis = new Diagnosis();

        diagnosis.setIdRoot(condition.getIdentifierFirstRep().getValue());
        diagnosis.setCodeCode(condition.getCode().getCodingFirstRep().getCode());
        diagnosis.setCodeDisplayName(condition.getCode().getCodingFirstRep().getDisplay());
        diagnosis.setStatusCodeCode(mapStatus(condition.getClinicalStatus()));
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

    private static String mapStatus(CodeableConcept status) {
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
}
