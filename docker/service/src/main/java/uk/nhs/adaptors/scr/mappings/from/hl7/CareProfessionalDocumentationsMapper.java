package uk.nhs.adaptors.scr.mappings.from.hl7;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import uk.nhs.adaptors.scr.mappings.from.common.UuidWrapper;
import uk.nhs.adaptors.scr.mappings.from.hl7.common.CodedEntry;
import uk.nhs.adaptors.scr.mappings.from.hl7.common.CodedEntryMapper;
import uk.nhs.adaptors.scr.utils.XmlUtils;
import static uk.nhs.adaptors.scr.utils.DateUtil.formatTimestampToFhir;

/**
 * Maps the Care Professional Documentation HL7 XML to FHIR JSON.
 *
 * @see: NIAD-2321
 */
@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CareProfessionalDocumentationsMapper implements XmlToFhirMapper {

    private final UuidWrapper uuid;
    private final CodedEntryMapper codedEntryMapper;
    private final XmlUtils xmlUtils;
    private static final String PERTINENT_CODE_CODE_XPATH = "./code/@code";
    private static final String PERTINENT_CODE_DISPLAY_XPATH = "./code/@displayName";
    private static final String PERTINENT_CRET_BASE_PATH = "/pertinentInformation2/pertinentCREType[."
        + "//UKCT_MT144035UK01.CareProfessionalDocumentation]";
    private static final String TREATMENTS_BASE_PATH = "./component/UKCT_MT144035UK01.CareProfessionalDocumentation";
    private static final String UK_CORE_PROCEDURE_META = "https://fhir.hl7.org.uk/StructureDefinition/UKCore-Communication";
    /**
     * List through all XML pertinent nodes (those following CRET path <UKCR...>).
     * Detach those XML nodes and run mapCommunication with them.
     *
     * @param document
     * @return
     */
    @Override
    public List<? extends Resource> map(Node document) {
        List<Resource> resources = new ArrayList<>();

        NodeList pertinentNodes = xmlUtils.getNodeListByXPath(document, PERTINENT_CRET_BASE_PATH);
        // Loop through <pertinentCREType> node.
        for (int i = 0; i < pertinentNodes.getLength(); i++) {
            Node pertinentCREType = xmlUtils.getNodeAndDetachFromParent(pertinentNodes, i);
            NodeList treatmentNodes = xmlUtils.getNodeListByXPath(pertinentCREType, TREATMENTS_BASE_PATH);
            // Get "category" code/display values.
            var pertinentCRETypeCode = xmlUtils.getValueByXPath(pertinentCREType, PERTINENT_CODE_CODE_XPATH);
            var pertinentCRETypeDisplay = xmlUtils.getValueByXPath(pertinentCREType, PERTINENT_CODE_DISPLAY_XPATH);
            // Loop through <UKCT..> node.
            for (int j = 0; j < treatmentNodes.getLength(); j++) {
                Node node = xmlUtils.getNodeAndDetachFromParent(treatmentNodes, j);
                mapCommunication(resources, pertinentCRETypeCode, pertinentCRETypeDisplay, node);
            }
        }
        return resources;
    }

    /**
     * Create new Communication JSON object, and set attributes accordingly.
     * @param resources
     * @param pertinentCRETypeCode Category Code
     * @param pertinentCRETypeDisplay Category Display
     * @param node
     */
    private void mapCommunication(List<Resource> resources, String pertinentCRETypeCode, String pertinentCRETypeDisplay, Node node) {
        CodedEntry entry = codedEntryMapper.getCommonCodedEntryValues(node);

        var communication = new Communication();
        communication.setId(entry.getId());
        communication.setMeta(new Meta().addProfile(UK_CORE_PROCEDURE_META));
        communication.addIdentifier().setValue(entry.getId());
        communication.setStatus(Communication.CommunicationStatus.COMPLETED);

        var topicCoding = new CodeableConcept().addCoding(new Coding()
            .setCode(entry.getCodeValue())
            .setSystem(SNOMED_SYSTEM)
            .setDisplay(entry.getCodeDisplay()));
        communication.setTopic(topicCoding);

        // Get effective time, convert to datetimetype, pass this to
        // formatTimestampToFhir, to convert this to Fhir compatible string with timezone.
        if (entry.getEffectiveTime().isPresent()) {
            var x = entry.getEffectiveTime().get().getValue();
            var y = new DateTimeType(x);
            var z = new DateTimeType(formatTimestampToFhir(y));
            communication.setSentElement(z);
        }

        resources.add(communication);
    }
}
