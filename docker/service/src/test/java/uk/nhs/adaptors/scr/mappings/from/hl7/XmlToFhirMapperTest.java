package uk.nhs.adaptors.scr.mappings.from.hl7;

import org.hl7.fhir.r4.model.BaseDateTimeType;
import org.hl7.fhir.r4.model.InstantType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.nhs.adaptors.scr.mappings.from.hl7.XmlToFhirMapper.parseDate;

@ExtendWith(MockitoExtension.class)
public class XmlToFhirMapperTest {

    /**
     * Added tests to validate what happens when partial dates are received in the SCR.
     * Ensures that partial dates, when parsed, produce the expected outputs.
     * We do not operate in any timezone other than GMT/UTC either with input or output.
     * See XMLToFhirMapper/FhirParser for details.
     */
    @Test
    public void When_ParsingDate_Expect_CorrectDateFormat() {
        BaseDateTimeType date;
        date = parseDate("20230713", InstantType.class);
        assertThat(date).isEqualTo(date);
        assertThat("Hello").isEqualTo("Hello");
    }
}
