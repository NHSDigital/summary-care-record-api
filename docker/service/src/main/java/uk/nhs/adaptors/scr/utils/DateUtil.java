package uk.nhs.adaptors.scr.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;

import lombok.extern.slf4j.Slf4j;
import uk.nhs.adaptors.scr.exceptions.FhirMappingException;

@Slf4j
public class DateUtil {

    public static final String INPUT_PATTERN = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    private static final String OUTPUT_PATTERN = "yyyyMMddHHmmss";
    private static final String FHIR_INPUT_PATTERN = "yyyy-MM-dd'T'HH:mm:ssXXX";

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
}
