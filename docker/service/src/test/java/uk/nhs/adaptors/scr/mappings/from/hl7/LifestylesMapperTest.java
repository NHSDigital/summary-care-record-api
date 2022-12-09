package uk.nhs.adaptors.scr.mappings.from.hl7;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Resource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

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
    private static final String CATEGORY_DISPLAY = "Lifestyle";
    private static final String FILE_NAME = "example";
    private static final String ID = "5EDDDF8C-775A-4437-8990-41012DB32BD0";
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
        /*
        var html = getHtmlExample(RESOURCE_DIRECTORY, FILE_NAME);
        var result = mapper.map(html).get(mapper.map(html).size());
        var resultObservation = (Observation) result;
        var codingFirstRep = resultObservation.getCode().getCodingFirstRep();

        assertThat(codingFirstRep.getCode()).isEqualTo("102906002");
        assertThat(codingFirstRep.getSystem()).isEqualTo("http://snomed.info/sct");
        assertThat(codingFirstRep.getDisplay()).isEqualTo("delinquent behaviour");
        */
    }

    @Test
    public void When_MappingFromHl7_Expect_MatchJson() {
//        var html = getHtmlExample(RESOURCE_DIRECTORY, FILE_NAME);
//        var result = mapper.map(html).get(0);
//        var actualJson = encodeToJson(result);
//        var expectedJson = getJsonExample(RESOURCE_DIRECTORY, FILE_NAME);


        when(participantMapper.map(any())).thenReturn(

        );

        var html = getHtmlExample(RESOURCE_DIRECTORY, FILE_NAME);
        var expectedJson = getJsonExample(RESOURCE_DIRECTORY, FILE_NAME);
        var results = mapper.map(html);
        var resultBundle = new Bundle();
        results.stream().map(resource -> getBundleEntryComponent(resource)).forEach(resultBundle::addEntry);
        var actualJson = encodeToJson(resultBundle);
        assertThat(actualJson).isEqualToIgnoringWhitespace(expectedJson);
    }

    private BundleEntryComponent getBundleEntryComponent(Resource resource) {
        return new BundleEntryComponent()
            .setFullUrl(getScrUrl() + "/" + resource.getResourceType() + "/" + resource.getId())
            .setResource(resource);
    }

    private String getScrUrl() {
        var baseUrl = "https://internal-dev.api.service.nhs.uk";
        var basePath = "summary-care-record/FHIR/R4";
        return String.format("%s/%s", baseUrl, basePath);
    }
}




































