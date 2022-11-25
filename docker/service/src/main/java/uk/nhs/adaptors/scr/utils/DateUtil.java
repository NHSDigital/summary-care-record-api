package uk.nhs.adaptors.scr.utils;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.InstantType;


@Slf4j
public class DateUtil {
    private static final String DATE_TIME_PATTERN = "yyyyMMddHHmmss";
    private static final String DATE_PATTERN = "yyyyMMdd";

    public static String formatDateToHl7(DateTimeType date) {
        if (date.hasValue()) {
            SimpleDateFormat sdfOutput;
            switch (date.getPrecision()) {
                case DAY:
                    sdfOutput = new SimpleDateFormat(DATE_PATTERN);
                    return sdfOutput.format(date.getValue());
                case MINUTE:
                case SECOND:
                case MILLI:
                    return LocalDateTime.ofInstant(date.toCalendar().toInstant(), ZoneId.of("Europe/London"))
                        .format(DateTimeFormatter.ofPattern(DATE_TIME_PATTERN));
                default:
                    return date.asStringValue();
            }
        }
        return null;
    }

    public static String formatTimestampToHl7(InstantType timestamp) {
        return new SimpleDateFormat(DATE_TIME_PATTERN).format(timestamp.getValue());
    }

    /**
     * Takes a datetime object and returns a date string with the correct timezone.
     * @param datetime
     * @return E.g. 2018-06-21T16:11:51+01:00 is returned, showing a time in BST.
     */
    public static String formatTimestampToFhir(DateTimeType datetime) {
        SimpleDateFormat sd1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZZZZZ");

        return LocalDateTime.parse(
            sd1.format(datetime.getValue()),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ", Locale.UK)
        )
        .atZone(
            ZoneId.of("Europe/London")
        )
        .format(formatter);
    }

}
