package uk.nhs.adaptors.scr.mappings.from.hl7;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.PractitionerRole;
import org.hl7.fhir.r4.model.Resource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.w3c.dom.Node;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
public class FamilyHistoriesMapperTest extends BaseHL7MapperTest {

    @Mock
    private ParticipantMapper participantMapper;

    @InjectMocks
    private FamilyHistoriesMapper familyHistoryMapper;

    private static final String UK_CORE_PROCEDURE_META = "https://fhir.hl7.org.uk/StructureDefinition/UKCore-Observation";
    private static final String RESOURCE_DIRECTORY = "family_history";
    private static final String PARTIALS_RESOURCE_DIRECTORY = "family_history/partials";
    private static final String CATEGORY_DISPLAY = "FamilyHistory";
    private static final String FILE_NAME = "example";
    private static final String FILE_AS_BUNDLE = "as_bundle";
    private static final String ID = "51089E5B-0840-4237-8D91-CFC0238E83B4";
    private static final String ID_ENCOUNTER = "0G582D97-8G89-11EA-8B4G-B741F13EFC48";
    private static final String INFORMATION_BASE_PATH = "/pertinentInformation2/pertinentCREType["
        + ".//UKCT_MT144044UK01.FamilyHistory]";
    private static final String STATUS_CODE = "FINAL";

    @Test
    public void When_MappingFromHL7_Expect_FieldValues() {
        var html = getHtmlExample(RESOURCE_DIRECTORY, FILE_NAME);
        var result = familyHistoryMapper.map(html).get(0);
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
        familyHistoryMapper.map(html);
        verifyXmlUtilsHits(html, INFORMATION_BASE_PATH);
    }

    /**
     * Test the presence/value of the Meta URL.
     */
    @Test
    public void When_MappingFromHl7_Expect_MetaUrl() {
        var html = getHtmlExample(RESOURCE_DIRECTORY, FILE_NAME);
        var result = familyHistoryMapper.map(html).get(0);
        var resultObservation = (Observation) result;
        assertThat(resultObservation.getMeta().getProfile().get(0).getValue()).isEqualTo(UK_CORE_PROCEDURE_META);
    }

    /**
     * Test the status code in the XML is as expected (STATUS_CODE value).
     */
    @Test
    public void When_MappingFromHl7_Expect_StatusCode() {
        var html = getHtmlExample(RESOURCE_DIRECTORY, FILE_NAME);
        var result = familyHistoryMapper.map(html).get(0);
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
        familyHistoryMapper.map(html);
        verifyCodedEntryHits();
    }

    /**
     * Test the values of coding/code section have mapped correctly.
     */
    @Test
    public void When_MappingFromHl7_Expect_CodingMapped() {
        var html = getHtmlExample(RESOURCE_DIRECTORY, FILE_NAME);
        var result = familyHistoryMapper.map(html).get(familyHistoryMapper.map(html).size());
        var resultObservation = (Observation) result;
        var codingFirstRep = resultObservation.getCode().getCodingFirstRep();

        assertThat(codingFirstRep.getCode()).isEqualTo("289916006");
        assertThat(codingFirstRep.getSystem()).isEqualTo("http://snomed.info/sct");
        assertThat(codingFirstRep.getDisplay()).isEqualTo("family history of kidney disease");
    }

    /**
     * Compares generated JSON by mapping the XML, against the expected JSON.
     */
    @Test
    public void When_MappingFromHl7_Expect_MatchJson() {
        returnExpectedUuid(ID_ENCOUNTER);
        var informantResources = prepAuthorResources();
        var authorResources = prepAuthorResources();
        doReturn(authorResources).doReturn(informantResources).when(participantMapper).map(any(Node.class));

        var html = getHtmlExample(RESOURCE_DIRECTORY, FILE_NAME);
        var expectedJson = getJsonExample(RESOURCE_DIRECTORY, FILE_AS_BUNDLE);
        var results = familyHistoryMapper.map(html);
        var resultBundle = new Bundle();
        results.stream().map(resource -> getBundleEntryComponent(resource)).forEach(resultBundle::addEntry);
        var actualJson = encodeToJson(resultBundle);

        assertThat(actualJson).isEqualToIgnoringWhitespace(expectedJson);
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
     * Generate reference uri to interlink the various JSON sections for authors/participants.
     * @param resource
     * @return BundleEntryComponent
     */
    private Bundle.BundleEntryComponent getBundleEntryComponent(Resource resource) {
        return new Bundle.BundleEntryComponent()
            .setFullUrl(getScrUrl() + "/" + resource.getResourceType() + "/" + resource.getId())
            .setResource(resource);
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
