package uk.nhs.adaptors.scr.mappings.from.fhir;

import org.hl7.fhir.r4.model.Procedure;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.adaptors.scr.models.GpSummary;
import uk.nhs.adaptors.scr.models.xml.Treatment;
import uk.nhs.adaptors.scr.utils.TemplateUtils;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class TreatmentMapperTest extends BaseFhirMapperTest {

    @InjectMocks
    private TreatmentMapper treatmentMapper;

    private static final String ID = "0F5A9E72-8F89-11EA-8B2D-B741F13EFC47";
    private static final String RESOURCE_DIRECTORY = "treatments";
    private static final String STATUS_CODE = "normal";
    private static final String FILE_NAME = "example";

    @Test
    public void When_MappingFromFHIR_Expect_RootId() {
        var json = getJsonExample(RESOURCE_DIRECTORY, FILE_NAME);
        returnExpectedUuid(ID);
        var procedure = getFhirParser(json, Procedure.class)
        var result = treatmentMapper.mapTreatment(procedure);

        assertThat(result.getIdRoot()).isEqualTo(ID);

    }

    @Test
    public void When_MappingFromFHIR_Expect_Code() {
        var json = getJsonExample(RESOURCE_DIRECTORY, FILE_NAME);

        var procedure = getFhirParser(json, Procedure.class);

        var result = treatmentMapper.mapTreatment(procedure);

        assertThat(result.getCodeCode()).isEqualTo("1240491000000103");
        assertThat(result.getCodeDisplayName())
            .isEqualTo("Severe acute respiratory syndrome coronavirus 2 vaccination (procedure)");
    }

    @Test
    public void When_MappingFromFHIR_Expect_StatusCode() {
        var json = getJsonExample(RESOURCE_DIRECTORY, FILE_NAME);

        var procedure = getFhirParser(json, Procedure.class);

        var result = treatmentMapper.mapTreatment(procedure);

        assertThat(result.getStatusCodeCode()).isEqualTo(STATUS_CODE);
    }

    @Test
    public void When_MappingFromFHIR_Expect_EffectiveTimeLow() {
        var json = getJsonExample(RESOURCE_DIRECTORY, FILE_NAME);

        var procedure = getFhirParser(json, Procedure.class);

        var result = treatmentMapper.mapTreatment(procedure);

        assertThat(result.getEffectiveTimeLow()).isEqualTo("20200805");

    }

    @Test
    public void When_MappingFromFHIR_Expect_MatchingHtml() {
        var expectedHtml = getExpectedHtml(RESOURCE_DIRECTORY, FILE_NAME);
        var json = getJsonExample(RESOURCE_DIRECTORY, FILE_NAME);

        returnExpectedUuid(ID);

        var procedure = getFhirParser(json, Procedure.class);

        var result = treatmentMapper.mapTreatment(procedure);

        var gpSummary = new GpSummary();
        var treatment = new ArrayList<Treatment>();
        treatment.add(result);
        gpSummary.setTreatments(treatment);

        var treatmentsTemplate = TemplateUtils.loadPartialTemplate("Treatments.mustache");

        var resultStr = TemplateUtils.fillTemplate(treatmentsTemplate, gpSummary);
        assertThat(resultStr).isEqualTo(expectedHtml);
    }
}
