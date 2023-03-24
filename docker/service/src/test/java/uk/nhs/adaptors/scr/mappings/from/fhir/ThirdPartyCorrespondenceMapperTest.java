package uk.nhs.adaptors.scr.mappings.from.fhir;

import org.hl7.fhir.r4.model.Communication;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.adaptors.scr.models.GpSummary;
import uk.nhs.adaptors.scr.models.xml.ThirdPartyCorrespondence;
import uk.nhs.adaptors.scr.utils.TemplateUtils;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class ThirdPartyCorrespondenceMapperTest extends BaseFhirMapperTest {

    @InjectMocks
    private ThirdPartyCorrespondenceMapper thirdPartyCorrMapper;

    private static final String ID = "7D50E3C0-7565-11E8-AEC7-950876D8FD27";
    private static final String RESOURCE_DIRECTORY = "third_party_correspondence";
    private static final String STATUS_CODE = "normal";
    private static final String FILE_NAME = "example";

    @Test
    public void When_MappingFromFHIR_Expect_RootId() {
        var communication = getFileAsObject(RESOURCE_DIRECTORY, FILE_NAME, Communication.class);
        var result = thirdPartyCorrMapper.mapThirdPartyCorrespondence(communication);

        assertThat(result.getIdRoot()).isEqualTo(ID);
    }

    @Test
    public void When_MappingFromFHIR_Expect_Code() {
        var communication = getFileAsObject(RESOURCE_DIRECTORY, FILE_NAME, Communication.class);


        var result = thirdPartyCorrMapper.mapThirdPartyCorrespondence(communication);

        assertThat(result.getCodeCode()).isEqualTo("415171009");
        assertThat(result.getCodeDisplayName())
                .isEqualTo("Prescribing observations");
    }

    @Test
    public void When_MappingFromFHIR_Expect_StatusCode() {
        var communication = getFileAsObject(RESOURCE_DIRECTORY, FILE_NAME, Communication.class);

        var result = thirdPartyCorrMapper.mapThirdPartyCorrespondence(communication);

        assertThat(result.getStatusCodeCode()).isEqualTo(STATUS_CODE);
    }

    @Test
    public void When_MappingFromFHIR_Expect_EffectiveTimeLow() {
        var communication = getFileAsObject(RESOURCE_DIRECTORY, FILE_NAME, Communication.class);

        var result = thirdPartyCorrMapper.mapThirdPartyCorrespondence(communication);

        assertThat(result.getEffectiveTimeLow()).isEqualTo("20160217135855");
    }

    @Test
    public void When_MappingFromFHIR_Expect_MatchingHtml() {
        var expectedHtml = getExpectedHtml(RESOURCE_DIRECTORY, FILE_NAME);
        var communication = getFileAsObject(RESOURCE_DIRECTORY, FILE_NAME, Communication.class);

        //Headers expected to appear in the message object in the final html.
        Map<String, String> additionalInformationHeaders = new HashMap() {
            {
                put("Clinical Observations and Findings", "ObservationsHeader");
            }
        };

        var result = thirdPartyCorrMapper.mapAdditionalInformationButtonEntry(communication, additionalInformationHeaders);

        var gpSummary = new GpSummary();
        var thirdpartyCorr = new ArrayList<ThirdPartyCorrespondence>();
        thirdpartyCorr.add(result);
        gpSummary.setThirdPartyCorrespondences(thirdpartyCorr);

        var thirdPartyCorrTemplate = TemplateUtils.loadPartialTemplate("ThirdPartyCorrespondences.mustache");

        var resultStr = TemplateUtils.fillTemplate(thirdPartyCorrTemplate, gpSummary);
        assertThat(resultStr).isEqualTo(expectedHtml);
    }
}
