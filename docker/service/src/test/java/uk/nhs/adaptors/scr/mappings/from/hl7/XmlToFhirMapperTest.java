package uk.nhs.adaptors.scr.mappings.from.hl7;

import org.hl7.fhir.r4.model.BaseDateTimeType;
import org.hl7.fhir.r4.model.InstantType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.adaptors.scr.exceptions.ScrBaseException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
        assertThat(date.getValueAsString()).isEqualTo("2023-07-13");

        date = parseDate("202307", InstantType.class);
        assertThat(date.getValueAsString()).isEqualTo("2023-07-31T00:00:00.002+00:00");

        date = parseDate("2023-07", InstantType.class);
        assertThat(date.getValueAsString()).isEqualTo("2023-07-31T00:00:00.002+00:00");

        date = parseDate("2023", InstantType.class);
        assertThat(date.getValueAsString()).isEqualTo("2023");

        // FLAGSAPI-806
        date = parseDate("1", InstantType.class);
        assertThat(date.getValueAsString()).isEqualTo("1970-01-01");

        assertThrows(ScrBaseException.class, () -> {
            parseDate("-2023", InstantType.class);
        });

        assertThrows(ScrBaseException.class, () -> {
            parseDate("2023--07", InstantType.class);
        });
    }
}
