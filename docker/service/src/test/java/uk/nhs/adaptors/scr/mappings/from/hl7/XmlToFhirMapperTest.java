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

    @Test
    public void When_MappingGpSummaryFromHL7_Expect_BundleMatch() {

        BaseDateTimeType date;

        date = parseDate("20230713093455", InstantType.class);
        assertThat(date.getValue().toString()).isEqualTo("Mon Jul 13 09:34:55 GMT 2023");

        date = parseDate("20230713", InstantType.class);
        assertThat(date.getValue().toString()).isEqualTo("Mon Jul 13 00:00:00 GMT 2023");

        date = parseDate("202307", InstantType.class);
        assertThat(date.getValue().toString()).isEqualTo("Fri Jul 31 00:00:00 GMT 2023");

        date = parseDate("2023-07", InstantType.class);
        assertThat(date.getValue().toString()).isEqualTo("Wed Aug 31 00:00:00 BST 2022");

        date = parseDate("2023", InstantType.class);
        assertThat(date.getValue().toString()).isEqualTo("Sat Dec 31 00:00:00 GMT 2022");


        assertThrows(ScrBaseException.class, () -> {
            parseDate("-2023", InstantType.class);
        });

        assertThrows(ScrBaseException.class, () -> {
            parseDate("2023--07", InstantType.class);
        });
    }
}
