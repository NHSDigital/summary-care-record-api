package uk.nhs.adaptors.scr.mappings.from.fhir;

import org.hl7.fhir.r4.model.Communication;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.adaptors.scr.components.FhirParser;
import uk.nhs.adaptors.scr.mappings.from.common.UuidWrapper;
import uk.nhs.adaptors.scr.models.GpSummary;
import uk.nhs.adaptors.scr.models.xml.Treatment;
import uk.nhs.adaptors.scr.utils.TemplateUtils;
import uk.nhs.utils.ProvisionOfAdviceInfoArgumentsProvider;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.nhs.utils.Utils.readResourceFile;

@ExtendWith(MockitoExtension.class)
public class ProvisionOfAdviceAndInfoMapperTest {
    @InjectMocks
    private ProvisionOfAdviceAndInfoMapper provisionOfAdviceAndInfoMapper;

    @Mock
    private UuidWrapper uuid;

    private FhirParser fhirParser = new FhirParser();

    @ParameterizedTest(name = "[{index}] - {0}.html/json")
    @ArgumentsSource(ProvisionOfAdviceInfoArgumentsProvider.class)
    public void When_MappingFromFHIR_Expect_RootId(String fileName) {
        var json = readResourceFile(String.format("provision_of_advice_info/%s.json", fileName));

        when(uuid.randomUuid()).thenReturn("0F582D83-8F89-11EA-8B2D-B741F13EFC47");

        var communication = fhirParser.parseResource(json, Communication.class);

        var result = provisionOfAdviceAndInfoMapper.mapProvisionOfAdviceInfo(communication);

        assertThat(result.getIdRoot()).isEqualTo("0F582D83-8F89-11EA-8B2D-B741F13EFC47");

    }

    @ParameterizedTest(name = "[{index}] - {0}.html/json")
    @ArgumentsSource(ProvisionOfAdviceInfoArgumentsProvider.class)
    public void When_MappingFromFHIR_Expect_Code(String fileName) {
        var json = readResourceFile(String.format("provision_of_advice_info/%s.json", fileName));

        var communication = fhirParser.parseResource(json, Communication.class);

        var result = provisionOfAdviceAndInfoMapper.mapProvisionOfAdviceInfo(communication);

        assertThat(result.getCodeCode()).isEqualTo("1240711000000104");
        assertThat(result.getCodeDisplayName())
            .isEqualTo("Educated about severe acute respiratory syndrome coronavirus 2 infection (situation)");
    }

    @ParameterizedTest(name = "[{index}] - {0}.html/json")
    @ArgumentsSource(ProvisionOfAdviceInfoArgumentsProvider.class)
    public void When_MappingFromFHIR_Expect_StatusCode(String fileName) {
        var json = readResourceFile(String.format("provision_of_advice_info/%s.json", fileName));

        var communication = fhirParser.parseResource(json, Communication.class);

        var result = provisionOfAdviceAndInfoMapper.mapProvisionOfAdviceInfo(communication);

        assertThat(result.getStatusCodeCode()).isEqualTo("normal");
    }

    @ParameterizedTest(name = "[{index}] - {0}.html/json")
    @ArgumentsSource(ProvisionOfAdviceInfoArgumentsProvider.class)
    public void When_MappingFromFHIR_Expect_EffectiveTimeLow(String fileName) {
        var json = readResourceFile(String.format("provision_of_advice_info/%s.json", fileName));

        var communication = fhirParser.parseResource(json, Communication.class);

        var result = provisionOfAdviceAndInfoMapper.mapProvisionOfAdviceInfo(communication);

        assertThat(result.getEffectiveTimeLow()).isEqualTo("20200805");

    }

//    @ParameterizedTest(name = "[{index}] - {0}.html/json")
//    @ArgumentsSource(ProvisionOfAdviceInfoArgumentsProvider.class)
//    public void When_MappingFromFHIR_Expect_MatchingHtml(String fileName) {
//        var expectedHtml = readResourceFile(String.format("provision_of_advice_info/%s.html", fileName));
//        var json = readResourceFile(String.format("provision_of_advice_info/%s.json", fileName));
//
//        when(uuid.randomUuid()).thenReturn("0F582D83-8F89-11EA-8B2D-B741F13EFC47");
//
//        var communication = fhirParser.parseResource(json, Communication.class);
//
//        var result = provisionOfAdviceAndInfoMapper.mapProvisionOfAdviceInfo(communication);
//
//        var gpSummary = new GpSummary();
//        var treatment = new ArrayList<Treatment>();
//        treatment.add(result);
//        gpSummary.setTreatments(treatment);
//
//        var provision_of_advice_infoTemplate = TemplateUtils.loadPartialTemplate("Treatments.mustache");
//
//        var resultStr = TemplateUtils.fillTemplate(provision_of_advice_infoTemplate, gpSummary);
//        assertThat(resultStr).isEqualTo(expectedHtml);
//    }
}
