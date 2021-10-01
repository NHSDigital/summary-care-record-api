package uk.nhs.adaptors.scr.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;

import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.InstantType;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class DateUtilTest {

    @ParameterizedTest(name = "formatDateToHl7 {0}")
    @MethodSource("dates")
    public void formatDateToHl7ShouldReturnDateInCorrectFormat(DateTimeType date, String expectedDate) {
        assertThat(DateUtil.formatDateToHl7(date)).isEqualTo(expectedDate);
    }

    @ParameterizedTest(name = "formatTimestampToHl7 {0}")
    @MethodSource("timestamps")
    public void formatTimestampToHl7ShouldReturnDateInCorrectFormat(InstantType timestamp, String expectedDate) {
        assertThat(DateUtil.formatTimestampToHl7(timestamp)).isEqualTo(expectedDate);
    }

    private static Stream<Arguments> dates() {
        return Stream.of(
            Arguments.of(
                new DateTimeType("2021-09-25T01:00:00.000+00:00"),
                "20210925020000"
            ),
            Arguments.of(
                new DateTimeType("2021-09-25T01:00:00+00:00"),
                "20210925020000"
            ),
            Arguments.of(
                new DateTimeType("2021-09-25"),
                "20210925"
            ),
            Arguments.of(
                new DateTimeType("2021-09"),
                "2021-09"
            )
        );
    }

    private static Stream<Arguments> timestamps() {
        return Stream.of(
            Arguments.of(
                new InstantType("2021-09-25T01:00:00"),
                "20210925010000"
            ),
            Arguments.of(
                new InstantType("2021-09-25T01:00:00.000"),
                "20210925010000"
            ),
            Arguments.of(
                new InstantType("2021-09-25T01:00:00.000+00:00"),
                "20210925030000"
            ),
            Arguments.of(
                new InstantType("2021-09-25T01:00:00+01:00"),
                "20210925020000"
            )
        );
    }
}
