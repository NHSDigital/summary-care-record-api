package uk.nhs.adaptors.scr.utils;

import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.InstantType;

import java.text.SimpleDateFormat;

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
                case SECOND:
                    sdfOutput = new SimpleDateFormat(DATE_TIME_PATTERN);
                    return sdfOutput.format(date.getValue());
                default:
                    return date.asStringValue();
            }
        }
        return null;
    }

    public static String formatTimestampToHl7(InstantType timestamp) {
        return new SimpleDateFormat(DATE_TIME_PATTERN).format(timestamp.getValue());
    }
}
