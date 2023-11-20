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

@Component
public class FhirParser {

    private final IParser jsonParser;

    public FhirParser() {
        FhirContext ctx = FhirContext.forR4();
        ctx.newJsonParser();
        ctx.setParserErrorHandler(new StrictErrorHandler());
        jsonParser = ctx.newJsonParser();
    }

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

    private List<String> findDate(String json, String regEx) {

        List<String> occurences = new ArrayList<>();
        Pattern pattern = Pattern.compile(regEx);
        Matcher matcher = pattern.matcher(json);
        while (matcher.find()) {
            String str = matcher.group();
            occurences.add(str);
        }

        return occurences;
    }

    public String encodeToJson(IBaseResource resource) {
        String output = jsonParser.setPrettyPrint(true).encodeResourceToString(resource);

        output = output.replace("<td/>", "<td></td>");

        List<String> days = findDate(output, "\\d{1,4}\\-\\d{1,2}\\-\\d{2,4}T00:00:00.003");
        List<String> months = findDate(output, "\\d{1,4}\\-\\d{1,2}\\-\\d{2,4}T00:00:00.002");
        List<String> years = findDate(output, "\\d{1,4}\\-\\d{1,2}\\-\\d{2,4}T00:00:00.001");

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
