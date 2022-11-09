package uk.nhs.adaptors.scr.mappings.from.fhir;

import org.hl7.fhir.r4.model.Communication;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.adaptors.scr.models.GpSummary;
import uk.nhs.adaptors.scr.models.xml.ProvisionOfAdviceAndInformation;
import uk.nhs.adaptors.scr.utils.TemplateUtils;
import uk.nhs.utils.ProvisionOfAdviceInfoArgumentsProvider;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class ProvisionOfAdviceAndInfoMapperTest extends BaseFhirMapperTest {
    @InjectMocks
    private ProvisionOfAdviceAndInfoMapper provisionOfAdviceAndInfoMapper;

    private static final String ID = "0F582D83-8F89-11EA-8B2D-B741F13EFC47";
    private static final String RESOURCE_DIRECTORY = "provision_of_advice_info";
    private static final String STATUS_CODE = "normal";

    @ParameterizedTest(name = "[{index}] - {0}.html/json")
    @ArgumentsSource(ProvisionOfAdviceInfoArgumentsProvider.class)
    public void When_MappingFromFHIR_Expect_RootId(String fileName) {
        var json = getJsonExample(RESOURCE_DIRECTORY, fileName);

        returnExpectedUuid(ID);

        var communication = getFhirParser(json, Communication.class);

        var result = provisionOfAdviceAndInfoMapper.mapProvisionOfAdviceInfo(communication);

        assertThat(result.getIdRoot()).isEqualTo(ID);

    }

    @ParameterizedTest(name = "[{index}] - {0}.html/json")
    @ArgumentsSource(ProvisionOfAdviceInfoArgumentsProvider.class)
    public void When_MappingFromFHIR_Expect_Code(String fileName) {
        var json = getJsonExample(RESOURCE_DIRECTORY, fileName);

        var communication = getFhirParser(json, Communication.class);

        var result = provisionOfAdviceAndInfoMapper.mapProvisionOfAdviceInfo(communication);

        assertThat(result.getCodeCode()).isEqualTo("1240711000000104");
        assertThat(result.getCodeDisplayName())
            .isEqualTo("Educated about severe acute respiratory syndrome coronavirus 2 infection (situation)");
    }

    @ParameterizedTest(name = "[{index}] - {0}.html/json")
    @ArgumentsSource(ProvisionOfAdviceInfoArgumentsProvider.class)
    public void When_MappingFromFHIR_Expect_StatusCode(String fileName) {
        var json = getJsonExample(RESOURCE_DIRECTORY, fileName);

        var communication = getFhirParser(json, Communication.class);

        var result = provisionOfAdviceAndInfoMapper.mapProvisionOfAdviceInfo(communication);

        assertThat(result.getStatusCodeCode()).isEqualTo(STATUS_CODE);
    }

    @ParameterizedTest(name = "[{index}] - {0}.html/json")
    @ArgumentsSource(ProvisionOfAdviceInfoArgumentsProvider.class)
    public void When_MappingFromFHIR_Expect_EffectiveTimeLow(String fileName) {
        var json = getJsonExample(RESOURCE_DIRECTORY, fileName);

        var communication = getFhirParser(json, Communication.class);

        var result = provisionOfAdviceAndInfoMapper.mapProvisionOfAdviceInfo(communication);

        assertThat(result.getEffectiveTimeLow()).isEqualTo("20200805");

    }

    @ParameterizedTest(name = "[{index}] - {0}.html/json")
    @ArgumentsSource(ProvisionOfAdviceInfoArgumentsProvider.class)
    public void When_MappingFromFHIR_Expect_MatchingHtml(String fileName) {
        var expectedHtml = getExpectedHtml(RESOURCE_DIRECTORY, fileName);
        var json = getJsonExample(RESOURCE_DIRECTORY, fileName);

        returnExpectedUuid(ID);

        var communication = getFhirParser(json, Communication.class);

        var result = provisionOfAdviceAndInfoMapper.mapProvisionOfAdviceInfo(communication);

        var gpSummary = new GpSummary();
        var provisionsOfAdviceInfo = new ArrayList<ProvisionOfAdviceAndInformation>();
        provisionsOfAdviceInfo.add(result);
        gpSummary.setProvisionsOfAdviceAndInformationToPatientsAndCarers(provisionsOfAdviceInfo);

        var provisionOfAdviceInfoTemplate = TemplateUtils.loadPartialTemplate("ProvisionsOfAdviceInformation.mustache");

        var resultStr = TemplateUtils.fillTemplate(provisionOfAdviceInfoTemplate, gpSummary);
        assertThat(resultStr).isEqualTo(expectedHtml);
    }
}
