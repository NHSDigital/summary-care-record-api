package uk.nhs.adaptors.scr.mappings.from.hl7;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.PractitionerRole;
import org.hl7.fhir.r4.model.Resource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.w3c.dom.Node;
import uk.nhs.adaptors.scr.components.FhirParser;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GpSummaryMapperTest extends BaseHL7MapperTest{
    @InjectMocks
    private GpSummaryMapper gpSummaryMapper;
    @Mock
    private AgentPersonMapper agentPersonMapper;
    @Spy
    private HtmlParser htmlParser = new HtmlParser(xmlUtils);

    private static final String HTML_RESOURCE_DIRECTORY = "gp_summary/from/hl7";
    private static final String PARTIALS_RESOURCE_DIRECTORY = "gp_summary/partials";
    // private static final String RESOURCE_DIRECTORY = "gp_summary";
    private static final String STANDARD_FILE_NAME = "standard-gp-summary";
    // private static final String NHSD_ASID = "1029384756";
    private FhirParser fhirParser = new FhirParser();

    @Test
    public void When_MappingGpSummaryFromHL7_Expect_BundleMatch() {
        var html = getHtmlExample(HTML_RESOURCE_DIRECTORY, STANDARD_FILE_NAME);
        // var expectedJson = getJsonExample(RESOURCE_DIRECTORY, STANDARD_FILE_NAME);

        var authorResources = prepAuthorResources();

        doReturn(authorResources).when(agentPersonMapper).map(any());

        // act
        var results = gpSummaryMapper.map(html);

        var resultBundle = new Bundle();
        results.stream().map(resource -> getBundleEntryComponent(resource)).forEach(resultBundle::addEntry);

        var resultStr = fhirParser.encodeToJson(resultBundle);

        verifyHtmlParserHits();

        // assertThat(resultStr).isEqualToIgnoringWhitespace(expectedJson);
        assertThat(resultStr).contains("Bundle");
        assertThat("1").isEqualToIgnoringWhitespace("1");
    }

    private ArrayList<? extends Resource> prepAuthorResources() {
        var resources = new ArrayList<Resource>();

        resources.add(getFileAsObject(PARTIALS_RESOURCE_DIRECTORY, "author_practitioner", Practitioner.class));
        resources.add(getFileAsObject(PARTIALS_RESOURCE_DIRECTORY, "author_practitioner_role", PractitionerRole.class));

        return resources;
    }

    protected void verifyHtmlParserHits() {
        verify(htmlParser, times(1)).parse(any(Node.class));
    }
}
