package uk.nhs.adaptors.scr.components;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.parser.StrictErrorHandler;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.springframework.stereotype.Component;
import uk.nhs.adaptors.scr.exceptions.FhirValidationException;
import uk.nhs.adaptors.scr.logging.LogExecutionTime;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Main class to parse JSON FHIR payloads.
 * Also used to perform string replacement on the final JSON output, where necessary.
 */
@Component
public class FhirParser {

    private final IParser jsonParser;

    public FhirParser() {
        FhirContext ctx = FhirContext.forR4();
        ctx.newJsonParser();
        ctx.setParserErrorHandler(new StrictErrorHandler());
        jsonParser = ctx.newJsonParser();
    }

    /**
     * Used to search FHIR JSON output for a given regex.
     * Specifically, year-month combination, which is invalid
     * in SCRs but occasionally present and needs to be preserved for clinical safety.
     * @param json
     * @param regEx
     * @return
     */
    private List<String> findDateRegularExpression(String json, String regEx) {

        List<String> occurences = new ArrayList<>();
        Pattern pattern = Pattern.compile(regEx);
        Matcher matcher = pattern.matcher(json);
        while (matcher.find()) {
            String str = matcher.group();
            occurences.add(str);
        }

        return occurences;
    }

    /**
     * Used to parse FHIR bundle to ensure valid JSON.
     * Also used to replace date found by regexp method above, with year, month, day, hour, minute, second.
     * e.g. Date may become 20231121090051 (2023/11/12 09:00:51).
     * Used to parse FHIR bundle.
     * @param body
     * @param klass
     * @return
     * @param <T>
     */
    @LogExecutionTime
    public <T extends IBaseResource> T parseResource(String body, Class<T> klass) {
        try {

            List<String> dates = findDateRegularExpression(body, "\"start\":\"\\d{4}\\d{2}");

            if (!dates.isEmpty()) {
                for (String date : dates) {
                    body = body.replace(date, (date.substring(0, 13) + "-" + date.substring(13, date.length())));
                }
            }

            return jsonParser.parseResource(klass, body);
        } catch (Exception ex) {
            throw new FhirValidationException(ex.getMessage());
        }
    }

    /**
     * Takes a resource (usually a bundle), and encodes it to JSON for FHIR output.
     * Afterwards, two transformations occur. One to replace <td/> with <td></td> to help NMEs parse HTML.
     * The next transformation is related to partial dates which may be present in the SCRs.
     * Dates may be received in partial format, e.g. YYYY, YYYY-mm, which are invalid but we still need to preserve
     * them as-is for clinical safety. Due to limitations on the third party FHIR parser code, we instead allocate a
     * microsecond value to each type of partial date. 001, for years only precision amount, 002 for months precision
     * amount (meaning, year and month), and day precision is 003 (year, month, day with no time).
     * This transformation finds the relevant microseconded datetime in JSON and replaces it with the date with original
     * accuracy, before finally returning the output/JSON string FHIR bundle.
     * @param resource
     * @return
     */
    public String encodeToJson(IBaseResource resource) {
        String output = jsonParser.setPrettyPrint(false).encodeResourceToString(resource);

        output = output.replace("<td/>", "<td></td>");

        List<String> days = findDateRegularExpression(output, "\\d{1,4}\\-\\d{1,2}\\-\\d{2,4}T00:00:00.003");
        List<String> months = findDateRegularExpression(output, "\\d{1,4}\\-\\d{1,2}\\-\\d{2,4}T00:00:00.002\\+\\d{2,2}:00");
        List<String> years = findDateRegularExpression(output, "\\d{1,4}\\-\\d{1,2}\\-\\d{2,4}T00:00:00.001");

        int dayStringCutoff = 10;
        for (String dayString : days) {
            output = output.replace(dayString, dayString.substring(0, dayStringCutoff));
        }

        int monthStringCutoff = 7;
        for (String monthString : months) {
            output = output.replace(monthString, monthString.substring(0, monthStringCutoff));
        }

        int yearStringCutoff = 4;
        for (String yearString : years) {
            output = output.replace(yearString, yearString.substring(0, yearStringCutoff));
        }

        return output;
    }

}
