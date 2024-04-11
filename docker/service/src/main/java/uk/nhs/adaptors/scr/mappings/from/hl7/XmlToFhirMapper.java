package uk.nhs.adaptors.scr.mappings.from.hl7;

import lombok.SneakyThrows;
import org.hl7.fhir.r4.model.BaseDateTimeType;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.InstantType;
import org.hl7.fhir.r4.model.Resource;
import org.w3c.dom.Node;
import uk.nhs.adaptors.scr.exceptions.ScrBaseException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.TimeZone;

import static ca.uhn.fhir.model.api.TemporalPrecisionEnum.DAY;
import static ca.uhn.fhir.model.api.TemporalPrecisionEnum.MINUTE;
import static ca.uhn.fhir.model.api.TemporalPrecisionEnum.SECOND;
import static ca.uhn.fhir.model.api.TemporalPrecisionEnum.YEAR;
import static ca.uhn.fhir.model.api.TemporalPrecisionEnum.MILLI;
import static java.lang.Integer.parseInt;

public interface XmlToFhirMapper {
    // List of known date/time formats which may be present in HL7 based SCR.
    String DATE_TIME_SECONDS_PATTERN = "yyyyMMddHHmmss";
    String DATE_TIME_MINUTES_PATTERN = "yyyyMMddHHmm";
    String DATE_PATTERN = "yyyyMMdd";
    String YEAR_MONTH_PATTERN = "yyyyMM";
    String YEAR_MONTH_PATTERN_DASH = "yyyy-MM";
    String YEAR_PATTERN = "yyyy";
    String SNOMED_SYSTEM = "http://snomed.info/sct";

    List<? extends Resource> map(Node document);

    /**
     * Takes a date, and parses it into a BaseDateTimeObject.
     * Dates may be received in partial format, e.g. YYYY, YYYY-mm, which are invalid, but we still need to preserve
     * them as-is for clinical safety. Due to limitations on the third party FHIR parser code, we instead allocate a
     * microsecond value to each type of partial date. 001, for years only precision amount, 002 for months precision
     * amount (meaning, year and month), and day precision is 003 (year, month, day with no time).
     * @param date
     * @return
     */

    @SneakyThrows
    static <T extends BaseDateTimeType> T parseDate(String date, Class<T> clazz) {
        BaseDateTimeType baseDateTimeType;
        if (clazz.equals(DateTimeType.class)) {
            baseDateTimeType = new DateTimeType();
        } else if (clazz.equals(InstantType.class)) {
            baseDateTimeType = new InstantType();
        } else {
            throw new ScrBaseException("Invalid target class: " + clazz.getName());
        }

        int dayPrecision = 3;
        int monthPrecision = 2;
        int yearPrecision = 1;


        if (isValidDate(date, DATE_TIME_SECONDS_PATTERN)) {
            LocalDateTime parsed = LocalDateTime.parse(date, DateTimeFormatter.ofPattern(DATE_TIME_SECONDS_PATTERN));
            baseDateTimeType.setPrecision(SECOND);
            baseDateTimeType.setTimeZone(TimeZone.getTimeZone("Europe/London"));
            baseDateTimeType.setSecond(parsed.getSecond());
            setHoursMinutesPart(baseDateTimeType, parsed);
            setDatePart(baseDateTimeType, parsed.getDayOfMonth(), parsed.getMonthValue(), parsed.getYear());
        } else if (isValidDate(date, DATE_TIME_MINUTES_PATTERN)) {
            LocalDateTime parsed = LocalDateTime.parse(date, DateTimeFormatter.ofPattern(DATE_TIME_MINUTES_PATTERN));
            baseDateTimeType.setPrecision(MINUTE);
            baseDateTimeType.setTimeZone(TimeZone.getTimeZone("Europe/London"));
            setHoursMinutesPart(baseDateTimeType, parsed);
            setDatePart(baseDateTimeType, parsed.getDayOfMonth(), parsed.getMonthValue(), parsed.getYear());
        } else if (isValidDate(date, DATE_PATTERN)) {
            LocalDate parsed = LocalDate.parse(date, DateTimeFormatter.ofPattern(DATE_PATTERN));
            baseDateTimeType.setPrecision(DAY);
            baseDateTimeType.setTimeZone(TimeZone.getTimeZone("Europe/London"));
            setDatePart(baseDateTimeType, parsed.getDayOfMonth(), parsed.getMonthValue(), parsed.getYear());
            baseDateTimeType.setMillis(dayPrecision);
        } else if (isValidDate(date, YEAR_MONTH_PATTERN_DASH)) {
            // We use precision DAY here, even though we want precision MONTH.
            // This is because using MONTH triggers a bug in the third party handling which results in a timezone
            // of BST being used.
            String[] parts = date.split("-");
            String year = parts[0];
            String month = parts[1];
            baseDateTimeType.setPrecision(MILLI);
            baseDateTimeType.setTimeZone(TimeZone.getTimeZone("Europe/London"));
            baseDateTimeType.setMillis(monthPrecision);
            baseDateTimeType.setYear(parseInt(year));
            baseDateTimeType.setMonth(parseInt(month) - 1);
        } else if (isValidDate(date, YEAR_MONTH_PATTERN)) {
            String year = date.substring(0, YEAR_PATTERN.length());
            String month = date.substring(YEAR_PATTERN.length());
            baseDateTimeType.setPrecision(MILLI);
            baseDateTimeType.setTimeZone(TimeZone.getTimeZone("Europe/London"));
            baseDateTimeType.setMillis(monthPrecision);
            baseDateTimeType.setYear(parseInt(year));
            baseDateTimeType.setMonth(parseInt(month) - 1);
        } else if (isValidDate(date, YEAR_PATTERN)) {
            baseDateTimeType.setPrecision(YEAR);
            baseDateTimeType.setTimeZone(TimeZone.getTimeZone("Europe/London"));
            baseDateTimeType.setYear(parseInt(date) + 1);
            baseDateTimeType.setMillis(yearPrecision);
        } else {
            throw new ScrBaseException("Unsupported date format: " + date);
        }

        return (T) baseDateTimeType;
    }

    /**
     * Checks that a date string is the length of one of the dates defined above, and that the date can be
     * correctly parsed.
     * @param date
     * @param format
     * @return
     */
    static boolean isValidDate(String date, String format) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        boolean isDate = date.length() == format.length();
        try {
            dateFormat.parse(date);
        } catch (ParseException e) {
            isDate = false;
        }
        return isDate;
    }

    private static void setHoursMinutesPart(BaseDateTimeType baseDateTimeType, LocalDateTime parsed) {
        baseDateTimeType.setMinute(parsed.getMinute());
        baseDateTimeType.setHour(parsed.getHour());
    }

    static void setDatePart(BaseDateTimeType baseDateTimeType, int dayOfMonth, int monthValue, int year) {
        baseDateTimeType.setDay(dayOfMonth);
        baseDateTimeType.setMonth(monthValue - 1);
        baseDateTimeType.setYear(year);
    }
}
