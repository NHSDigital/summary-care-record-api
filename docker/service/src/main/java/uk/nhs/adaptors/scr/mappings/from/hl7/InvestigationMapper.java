package uk.nhs.adaptors.scr.mappings.from.hl7;

import lombok.RequiredArgsConstructor;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Procedure;
import org.hl7.fhir.r4.model.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.List;

import static uk.nhs.adaptors.scr.utils.XmlUtils.getNodesByXPath;
import static uk.nhs.adaptors.scr.utils.XmlUtils.getValueByXPath;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class InvestigationMapper implements XmlToFhirMapper {

    private static final String GP_SUMMARY_XPATH = "//QUPC_IN210000UK04/ControlActEvent/subject//GPSummary";
    private static final String PERTINENT_CRET_BASE_PATH =
        GP_SUMMARY_XPATH + "/pertinentInformation2/pertinentCREType[.//UKCT_MT144045UK01.Investigation]";
    private static final String PERTINENT_CODE_CODE_XPATH = "./code/@code";
    private static final String PERTINENT_CODE_DISPLAY_XPATH = "./code/@displayName";
    private static final String INVESTIGATION_BASE_PATH = "./component/UKCT_MT144045UK01.Investigation";

    private final ProcedureMapper procedureMapper;

    @Override
    public List<? extends Resource> map(Node document) {
        List<Resource> resources = new ArrayList<>();
        for (var pertinentCREType : getNodesByXPath(document, PERTINENT_CRET_BASE_PATH)) {
            var pertinentCRETypeCode = getValueByXPath(pertinentCREType, PERTINENT_CODE_CODE_XPATH);
            var pertinentCRETypeDisplay = getValueByXPath(pertinentCREType, PERTINENT_CODE_DISPLAY_XPATH);
            for (Node node : getNodesByXPath(pertinentCREType, INVESTIGATION_BASE_PATH)) {
                Procedure procedure = procedureMapper.mapProcedure(node);
                procedure.setCategory(new CodeableConcept(new Coding()
                    .setSystem(SNOMED_SYSTEM)
                    .setCode(pertinentCRETypeCode)
                    .setDisplay(pertinentCRETypeDisplay)));
                resources.add(procedure);
            }
        }
        return resources;
    }
}
