package uk.nhs.adaptors.scr.mappings.from.hl7;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Composition;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Node;
import uk.nhs.adaptors.scr.utils.XmlUtils;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.List;

import static uk.nhs.adaptors.scr.utils.FhirHelper.randomUUID;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class GpSummaryMapper implements XmlToFhirMapper {

    private static final String DATE_TIME_PATTERN = "yyyyMMddHHmmss";
    private static final String NHS_NUMBER_SYSTEM = "https://fhir.nhs.uk/Id/nhs-number";

    private static final String BASE_XPATH = "//QUPC_IN210000UK04/ControlActEvent/subject//GPSummary";

    private static final String GP_SUMMARY_ID_XPATH = BASE_XPATH + "/id/@root";
    private static final String GP_SUMMARY_CODE_CODE_XPATH = BASE_XPATH + "/code/@code";
    private static final String GP_SUMMARY_CODE_CODE_SYSTEM_XPATH = BASE_XPATH + "/code/@codeSystem";
    private static final String GP_SUMMARY_CODE_DISPLAY_NAME_XPATH = BASE_XPATH + "/code/@displayName";
    private static final String GP_SUMMARY_STATUS_CODE_XPATH = BASE_XPATH + "/statusCode/@code";
    private static final String GP_SUMMARY_EFFECTIVE_TIME_XPATH = BASE_XPATH + "/effectiveTime/@value";

    private static final String RECORD_TARGET_PATIENT_ID_EXTENSION_XPATH =
        BASE_XPATH + "/recordTarget/patient/id/@extension";

    private static final String REPLACEMENT_OF_PRIOR_MESSAGE_REF_ID_ROOT_XPATH =
        BASE_XPATH + "/replacementOf/priorMessageRef/id/@root";

    private static final String PERTINENT_ROOT_CRE_TYPE_CODE_CODE_XPATH =
        BASE_XPATH + "/pertinentInformation1/pertinentRootCREType/code/@code";
    private static final String PERTINENT_ROOT_CRE_TYPE_CODE_CODE_SYSTEM_XPATH =
        BASE_XPATH + "/pertinentInformation1/pertinentRootCREType/code/@codeSystem";
    private static final String PERTINENT_ROOT_CRE_TYPE_CODE_DISPLAY_NAME_XPATH =
        BASE_XPATH + "/pertinentInformation1/pertinentRootCREType/code/@displayName";

    private static final String PRESENTATION_TEXT_VALUE =
        BASE_XPATH + "/excerptFrom/UKCT_MT144051UK01.CareProfessionalDocumentationCRE/component/presentationText/value/html";

    private final HtmlParser htmlParser;

    @SneakyThrows
    public List<Resource> map(Node document) {
        var simpleDateFormat = new SimpleDateFormat(DATE_TIME_PATTERN);

        var gpSummaryId =
            XmlUtils.getValueByXPath(document, GP_SUMMARY_ID_XPATH);
        var gpSummaryCodeCode =
            XmlUtils.getValueByXPath(document, GP_SUMMARY_CODE_CODE_XPATH);
        var gpSummaryCodeCodeSystem =
            XmlUtils.getValueByXPath(document, GP_SUMMARY_CODE_CODE_SYSTEM_XPATH);
        var gpSummaryCodeDisplayName =
            XmlUtils.getValueByXPath(document, GP_SUMMARY_CODE_DISPLAY_NAME_XPATH);
        var gpSummaryStatusCode =
            XmlUtils.getValueByXPath(document, GP_SUMMARY_STATUS_CODE_XPATH);
        var gpSummaryEffectiveTime =
            simpleDateFormat.parse(XmlUtils.getValueByXPath(document, GP_SUMMARY_EFFECTIVE_TIME_XPATH));
        var recordTargetPatientIdExtension =
            XmlUtils.getValueByXPath(document, RECORD_TARGET_PATIENT_ID_EXTENSION_XPATH);
        var replacementOfPriorMessageRefIdRoot =
            XmlUtils.getValueByXPath(document, REPLACEMENT_OF_PRIOR_MESSAGE_REF_ID_ROOT_XPATH);
        var pertinentRootCreTypeCodeCode =
            XmlUtils.getValueByXPath(document, PERTINENT_ROOT_CRE_TYPE_CODE_CODE_XPATH);
        var pertinentRootCreTypeCodeCodeSystem =
            XmlUtils.getValueByXPath(document, PERTINENT_ROOT_CRE_TYPE_CODE_CODE_SYSTEM_XPATH);
        var pertinentRootCreTypeCodeDisplayName =
            XmlUtils.getValueByXPath(document, PERTINENT_ROOT_CRE_TYPE_CODE_DISPLAY_NAME_XPATH);
        var presentationTextValue =
            XmlUtils.getNodesByXPath(document, PRESENTATION_TEXT_VALUE).stream()
                .findFirst();

        var composition = new Composition();
        composition.setId(randomUUID());

        composition.setIdentifier(
            new Identifier()
                .setValue(gpSummaryId)
                .setSystem("https://tools.ietf.org/html/rfc4122"));

        composition.setType(
            new CodeableConcept().addCoding(new Coding()
                .setCode(gpSummaryCodeCode)
                .setSystem(gpSummaryCodeCodeSystem)
                .setDisplay(gpSummaryCodeDisplayName)));

        composition.setStatus(
            mapCompositionStatus(gpSummaryStatusCode));

        composition.setDate(gpSummaryEffectiveTime);

        Patient patient = getSubject(recordTargetPatientIdExtension);

        composition.setSubject(new Reference(patient));

        composition.addRelatesTo(
            new Composition.CompositionRelatesToComponent().setTarget(new Identifier()
                .setValue(replacementOfPriorMessageRefIdRoot))
                .setCode(Composition.DocumentRelationshipType.REPLACES));

        composition.addCategory(
            new CodeableConcept().addCoding(new Coding()
                .setCode(pertinentRootCreTypeCodeCode)
                .setSystem(pertinentRootCreTypeCodeCodeSystem)
                .setDisplay(pertinentRootCreTypeCodeDisplayName)));

        presentationTextValue
            .map(htmlParser::parse)
            .map(Collection::stream)
            .ifPresent(section -> section.forEach(composition::addSection));

        return List.of(composition, patient);
    }

    private Patient getSubject(String recordTargetPatientIdExtension) {
        Patient patient = new Patient();
        patient.setId(randomUUID());
        patient.addIdentifier(new Identifier()
            .setValue(recordTargetPatientIdExtension)
            .setSystem(NHS_NUMBER_SYSTEM));
        return patient;
    }

    private static Composition.CompositionStatus mapCompositionStatus(String compositionStatus) {
        switch (compositionStatus) {
            case "active":
                return Composition.CompositionStatus.FINAL;
            default:
                throw new IllegalArgumentException(String.format("Unable to map '%s'", compositionStatus));
        }
    }
}
