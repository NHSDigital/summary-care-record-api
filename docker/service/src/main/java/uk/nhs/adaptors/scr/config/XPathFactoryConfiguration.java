package uk.nhs.adaptors.scr.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.annotation.RequestScope;

import javax.xml.xpath.XPathFactory;

@Configuration
public class XPathFactoryConfiguration {

    @Bean
    @RequestScope
    public XPathFactory xpathFactory() {
        return XPathFactory.newInstance();
    }
}
