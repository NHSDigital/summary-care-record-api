package uk.nhs.adaptors.scr.mappings.from.hl7;

import lombok.SneakyThrows;
import org.hl7.fhir.r4.model.BaseDateTimeType;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.InstantType;
import org.hl7.fhir.r4.model.Resource;
import org.w3c.dom.Node;
import uk.nhs.adaptors.scr.exceptions.ScrBaseException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.TimeZone;

import static ca.uhn.fhir.model.api.TemporalPrecisionEnum.DAY;
import static ca.uhn.fhir.model.api.TemporalPrecisionEnum.MINUTE;
import static ca.uhn.fhir.model.api.TemporalPrecisionEnum.MONTH;
import static ca.uhn.fhir.model.api.TemporalPrecisionEnum.SECOND;
import static ca.uhn.fhir.model.api.TemporalPrecisionEnum.YEAR;
import static java.lang.Integer.parseInt;

public interface XmlToFhirMapper {
    String DATE_TIME_SECONDS_PATTERN = "yyyyMMddHHmmss";
    String DATE_TIME_MINUTES_PATTERN = "yyyyMMddHHmm";
    String DATE_PATTERN = "yyyyMMdd";
    String YEAR_MONTH_PATTERN = "yyyyMM";
    String YEAR_PATTERN = "yyyy";
    String SNOMED_SYSTEM = "http://snomed.info/sct";

    List<? extends Resource> map(Node document);

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

        if (date.length() == YEAR_PATTERN.length()) {
            baseDateTimeType.setPrecision(YEAR);
            baseDateTimeType.setYear(parseInt(date));
        } else if (date.length() == YEAR_MONTH_PATTERN.length()) {
            String year = date.substring(0, YEAR_PATTERN.length());
            String month = date.substring(YEAR_PATTERN.length());
            baseDateTimeType.setPrecision(MONTH);
            baseDateTimeType.setMonth(parseInt(month));
            baseDateTimeType.setYear(parseInt(year));
        } else if (date.length() == DATE_PATTERN.length()) {
            LocalDate parsed = LocalDate.parse(date, DateTimeFormatter.ofPattern(DATE_PATTERN));
            baseDateTimeType.setPrecision(DAY);
            setDatePart(baseDateTimeType, parsed.getDayOfMonth(), parsed.getMonthValue(), parsed.getYear());
        } else if (date.length() == DATE_TIME_MINUTES_PATTERN.length()) {
            LocalDateTime parsed = LocalDateTime.parse(date, DateTimeFormatter.ofPattern(DATE_TIME_MINUTES_PATTERN));
            baseDateTimeType.setPrecision(MINUTE);
            baseDateTimeType.setTimeZone(TimeZone.getTimeZone("Europe/London"));
            setHoursMinutesPart(baseDateTimeType, parsed);
            setDatePart(baseDateTimeType, parsed.getDayOfMonth(), parsed.getMonthValue(), parsed.getYear());
        } else if (date.length() == DATE_TIME_SECONDS_PATTERN.length()) {
            LocalDateTime parsed = LocalDateTime.parse(date, DateTimeFormatter.ofPattern(DATE_TIME_SECONDS_PATTERN));
            baseDateTimeType.setPrecision(SECOND);
            baseDateTimeType.setTimeZone(TimeZone.getTimeZone("Europe/London"));
            baseDateTimeType.setSecond(parsed.getSecond());
            setHoursMinutesPart(baseDateTimeType, parsed);
            setDatePart(baseDateTimeType, parsed.getDayOfMonth(), parsed.getMonthValue(), parsed.getYear());
        } else {
            throw new ScrBaseException("Unsupported date format: " + date);
        }

        return (T) baseDateTimeType;
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
