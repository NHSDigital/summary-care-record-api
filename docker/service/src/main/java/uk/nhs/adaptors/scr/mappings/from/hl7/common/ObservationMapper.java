package uk.nhs.adaptors.scr.mappings.from.hl7.common;

import lombok.RequiredArgsConstructor;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Period;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Node;
import uk.nhs.adaptors.scr.mappings.from.hl7.XmlToFhirMapper;

import static org.hl7.fhir.r4.model.Observation.ObservationStatus.ENTEREDINERROR;
import static org.hl7.fhir.r4.model.Observation.ObservationStatus.FINAL;
import static uk.nhs.adaptors.scr.mappings.from.hl7.XmlToFhirMapper.SNOMED_SYSTEM;
import static uk.nhs.adaptors.scr.utils.XmlUtils.getOptionalValueByXPath;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ObservationMapper {

    private static final String UK_CORE_OBSERVATION_META = "https://fhir.hl7.org.uk/StructureDefinition/UKCore-Observation";
    private static final String EFFECTIVE_TIME_CENTRE_XPATH = "./effectiveTime/centre/@value";

    private final CodedEntryMapper codedEntryMapper;

    public Observation mapObservation(Node node) {
        CodedEntry entry = codedEntryMapper.getCommonCodedEntryValues(node);
        var effectiveTimeCentre =
            getOptionalValueByXPath(node, EFFECTIVE_TIME_CENTRE_XPATH).map(XmlToFhirMapper::parseDate);

        var observation = new Observation();
        observation.setId(entry.getId());
        observation.setMeta(new Meta().addProfile(UK_CORE_OBSERVATION_META));
        observation.addIdentifier(new Identifier().setValue(entry.getId()));
        observation.setCode(new CodeableConcept().addCoding(new Coding()
            .setCode(entry.getCodeValue())
            .setSystem(SNOMED_SYSTEM)
            .setDisplay(entry.getCodeDisplay())));
        observation.setStatus(mapStatus(entry.getStatus()));

        if (entry.getEffectiveTimeLow().isPresent() || entry.getEffectiveTimeHigh().isPresent()) {
            var period = new Period();
            entry.getEffectiveTimeLow().ifPresent(period::setStart);
            entry.getEffectiveTimeHigh().ifPresent(period::setEnd);
            observation.setEffective(period);
        } else {
            effectiveTimeCentre
                .map(DateTimeType::new)
                .ifPresent(observation::setEffective);
        }
        return observation;
    }

    private static Observation.ObservationStatus mapStatus(String statusCode) {
        switch (statusCode) {
            case "normal":
            case "active":
            case "completed":
                return FINAL;
            case "nullified":
                return ENTEREDINERROR;
            default:
                throw new IllegalArgumentException(statusCode);
        }
    }
}
