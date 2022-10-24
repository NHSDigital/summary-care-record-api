package uk.nhs.adaptors.scr.mappings.from.hl7;

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

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TreatmentsMapper implements XmlToFhirMapper {

    private final UuidWrapper uuid;
    private final CodedEntryMapper codedEntryMapper;
    private final XmlUtils xmlUtils;

    private static final String PERTINENT_CRET_BASE_PATH = "/pertinentInformation2/pertinentCREType[.//UKCT_MT144055UK01.Treatment]";
    private static final String TREATMENTS_BASE_PATH = "./component/UKCT_MT144055UK01.Treatment";
    private static final String UK_CORE_PROCEDURE_META = "https://fhir.hl7.org.uk/StructureDefinition/UKCore-Procedure";

    @Override
    public List<? extends Resource> map(Node document) {
        List<Resource> resources = new ArrayList<>();

        NodeList pertinentNodes = xmlUtils.getNodeListByXPath(document, PERTINENT_CRET_BASE_PATH);
        for (int i = 0; i < pertinentNodes.getLength(); i++) {
            Node pertinentCREType = xmlUtils.getNodeAndDetachFromParent(pertinentNodes, i);
            NodeList investigationNodes = xmlUtils.getNodeListByXPath(pertinentCREType, TREATMENTS_BASE_PATH);
            for (int j = 0; j < investigationNodes.getLength(); j++) {
                Node node = xmlUtils.getNodeAndDetachFromParent(investigationNodes, j);
                mapProcedure(resources, node);
            }
        }
        return resources;
    }

    private void mapProcedure(List<Resource> resources, Node node) {
        var procedure = new Procedure();

        procedure.setId(uuid.randomUuid());
        procedure.setMeta(new Meta().addProfile(UK_CORE_PROCEDURE_META));
        procedure.setStatus(Procedure.ProcedureStatus.COMPLETED);

        CodedEntry entry = codedEntryMapper.getCommonCodedEntryValues(node);

        var coding = new CodeableConcept().addCoding(new Coding()
            .setSystem(SNOMED_SYSTEM)
            .setCode(entry.getCodeValue())
            .setDisplay(entry.getCodeDisplay()));
        procedure.setCode(coding);

        entry.getEffectiveTimeLow().ifPresent(procedure::setPerformed);

        resources.add(procedure);
    }
}
