package uk.nhs.adaptors.scr.utils;

import lombok.extern.slf4j.Slf4j;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import uk.nhs.adaptors.scr.exceptions.FhirMappingException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.apache.commons.lang3.StringUtils.EMPTY;

@Slf4j
public class DateUtil {
    private static final String DATE_PATTERN = "yyyyMMddHHmmss";

    private static final String OUTPUT_PATTERN = "yyyyMMddHHmmss";
    private static final String FHIR_TIMESTAMP_INPUT_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";

    public static String formatDate(Date date) {
        if (date != null) {
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);
            return sdf.format(date);
        } else {
            return EMPTY;
        }
    }

    public static String formatTimestampFhirToHl7(String dateStr) {
        if (StringUtils.isNotBlank(dateStr)) {
            SimpleDateFormat sdfInput = new SimpleDateFormat(FHIR_TIMESTAMP_INPUT_PATTERN);
            SimpleDateFormat sdfOutput = new SimpleDateFormat(OUTPUT_PATTERN);

            try {
                Date date = sdfInput.parse(dateStr);
                return sdfOutput.format(date);
            } catch (ParseException e) {
                throw new FhirMappingException("Date in incorrect format", e);
            }
        } else {
            return StringUtils.EMPTY;
        }
    }

    public static String formatDateToHl7(Date date) {
        if (date == null) {
            return null;
        }
        SimpleDateFormat sdfOutput = new SimpleDateFormat(OUTPUT_PATTERN);
        return sdfOutput.format(date);
    }
}
