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
import static ca.uhn.fhir.model.api.TemporalPrecisionEnum.SECOND;

public interface XmlToFhirMapper {
    String DATE_TIME_PATTERN = "yyyyMMddHHmmss";
    String DATE_PATTERN = "yyyyMMdd";
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

        if (date.length() == DATE_PATTERN.length()) {
            LocalDate parsed = LocalDate.parse(date, DateTimeFormatter.ofPattern(DATE_PATTERN));
            baseDateTimeType.setPrecision(DAY);
            baseDateTimeType.setDay(parsed.getDayOfMonth());
            baseDateTimeType.setMonth(parsed.getMonthValue() - 1);
            baseDateTimeType.setYear(parsed.getYear());
            return (T) baseDateTimeType;
        } else if (date.length() == DATE_TIME_PATTERN.length()) {
            LocalDateTime parsed = LocalDateTime.parse(date, DateTimeFormatter.ofPattern(DATE_TIME_PATTERN));
            baseDateTimeType.setPrecision(SECOND);
            baseDateTimeType.setTimeZone(TimeZone.getTimeZone("Europe/London"));
            baseDateTimeType.setMinute(parsed.getMinute());
            baseDateTimeType.setSecond(parsed.getSecond());
            baseDateTimeType.setHour(parsed.getHour());
            baseDateTimeType.setDay(parsed.getDayOfMonth());
            baseDateTimeType.setMonth(parsed.getMonthValue() - 1);
            baseDateTimeType.setYear(parsed.getYear());
            return (T) baseDateTimeType;
        }

        throw new ScrBaseException("Unsupported date format: " + date);
    }
}
