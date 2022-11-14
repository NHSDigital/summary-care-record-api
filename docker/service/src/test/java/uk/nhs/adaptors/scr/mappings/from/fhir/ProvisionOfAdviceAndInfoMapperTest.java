package uk.nhs.adaptors.scr.mappings.from.fhir;

import org.hl7.fhir.r4.model.Communication;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.adaptors.scr.models.GpSummary;
import uk.nhs.adaptors.scr.models.xml.ProvisionOfAdviceAndInformation;
import uk.nhs.adaptors.scr.utils.TemplateUtils;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class ProvisionOfAdviceAndInfoMapperTest extends BaseFhirMapperTest {
    @InjectMocks
    private ProvisionOfAdviceAndInfoMapper provisionOfAdviceAndInfoMapper;

    private static final String ID = "0F582D83-8F89-11EA-8B2D-B741F13EFC47";
    private static final String RESOURCE_DIRECTORY = "provision_of_advice_info";
    private static final String STATUS_CODE = "normal";
    private static final String FILE_NAME = "example";

    @Test
    public void When_MappingFromFHIR_Expect_RootId() {
        var communication = getFileAsObject(RESOURCE_DIRECTORY, FILE_NAME, Communication.class);

        returnExpectedUuid(ID);

        var result = provisionOfAdviceAndInfoMapper.mapProvisionOfAdviceInfo(communication);

        assertThat(result.getIdRoot()).isEqualTo(ID);

    }

    @Test
    public void When_MappingFromFHIR_Expect_Code() {
        var communication = getFileAsObject(RESOURCE_DIRECTORY, FILE_NAME, Communication.class);

        var result = provisionOfAdviceAndInfoMapper.mapProvisionOfAdviceInfo(communication);

        assertThat(result.getCodeCode()).isEqualTo("1240711000000104");
        assertThat(result.getCodeDisplayName())
            .isEqualTo("Educated about severe acute respiratory syndrome coronavirus 2 infection (situation)");
    }

    @Test
    public void When_MappingFromFHIR_Expect_StatusCode() {
        var communication = getFileAsObject(RESOURCE_DIRECTORY, FILE_NAME, Communication.class);

        var result = provisionOfAdviceAndInfoMapper.mapProvisionOfAdviceInfo(communication);

        assertThat(result.getStatusCodeCode()).isEqualTo(STATUS_CODE);
    }

    @Test
    public void When_MappingFromFHIR_Expect_EffectiveTimeLow() {
        var communication = getFileAsObject(RESOURCE_DIRECTORY, FILE_NAME, Communication.class);

        var result = provisionOfAdviceAndInfoMapper.mapProvisionOfAdviceInfo(communication);

        assertThat(result.getEffectiveTimeLow()).isEqualTo("20200805");

    }

    @Test
    public void When_MappingFromFHIR_Expect_MatchingHtml() {
        var expectedHtml = getExpectedHtml(RESOURCE_DIRECTORY, FILE_NAME);
        var communication = getFileAsObject(RESOURCE_DIRECTORY, FILE_NAME, Communication.class);

        returnExpectedUuid(ID);

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
