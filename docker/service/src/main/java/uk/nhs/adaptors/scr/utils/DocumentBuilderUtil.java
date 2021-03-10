package uk.nhs.adaptors.scr.utils;

import lombok.SneakyThrows;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;

import static javax.xml.XMLConstants.ACCESS_EXTERNAL_DTD;
import static javax.xml.XMLConstants.ACCESS_EXTERNAL_SCHEMA;
import static org.apache.commons.lang3.StringUtils.EMPTY;

public class DocumentBuilderUtil {

    @SneakyThrows
    public static DocumentBuilder documentBuilder() {
        DocumentBuilderFactory df = DocumentBuilderFactory.newInstance();
        df.setAttribute(ACCESS_EXTERNAL_DTD, EMPTY);
        df.setAttribute(ACCESS_EXTERNAL_SCHEMA, EMPTY);
        return df.newDocumentBuilder();
    }

    @SneakyThrows
    public static Document parseDocument(String xml) {
        return documentBuilder().parse(new InputSource(new StringReader(xml)));
    }
}
