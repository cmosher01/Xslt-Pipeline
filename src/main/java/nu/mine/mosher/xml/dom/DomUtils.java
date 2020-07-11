package nu.mine.mosher.xml.dom;


import nu.mine.mosher.xml.sax.ErrorHandlerImpl;
import org.w3c.dom.Document;
import org.xml.sax.*;

import javax.xml.XMLConstants;
import javax.xml.parsers.*;
import java.io.*;
import java.net.URL;
import java.util.*;


public class DomUtils {
    public static Document asDom(final BufferedInputStream xml, final boolean validate, final List<URL> schemas) throws ParserConfigurationException, IOException, SAXException {
        return asDom(new InputSource(xml), validate, schemas);
    }

    public static Document asDom(final URL xml, final boolean validate, final List<URL> schemas) throws ParserConfigurationException, IOException, SAXException {
        return asDom(new InputSource(xml.toExternalForm()), validate, schemas);
    }

    private static Document asDom(final InputSource xml, final boolean validate, final List<URL> schemas) throws ParserConfigurationException, IOException, SAXException {
        final DocumentBuilder builder = factory(validate, schemas).newDocumentBuilder();
        builder.setErrorHandler(new ErrorHandlerImpl());

        return builder.parse(xml);
    }

    public static Document empty() throws ParserConfigurationException {
        return factory(false, Collections.emptyList()).newDocumentBuilder().newDocument();
    }



    private static DocumentBuilderFactory factory(final boolean validate, final List<URL> schemas) throws ParserConfigurationException {
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        factory.setValidating(validate);
        factory.setFeature("http://apache.org/xml/features/validation/schema", validate);

        factory.setNamespaceAware(true);
        factory.setExpandEntityReferences(true);
        factory.setCoalescing(true);
        factory.setIgnoringElementContentWhitespace(false);
        factory.setIgnoringComments(false);

        factory.setFeature("http://apache.org/xml/features/honour-all-schemaLocations", true);
        factory.setFeature("http://apache.org/xml/features/warn-on-duplicate-entitydef", true);
        factory.setFeature("http://apache.org/xml/features/standard-uri-conformant", true);
        factory.setFeature("http://apache.org/xml/features/xinclude", true);
        factory.setFeature("http://apache.org/xml/features/validate-annotations", true);
        factory.setFeature("http://apache.org/xml/features/validation/schema-full-checking", true);
        factory.setFeature("http://apache.org/xml/features/validation/warn-on-duplicate-attdef", true);
        factory.setFeature("http://apache.org/xml/features/validation/warn-on-undeclared-elemdef", true);
        factory.setFeature("http://apache.org/xml/features/dom/defer-node-expansion", false);

        //These options often crash Xerces (as of 2.12.0):
        //factory.setFeature("http://apache.org/xml/features/scanner/notify-char-refs", true);
        //factory.setFeature("http://apache.org/xml/features/scanner/notify-builtin-refs", true);

        factory.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaLanguage", XMLConstants.W3C_XML_SCHEMA_NS_URI);

        if (!schemas.isEmpty()) {
            factory.setAttribute(
                "http://java.sun.com/xml/jaxp/properties/schemaSource",
                schemas.stream().sequential().map(URL::toExternalForm).toArray(String[]::new));
        }

        return factory;
    }
}
