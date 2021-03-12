package uk.nhs.adaptors.scr.components;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.parser.StrictErrorHandler;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.springframework.stereotype.Component;
import uk.nhs.adaptors.scr.exceptions.FhirValidationException;
import uk.nhs.adaptors.scr.logging.LogExecutionTime;

@Component
public class FhirParser {

    private final IParser jsonParser;

    public FhirParser() {
        FhirContext ctx = FhirContext.forR4();
        ctx.newJsonParser();
        ctx.setParserErrorHandler(new StrictErrorHandler());
        jsonParser = ctx.newJsonParser();
    }

    @LogExecutionTime
    public <T extends IBaseResource> T parseResource(String body, Class<T> klass) {
        try {
            return jsonParser.parseResource(klass, body);
        } catch (Exception ex) {
            throw new FhirValidationException(ex.getMessage());
        }
    }

    public String encodeToJson(IBaseResource resource) {
        return jsonParser.setPrettyPrint(true).encodeResourceToString(resource);
    }
}
