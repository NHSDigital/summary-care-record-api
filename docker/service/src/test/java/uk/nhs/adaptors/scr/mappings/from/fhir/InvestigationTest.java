package uk.nhs.adaptors.scr.mappings.from.fhir;

import org.hl7.fhir.r4.model.Procedure;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.adaptors.scr.models.GpSummary;
import uk.nhs.adaptors.scr.models.xml.Investigation;
import uk.nhs.adaptors.scr.utils.TemplateUtils;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class InvestigationTest extends BaseFhirMapperTest {

    @InjectMocks
    private InvestigationMapper investigation;

    private static final String ID = "0F5A9E75-8F89-11EA-8B2D-B741F13EFC47";
    private static final String RESOURCE_DIRECTORY = "investigation";
    private static final String STATUS_CODE = "normal";
    private static final String FILE_NAME = "example";

    @Test
    public void When_MappingFromFHIR_Expect_RootId() {
        var procedure = getFileAsObject(RESOURCE_DIRECTORY, FILE_NAME, Procedure.class);

        returnExpectedUuid(ID);

        var result = investigation.mapInvestigation(procedure);

        assertThat(result.getIdRoot()).isEqualTo(ID);
    }

    @Test
    public void When_MappingFromFHIR_Expect_Code() {
        var procedure = getFileAsObject(RESOURCE_DIRECTORY, FILE_NAME, Procedure.class);

        var result = investigation.mapInvestigation(procedure);

        assertThat(result.getCodeCode()).isEqualTo("1240461000000109");
        assertThat(result.getCodeDisplayName())
            .isEqualTo("Measurement of severe acute respiratory syndrome coronavirus 2 antibody (procedure)");
    }

    @Test
    public void When_MappingFromFHIR_Expect_StatusCode() {
        var procedure = getFileAsObject(RESOURCE_DIRECTORY, FILE_NAME, Procedure.class);

        var result = investigation.mapInvestigation(procedure);

        assertThat(result.getStatusCodeCode()).isEqualTo(STATUS_CODE);
    }

    @Test
    public void When_MappingFromFHIR_Expect_EffectiveTimeLow() {
        var procedure = getFileAsObject(RESOURCE_DIRECTORY, FILE_NAME, Procedure.class);

        var result = investigation.mapInvestigation(procedure);

        assertThat(result.getEffectiveTimeLow()).isEqualTo("20200805");
    }

    @Test
    public void When_MappingFromFHIR_Expect_MatchingHtml() {
        var expectedHtml = getExpectedHtml(RESOURCE_DIRECTORY, FILE_NAME);
        var procedure = getFileAsObject(RESOURCE_DIRECTORY, FILE_NAME, Procedure.class);

        returnExpectedUuid(ID);

        var result = investigation.mapInvestigation(procedure);

        var gpSummary = new GpSummary();
        var investigations = new ArrayList<Investigation>();
        investigations.add(result);
        gpSummary.setInvestigations(investigations);

        var investigationsTemplate = TemplateUtils.loadPartialTemplate("Investigations.mustache");

        var resultStr = TemplateUtils.fillTemplate(investigationsTemplate, gpSummary);
        assertThat(resultStr).isEqualTo(expectedHtml);
    }
}
