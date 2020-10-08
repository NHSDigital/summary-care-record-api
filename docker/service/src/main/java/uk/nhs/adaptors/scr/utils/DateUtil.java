package uk.nhs.adaptors.scr.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;

import lombok.extern.slf4j.Slf4j;
import uk.nhs.adaptors.scr.exceptions.FhirMappingException;

@Slf4j
public class DateUtil {

    private static final String INPUT_PATTERN = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    private static final String OUTPUT_PATTERN = "yyyyMMddHHmmss";

    public static String formatDate(String dateStr) throws FhirMappingException {
        String value = StringUtils.EMPTY;

        if (StringUtils.isNotBlank(dateStr)) {
            SimpleDateFormat sdfInput = new SimpleDateFormat(INPUT_PATTERN);
            SimpleDateFormat sdfOutput = new SimpleDateFormat(OUTPUT_PATTERN);

            try {
                Date date = sdfInput.parse(dateStr);
                value = sdfOutput.format(date);
            } catch (ParseException e) {
                throw new FhirMappingException("Date in incorrect format", e);
            }
        }

        return value;
    }

    public static Date parseDateHL7ToFhir(String inputDateString){
        SimpleDateFormat sdfOutput = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        SimpleDateFormat sdfInput = new SimpleDateFormat("yyyyMMddHHmmss");

        try {
            Date inputDate = sdfInput.parse(inputDateString);
            String outputDateString = sdfOutput.format(inputDate);
            return sdfOutput.parse(outputDateString);
        } catch (ParseException e) {
            //LOGGER.error(e.getMessage());
            //throwFhirMappingError("Date in incorrect format");
            return null;
        }
    }
}
