package uk.nhs.adaptors.scr.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {

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
