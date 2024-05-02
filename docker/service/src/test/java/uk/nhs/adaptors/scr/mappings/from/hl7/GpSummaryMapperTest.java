package uk.nhs.adaptors.scr.mappings.from.hl7;

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
import uk.nhs.adaptors.scr.utils.XmlUtils;

import javax.xml.xpath.XPathFactory;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class GpSummaryMapperTest extends BaseHL7MapperUtilities {
    @InjectMocks
    private GpSummaryMapper gpSummaryMapper;
    @Mock
    private AgentPersonMapper agentPersonMapper;
    @Mock
    private AgentPersonSdsMapper agentPersonSdsMapper;
    @Spy
    private HtmlParser htmlParser = new HtmlParser(new XmlUtils(XPathFactory.newInstance()));

    private static final String HTML_RESOURCE_DIRECTORY = "gp_summary/from/hl7";
    private static final String PARTIALS_RESOURCE_DIRECTORY = "gp_summary/partials";
    private static final String STANDARD_FILE_NAME = "standard_gp_summary";
    private static final String ADDITIONAL_INFO_FILE_NAME = "additional_information_gp_summary_1";
    private static final String STANDARD_COMPOSITION_FILE_NAME = "standard_gp_summary_composition";
    private static final String ADDITIONAL_INFO_COMPOSITION_FILE_NAME = "additional_information_gp_summary_1_composition";
    private FhirParser fhirParser = new FhirParser();

    @Test
    public void When_MappingGpSummaryFromHL7_Expect_BundleMatch() {
        var html = getHtmlExample(HTML_RESOURCE_DIRECTORY, STANDARD_FILE_NAME);
        var expectedJson = getJsonExample(PARTIALS_RESOURCE_DIRECTORY, STANDARD_COMPOSITION_FILE_NAME);

        var authorResources = prepStandardAuthorResources();

        doReturn(authorResources).when(agentPersonMapper).map(any());

        // act
        var results = gpSummaryMapper.map(html);

        // verify HTML Parser hit
        verifyHtmlParserHits();

        // verify composition is as expected (matches partial)
        var resultComposition = fhirParser.encodeToJson(results.get(0));
        assertThat(resultComposition).isEqualToIgnoringWhitespace(expectedJson);

        // verify author as expected
        var resultPractitionerRole = (PractitionerRole) results
            .stream()
            .filter(it -> it instanceof PractitionerRole)
            .findFirst()
            .get();
        var practitionerRoleCoding = resultPractitionerRole.getCodeFirstRep().getCodingFirstRep();
        assertThat(practitionerRoleCoding.getCode()).isEqualTo("NR0260");
        assertThat(practitionerRoleCoding.getDisplay()).isEqualTo("General Medical Practitioner");

        var resultPractitioner = (Practitioner) results
            .stream()
            .filter(it -> it instanceof Practitioner)
            .findFirst()
            .get();
        assertThat(resultPractitioner.getNameFirstRep().getText()).isEqualTo("Dr Mark Spencer");
    }

    @Test
    public void When_MappingAdditionalInformationGpSummaryFromHL7_Expect_BundleMatch() {
        var html = getHtmlExample(HTML_RESOURCE_DIRECTORY, ADDITIONAL_INFO_FILE_NAME);
        var expectedJson = getJsonExample(PARTIALS_RESOURCE_DIRECTORY, ADDITIONAL_INFO_COMPOSITION_FILE_NAME);

        var authorResources = prepAdditionalInfoAuthorResources();

        doReturn(authorResources).when(agentPersonSdsMapper).map(any());

        // act
        var results = gpSummaryMapper.map(html);

        // verify HTML Parser hit
        verifyHtmlParserHits();

        // verify composition is as expected (matches partial)
        var resultComposition = fhirParser.encodeToJson(results.get(0));
        assertThat(resultComposition).isEqualToIgnoringWhitespace(expectedJson);

        // verify author as expected
        var resultPractitionerRole = (PractitionerRole) results
            .stream()
            .filter(it -> it instanceof PractitionerRole)
            .findFirst()
            .get();
        var practitionerRoleCoding = resultPractitionerRole.getCodeFirstRep().getCodingFirstRep();
        assertThat(practitionerRoleCoding.getCode()).isEqualTo("NR0260");
        assertThat(practitionerRoleCoding.getDisplay()).isEqualTo("General Medical Practitioner");

        var resultPractitioner = (Practitioner) results
            .stream()
            .filter(it -> it instanceof Practitioner)
            .findFirst()
            .get();
        assertThat(resultPractitioner.getNameFirstRep().getText()).isEqualTo("Mr Bob Wilson");
        assertThat(resultPractitioner.getIdentifierFirstRep().getValue()).isEqualTo("676789689789");
    }

    private ArrayList<? extends Resource> prepStandardAuthorResources() {
        var resources = new ArrayList<Resource>();

        resources.add(getFileAsObject(PARTIALS_RESOURCE_DIRECTORY, "author_practitioner", Practitioner.class));
        resources.add(getFileAsObject(PARTIALS_RESOURCE_DIRECTORY, "author_practitioner_role", PractitionerRole.class));

        return resources;
    }

    private ArrayList<? extends Resource> prepAdditionalInfoAuthorResources() {
        var resources = new ArrayList<Resource>();

        var filename = "additional_info_1";

        resources.add(getFileAsObject(
            PARTIALS_RESOURCE_DIRECTORY, String.format("author_practitioner_%s", filename), Practitioner.class));
        resources.add(getFileAsObject(
            PARTIALS_RESOURCE_DIRECTORY, String.format("author_practitioner_role_%s", filename), PractitionerRole.class));

        return resources;
    }

    protected void verifyHtmlParserHits() {
        verify(htmlParser, times(1)).parse(any(Node.class));
    }
}
