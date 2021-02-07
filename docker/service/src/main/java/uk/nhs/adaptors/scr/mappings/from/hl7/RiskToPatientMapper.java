package uk.nhs.adaptors.scr.mappings.from.hl7;

import lombok.RequiredArgsConstructor;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Node;
import uk.nhs.adaptors.scr.mappings.from.hl7.common.ObservationMapper;
import uk.nhs.adaptors.scr.utils.XmlUtils;

import java.util.ArrayList;
import java.util.List;

import static uk.nhs.adaptors.scr.utils.XmlUtils.getValueByXPath;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RiskToPatientMapper implements XmlToFhirMapper {
    private static final String GP_SUMMARY_XPATH = "//QUPC_IN210000UK04/ControlActEvent/subject//GPSummary";
    private static final String PERTINENT_CRET_BASE_PATH =
        GP_SUMMARY_XPATH + "/pertinentInformation2/pertinentCREType[.//UKCT_MT144054UK01.RiskToPatient]";
    private static final String PERTINENT_CODE_CODE_XPATH = "./code/@code";
    private static final String PERTINENT_CODE_DISPLAY_XPATH = "./code/@displayName";
    private static final String RISK_BASE_PATH = "./component/UKCT_MT144054UK01.RiskToPatient";

    private final ObservationMapper observationMapper;

    @Override
    public List<? extends Resource> map(Node document) {
        var resources = new ArrayList<Resource>();
        for (var pertinentCREType : XmlUtils.getNodesByXPath(document, PERTINENT_CRET_BASE_PATH)) {
            var pertinentCRETypeCode = getValueByXPath(pertinentCREType, PERTINENT_CODE_CODE_XPATH);
            var pertinentCRETypeDisplay = getValueByXPath(pertinentCREType, PERTINENT_CODE_DISPLAY_XPATH);
            for (var node : XmlUtils.getNodesByXPath(pertinentCREType, RISK_BASE_PATH)) {
                Observation observation = observationMapper.mapObservation(node);

                observation.addCategory(new CodeableConcept(new Coding()
                    .setSystem(SNOMED_SYSTEM)
                    .setCode(pertinentCRETypeCode)
                    .setDisplay(pertinentCRETypeDisplay)));

                resources.add(observation);
            }
        }
        return resources;
    }
}
