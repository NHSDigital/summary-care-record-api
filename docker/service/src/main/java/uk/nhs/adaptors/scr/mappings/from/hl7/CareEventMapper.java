package uk.nhs.adaptors.scr.mappings.from.hl7;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import uk.nhs.adaptors.scr.mappings.from.hl7.common.CodedEntry;
import uk.nhs.adaptors.scr.mappings.from.hl7.common.CodedEntryMapper;
import uk.nhs.adaptors.scr.mappings.from.hl7.common.UuidWrapper;
import uk.nhs.adaptors.scr.utils.XmlUtils;

import java.util.ArrayList;
import java.util.List;

import static uk.nhs.adaptors.scr.utils.FhirHelper.randomUUID;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CareEventMapper implements XmlToFhirMapper {

    private final UuidWrapper UUID;
    private final CodedEntryMapper codedEntryMapper;
    private final XmlUtils xmlUtils;

//    private static final String GP_SUMMARY_XPATH = "//QUPC_IN210000UK04/ControlActEvent/subject//GPSummary";
    private static final String PERTINENT_CRET_BASE_PATH = "/pertinentInformation2/pertinentCREType[.//UKCT_MT144037UK01.CareEvent]";
    private static final String PERTINENT_CODE_CODE_XPATH = "./code/@code";
    private static final String PERTINENT_CODE_DISPLAY_XPATH = "./code/@displayName";
    private static final String CARE_EVENT_BASE_PATH = "./component/UKCT_MT144037UK01.CareEvent";


    @Override
    public List<? extends Resource> map(Node document) {
        List<Resource> resources = new ArrayList<>();

        NodeList pertinentNodes = xmlUtils.getNodeListByXPath(document, PERTINENT_CRET_BASE_PATH);
        for (int i = 0; i < pertinentNodes.getLength(); i++) {
            Node pertinentCREType = xmlUtils.getNodeAndDetachFromParent(pertinentNodes, i);
            var pertinentCRETypeCode = xmlUtils.getValueByXPath(pertinentCREType, PERTINENT_CODE_CODE_XPATH);
            var pertinentCRETypeDisplay = xmlUtils.getValueByXPath(pertinentCREType, PERTINENT_CODE_DISPLAY_XPATH);
            NodeList careEventNodes = xmlUtils.getNodeListByXPath(pertinentCREType, CARE_EVENT_BASE_PATH);
            for (int j = 0; j < careEventNodes.getLength(); j++) {
                Node node = xmlUtils.getNodeAndDetachFromParent(careEventNodes, j);

                var careEvent = new Encounter();
                careEvent.setId(UUID.RandomUUID());

                CodedEntry entry = codedEntryMapper.getCommonCodedEntryValues(node);

                if (entry.getEffectiveTimeLow().isPresent() || entry.getEffectiveTimeHigh().isPresent()) {
                    var period = new Period();
                    entry.getEffectiveTimeLow().ifPresent(period::setEndElement);
                    entry.getEffectiveTimeHigh().ifPresent(period::setStartElement);
                    careEvent.setPeriod(period);
                }
                resources.add(careEvent);
            }
        }

        return resources;
    }
}
