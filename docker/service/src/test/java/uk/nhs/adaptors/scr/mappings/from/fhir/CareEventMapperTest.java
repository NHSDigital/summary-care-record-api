package uk.nhs.adaptors.scr.mappings.from.fhir;

import org.hl7.fhir.r4.model.Encounter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.adaptors.scr.models.GpSummary;
import uk.nhs.adaptors.scr.models.xml.CareEvent;
import uk.nhs.adaptors.scr.utils.TemplateUtils;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class CareEventMapperTest extends BaseFhirMapperTest {

    @InjectMocks
    private CareEventMapper careEvent;

    private static final String ID = "0F582D91-8F89-11EA-8B2D-B741F13EFC47";
    private static final String RESOURCE_DIRECTORY = "care_event";
    private static final String STATUS_CODE = "normal";
    private static final String FILE_NAME = "example";

    @Test
    public void When_MappingFromFHIR_Expect_RootId() {
        var encounter = getFileAsObject(RESOURCE_DIRECTORY, FILE_NAME, Encounter.class);

        returnExpectedUuid(ID);

        var result = careEvent.mapCareEvent(encounter);

        assertThat(result.getIdRoot()).isEqualTo(ID);
    }

    @Test
    public void When_MappingFromFHIR_Expect_Code() {
        var encounter = getFileAsObject(RESOURCE_DIRECTORY, FILE_NAME, Encounter.class);

        var result = careEvent.mapCareEvent(encounter);

        assertThat(result.getCodeCode()).isEqualTo("1240631000000102");
        assertThat(result.getCodeDisplayName())
            .isEqualTo("Did not attend SARS-CoV-2 (severe acute respiratory syndrome coronavirus 2) vaccination");
    }

    @Test
    public void When_MappingFromFHIR_Expect_StatusCode() {
        var encounter = getFileAsObject(RESOURCE_DIRECTORY, FILE_NAME, Encounter.class);

        var result = careEvent.mapCareEvent(encounter);

        assertThat(result.getStatusCodeCode()).isEqualTo(STATUS_CODE);
    }

    @Test
    public void When_MappingFromFHIR_Expect_EffectiveTimeLow() {
        var encounter = getFileAsObject(RESOURCE_DIRECTORY, FILE_NAME, Encounter.class);

        var result = careEvent.mapCareEvent(encounter);

        assertThat(result.getEffectiveTimeLow()).isEqualTo("20200805");
    }

    @Test
    public void When_MappingFromFHIR_Expect_MatchingHtml() {
        var expectedHtml = getExpectedHtml(RESOURCE_DIRECTORY, FILE_NAME);
        var encounter = getFileAsObject(RESOURCE_DIRECTORY, FILE_NAME, Encounter.class);

        returnExpectedUuid(ID);

        var result = careEvent.mapCareEvent(encounter);

        var gpSummary = new GpSummary();
        var careEvents = new ArrayList<CareEvent>();
        careEvents.add(result);
        gpSummary.setCareEvents(careEvents);

        var careEventsTemplate = TemplateUtils.loadPartialTemplate("CareEvents.mustache");

        var resultStr = TemplateUtils.fillTemplate(careEventsTemplate, gpSummary);
        assertThat(resultStr).isEqualTo(expectedHtml);
    }

}
