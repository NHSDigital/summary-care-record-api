package uk.nhs.adaptors.scr.components;

import static uk.nhs.adaptors.scr.controllers.FhirMediaTypes.APPLICATION_FHIR_JSON;
import static uk.nhs.adaptors.scr.controllers.FhirMediaTypes.APPLICATION_FHIR_XML;

import java.util.List;
import java.util.function.Function;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.HttpMediaTypeNotAcceptableException;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.DataFormatException;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.parser.StrictErrorHandler;
import uk.nhs.adaptors.scr.exceptions.FhirValidationException;

@Component
public class FhirParser {

    private final IParser jsonParser;
    private final IParser xmlParser;

    public FhirParser() {
        FhirContext ctx = FhirContext.forR4();
        ctx.newJsonParser();
        ctx.setParserErrorHandler(new StrictErrorHandler());
        jsonParser = ctx.newJsonParser();
        xmlParser = ctx.newXmlParser();
    }

    private static IBaseResource parse(String body, IParser parser) {
        try {
            return parser.parseResource(body);
        } catch (DataFormatException e) {
            throw new FhirValidationException("Unable to parse " + parser.getEncoding().name() + " resource: " + e.getMessage());
        }
    }

    private static String encode(IBaseResource resource, IParser parser) {
        return parser.setPrettyPrint(true).encodeResourceToString(resource);
    }

    public Bundle parseResource(MediaType contentType, String body) throws HttpMediaTypeNotAcceptableException {
        Function<String, IBaseResource> parser;
        if (contentType.equalsTypeAndSubtype(APPLICATION_FHIR_JSON)) {
            parser = this::parseJson;
        } else if (contentType.equalsTypeAndSubtype(APPLICATION_FHIR_XML)) {
            parser = this::parseXml;
        } else {
            throw new HttpMediaTypeNotAcceptableException(List.of(APPLICATION_FHIR_JSON, APPLICATION_FHIR_XML));
        }
        return (Bundle) parser.apply(body);
    }

    public String encodeResource(MediaType contentType, IBaseResource resource) throws HttpMediaTypeNotAcceptableException {
        Function<IBaseResource, String> encoder;
        if (contentType.equalsTypeAndSubtype(APPLICATION_FHIR_JSON)) {
            encoder = this::encodeToJson;
        } else if (contentType.equalsTypeAndSubtype(APPLICATION_FHIR_XML)) {
            encoder = this::encodeToXml;
        } else {
            throw new HttpMediaTypeNotAcceptableException(List.of(APPLICATION_FHIR_JSON, APPLICATION_FHIR_XML));
        }
        return encoder.apply(resource);
    }

    private IBaseResource parseJson(String body) {
        return parse(body, jsonParser);
    }

    private IBaseResource parseXml(String body) {
        return parse(body, xmlParser);
    }

    private String encodeToJson(IBaseResource resource) {
        return encode(resource, jsonParser);
    }

    private String encodeToXml(IBaseResource resource) {
        return encode(resource, xmlParser);
    }
}
