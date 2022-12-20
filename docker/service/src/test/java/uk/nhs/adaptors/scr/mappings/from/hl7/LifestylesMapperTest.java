package uk.nhs.adaptors.scr.mappings.from.hl7;

import java.util.ArrayList;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.PractitionerRole;
import org.hl7.fhir.r4.model.RelatedPerson;
import org.hl7.fhir.r4.model.Resource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.w3c.dom.Node;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

/**
 * Test runner for Lifestyle HL7 XML files' conversion to FHIR-JSON.
 *
 * @see: NIAD-2325
 */
@ExtendWith(MockitoExtension.class)
public class LifestylesMapperTest extends BaseHL7MapperTest {
    @Mock
    private ParticipantMapper participantMapper;

    @InjectMocks
    private LifestylesMapper mapper;

    private static final String UK_CORE_PROCEDURE_META = "https://fhir.hl7.org.uk/StructureDefinition/UKCore-Observation";
    private static final String RESOURCE_DIRECTORY = "lifestyle";
    private static final String PARTIALS_RESOURCE_DIRECTORY = "lifestyle/partials";
    private static final String CATEGORY_DISPLAY = "Lifestyle";
    private static final String FILE_NAME = "example";
    private static final String FILE_AS_BUNDLE = "as_bundle";
    private static final String ID = "5EDDDF8C-775A-4437-8990-41012DB32BD0";
    private static final String ID_ENCOUNTER = "5F748C32-4BE8-44C5-8E50-F640B2F4743E";
    private static final String INFORMATION_BASE_PATH = "/pertinentInformation2/pertinentCREType["
        + ".//UKCT_MT144036UK01.LifeStyle]";
    private static final String STATUS_CODE = "FINAL";

    /**
     * Load XML file from given path. Test some basic values to be as expected.
     * Tests ID, Meta, Profile, Status and Category.
     */
    @Test
    public void When_MappingFromHL7_Expect_FieldValues() {
        var html = getHtmlExample(RESOURCE_DIRECTORY, FILE_NAME);
        var result = mapper.map(html).get(0);
        var resultObservation = (Observation) result;
        assertThat(result.getId()).isEqualTo(ID);
        assertThat(result.getMeta().getProfile().get(0).getValue()).isEqualTo(UK_CORE_PROCEDURE_META);
        assertThat(resultObservation.getStatus().toString()).isEqualTo(STATUS_CODE);
        assertThat(resultObservation.getCategory().get(0).getCoding().get(0).getDisplay()).isEqualTo(CATEGORY_DISPLAY);
    }

    /**
     * Verify presence of XPath of INFORMATION_BASE_PATH.
     */
    @Test
    public void When_MappingFromHl7_Expect_XmlUtilsHit() {
        var html = getHtmlExample(RESOURCE_DIRECTORY, FILE_NAME);
        mapper.map(html);
        verifyXmlUtilsHits(html, INFORMATION_BASE_PATH);
    }

    /**
     * Test the presence/value of the Meta URL.
     */
    @Test
    public void When_MappingFromHl7_Expect_MetaUrl() {
        var html = getHtmlExample(RESOURCE_DIRECTORY, FILE_NAME);
        var result = mapper.map(html).get(0);
        var resultObservation = (Observation) result;
        assertThat(resultObservation.getMeta().getProfile().get(0).getValue()).isEqualTo(UK_CORE_PROCEDURE_META);
    }

    /**
     * Test the status code in the XML is as expected (STATUS_CODE value).
     */
    @Test
    public void When_MappingFromHl7_Expect_StatusCode() {
        var html = getHtmlExample(RESOURCE_DIRECTORY, FILE_NAME);
        var result = mapper.map(html).get(0);
        var resultObservation = (Observation) result;
        assertThat(resultObservation.getStatus().toString()).isEqualTo(STATUS_CODE);
    }

    /**
     * Test the presence of coded entry xml, i.e. code value, displayName, status and effective time.
     * @see uk.nhs.adaptors.scr.mappings.from.hl7.common.CodedEntryMapper
     */
    @Test
    public void When_MappingFromHl7_Expect_CodedEntryHit() {
        var html = getHtmlExample(RESOURCE_DIRECTORY, FILE_NAME);
        mapper.map(html);
        verifyCodedEntryHits();
    }

    /**
     * Test the values of coding/code section have mapped correctly.
     */
    @Test
    public void When_MappingFromHl7_Expect_CodingMapped() {
        var html = getHtmlExample(RESOURCE_DIRECTORY, FILE_NAME);
        var result = mapper.map(html).get(mapper.map(html).size());
        var resultObservation = (Observation) result;
        var codingFirstRep = resultObservation.getCode().getCodingFirstRep();

        assertThat(codingFirstRep.getCode()).isEqualTo("102906002");
        assertThat(codingFirstRep.getSystem()).isEqualTo("http://snomed.info/sct");
        assertThat(codingFirstRep.getDisplay()).isEqualTo("delinquent behaviour");
    }

    /**
     * Compares generated JSON by mapping the XML, against the expected JSON.
     */
    @Test
    public void When_MappingFromHl7_Expect_MatchJson() {
        returnExpectedUuid(ID_ENCOUNTER);
        var informantResources = prepInformantResources();
        var authorResources = prepAuthorResources();
        doReturn(authorResources).doReturn(informantResources).when(participantMapper).map(any(Node.class));

        var html = getHtmlExample(RESOURCE_DIRECTORY, FILE_NAME);
        var expectedJson = getJsonExample(RESOURCE_DIRECTORY, FILE_AS_BUNDLE);
        var results = mapper.map(html);
        var resultBundle = new Bundle();
        results.stream().map(resource -> getBundleEntryComponent(resource)).forEach(resultBundle::addEntry);
        var actualJson = encodeToJson(resultBundle);

        assertThat(actualJson).isEqualToIgnoringWhitespace(expectedJson);
    }

    /**
     * List related person and add to resources using partial JSON file.
     * @return resources
     */
    private ArrayList<? extends Resource> prepInformantResources() {
        var resources = new ArrayList<Resource>();
        var relatedPerson = getFileAsObject(PARTIALS_RESOURCE_DIRECTORY, "related_person", RelatedPerson.class);

        resources.add(relatedPerson);
        return resources;
    }

    /**
     * List practitioner and practioner roles and add them to resources using JSON partial files.
     * @return resources
     */
    private ArrayList<? extends Resource> prepAuthorResources() {
        var resources = new ArrayList<Resource>();
        var practitioner = getFileAsObject(PARTIALS_RESOURCE_DIRECTORY, "practitioner", Practitioner.class);
        var practitionerRole = getFileAsObject(PARTIALS_RESOURCE_DIRECTORY, "practitioner_role", PractitionerRole.class);

        resources.add(practitioner);
        resources.add(practitionerRole);
        return resources;
    }

    /**
     * Returns the Summary Care Record URL.
     * @return String URL
     */
    private String getScrUrl() {
        var baseUrl = "https://internal-dev.api.service.nhs.uk";
        var basePath = "summary-care-record/FHIR/R4";
        return String.format("%s/%s", baseUrl, basePath);
    }
}
