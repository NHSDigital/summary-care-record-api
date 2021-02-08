package uk.nhs.adaptors.scr.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import uk.nhs.adaptors.scr.parameterparsers.ComponentIdentifierAnnotationFormatterFactory;
import uk.nhs.adaptors.scr.parameterparsers.PatientNhsNumberAnnotationFormatterFactory;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addFormatterForFieldAnnotation(new ComponentIdentifierAnnotationFormatterFactory());
        registry.addFormatterForFieldAnnotation(new PatientNhsNumberAnnotationFormatterFactory());
    }
}
