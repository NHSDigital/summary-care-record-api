package uk.nhs.adaptors.scr.utils;

import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.util.Date;

import static org.apache.commons.lang3.StringUtils.EMPTY;

@Slf4j
public class DateUtil {
    private static final String DATE_PATTERN = "yyyyMMddHHmmss";

    public static String formatDate(Date date) {
        if (date != null) {
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);
            return sdf.format(date);
        } else {
            return EMPTY;
        }
    }
}
