package uk.nhs.adaptors.scr.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import uk.nhs.adaptors.scr.exceptions.FhirMappingException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Slf4j
public class DateUtil {

    public static final String INPUT_PATTERN = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    private static final String OUTPUT_PATTERN = "yyyyMMddHHmmss";
    private static final String FHIR_INPUT_PATTERN = "yyyy-MM-dd'T'HH:mm:ssXXX";
    private static final String FHIR_TIMESTAMP_INPUT_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";

    public static String formatDate(String dateStr) {
        if (StringUtils.isNotBlank(dateStr)) {
            SimpleDateFormat sdfInput = new SimpleDateFormat(INPUT_PATTERN);
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

    public static String formatDateFhirToHl7(String dateStr) {
        if (StringUtils.isNotBlank(dateStr)) {
            SimpleDateFormat sdfInput = new SimpleDateFormat(FHIR_INPUT_PATTERN);
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
