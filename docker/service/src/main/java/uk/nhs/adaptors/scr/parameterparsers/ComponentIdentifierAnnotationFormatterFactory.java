package uk.nhs.adaptors.scr.parameterparsers;

import org.springframework.context.support.EmbeddedValueResolutionSupport;
import org.springframework.format.AnnotationFormatterFactory;
import org.springframework.format.Formatter;
import org.springframework.format.Parser;
import org.springframework.format.Printer;
import uk.nhs.adaptors.scr.exceptions.BadRequestException;

import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ComponentIdentifierAnnotationFormatterFactory extends EmbeddedValueResolutionSupport
    implements AnnotationFormatterFactory<ComponentIdentifier> {

    private static final String IDENTIFIER_PATTERN = "\\[uuid\\]\\$composition\\.subject:Patient\\.identifier=https://fhir\\.nhs\\.uk\\/Id\\/nhs-number\\|";
    private static final String PATTERN = "^(" + IDENTIFIER_PATTERN + "\\d{10})$";

    @Override
    public Set<Class<?>> getFieldTypes() {
        Set<Class<?>> fieldTypes = new HashSet<>();
        fieldTypes.add(String.class);
        return Collections.unmodifiableSet(fieldTypes);
    }

    @Override
    public Printer<String> getPrinter(ComponentIdentifier annotation, Class<?> fieldType) {
        return configureFormatterFrom();
    }

    @Override
    public Parser<String> getParser(ComponentIdentifier annotation, Class<?> fieldType) {
        return configureFormatterFrom();
    }

    private Formatter<String> configureFormatterFrom() {
        return new Formatter<>() {
            @Override
            public String print(String object, Locale locale) {
                throw new UnsupportedOperationException();
            }

            @Override
            public String parse(String componentIdentifier, Locale locale) {
                Matcher m = Pattern.compile(PATTERN).matcher(componentIdentifier);
                if (m.find() && m.groupCount() == 1) {
                    return componentIdentifier.split(IDENTIFIER_PATTERN)[1];
                }
                throw new BadRequestException("Invalid value - %s in field 'component.identifier'");

            }
        };
    }
}
