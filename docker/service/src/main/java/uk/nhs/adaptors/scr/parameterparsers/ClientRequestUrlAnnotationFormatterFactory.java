package uk.nhs.adaptors.scr.parameterparsers;

import org.springframework.context.support.EmbeddedValueResolutionSupport;
import org.springframework.format.AnnotationFormatterFactory;
import org.springframework.format.Formatter;
import org.springframework.format.Parser;
import org.springframework.format.Printer;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import uk.nhs.adaptors.scr.controllers.utils.UrlUtils;

import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class ClientRequestUrlAnnotationFormatterFactory extends EmbeddedValueResolutionSupport
    implements AnnotationFormatterFactory<ClientRequestUrl> {

    @Override
    public Set<Class<?>> getFieldTypes() {
        Set<Class<?>> fieldTypes = new HashSet<>();
        fieldTypes.add(String.class);
        return Collections.unmodifiableSet(fieldTypes);
    }

    @Override
    public Printer<String> getPrinter(ClientRequestUrl annotation, Class<?> fieldType) {
        return configureFormatterFrom();
    }

    @Override
    public Parser<String> getParser(ClientRequestUrl annotation, Class<?> fieldType) {
        return configureFormatterFrom();
    }

    private Formatter<String> configureFormatterFrom() {
        return new Formatter<>() {
            @Override
            public String print(String object, Locale locale) {
                throw new UnsupportedOperationException();
            }

            @Override
            public String parse(String clientRequestUrl, Locale locale) {
                var servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (servletRequestAttributes == null) {
                    throw new IllegalStateException();
                }
                return UrlUtils.extractBaseUrl(clientRequestUrl, servletRequestAttributes.getRequest().getRequestURI());
            }
        };
    }
}
