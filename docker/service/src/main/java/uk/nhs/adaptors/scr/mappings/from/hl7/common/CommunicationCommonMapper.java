package uk.nhs.adaptors.scr.mappings.from.hl7.common;

import lombok.RequiredArgsConstructor;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Communication;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.List;

import static org.hl7.fhir.r4.model.Communication.CommunicationStatus.COMPLETED;
import static org.hl7.fhir.r4.model.Communication.CommunicationStatus.INPROGRESS;
import static org.hl7.fhir.r4.model.Communication.CommunicationStatus.NULL;
import static org.hl7.fhir.r4.model.Communication.CommunicationStatus.STOPPED;
import static uk.nhs.adaptors.scr.mappings.from.hl7.XmlToFhirMapper.SNOMED_SYSTEM;
import static uk.nhs.adaptors.scr.utils.XmlUtils.getNodesByXPath;
import static uk.nhs.adaptors.scr.utils.XmlUtils.getValueByXPath;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CommunicationCommonMapper {

    private static final String GP_SUMMARY_XPATH = "//QUPC_IN210000UK04/ControlActEvent/subject//GPSummary";
    private static final String UK_CORE_COMMUNICATION_PROFILE = "https://fhir.hl7.org.uk/StructureDefinition/UKCore-Encounter";
    private static final String PERTINENT_CRET_BASE_PATH = GP_SUMMARY_XPATH + "/pertinentInformation2/pertinentCREType[.//%s]";
    private static final String PERTINENT_CODE_CODE_XPATH = "./code/@code";
    private static final String PERTINENT_CODE_DISPLAY_XPATH = "./code/@displayName";
    private static final String ENTRY_BASE_PATH = "./component/%s";

    private final CodedEntryMapper codedEntryMapper;

    public List<Resource> map(Node document, String xmlNodeName) {
        List<Resource> resources = new ArrayList<>();
        for (var pertinentCREType : getNodesByXPath(document, String.format(PERTINENT_CRET_BASE_PATH, xmlNodeName))) {
            var pertinentCRETypeCode = getValueByXPath(pertinentCREType, PERTINENT_CODE_CODE_XPATH);
            var pertinentCRETypeDisplay = getValueByXPath(pertinentCREType, PERTINENT_CODE_DISPLAY_XPATH);
            for (Node node : getNodesByXPath(pertinentCREType, String.format(ENTRY_BASE_PATH, xmlNodeName))) {
                Communication communication = mapCommunication(node);

                communication.addCategory(new CodeableConcept(new Coding()
                        .setSystem(SNOMED_SYSTEM)
                        .setCode(pertinentCRETypeCode)
                        .setDisplay(pertinentCRETypeDisplay)
                    )
                );

                resources.add(communication);

            }
        }
        return resources;
    }

    private Communication mapCommunication(Node node) {
        CodedEntry entry = codedEntryMapper.getCommonCodedEntryValues(node);
        Communication communication = new Communication();
        communication.setId(entry.getId());
        communication.setMeta(new Meta().addProfile(UK_CORE_COMMUNICATION_PROFILE));
        communication.setStatus(mapStatus(entry.getStatus()));

        communication.setTopic(new CodeableConcept(new Coding()
                .setSystem(SNOMED_SYSTEM)
                .setCode(entry.getCodeValue())
                .setDisplay(entry.getCodeDisplay())
            )
        );

        entry.getEffectiveTimeLow().ifPresent(communication::setSent);

        return communication;
    }

    private static Communication.CommunicationStatus mapStatus(String status) {
        switch (status) {
            case "normal":
            case "completed":
                return COMPLETED;
            case "active":
                return INPROGRESS;
            case "aborted":
                return STOPPED;
            case "nullified":
                return NULL;
            default:
                throw new IllegalStateException("Invalid ProvisionOfAdviceAndInformation status: " + status);
        }
    }
}
