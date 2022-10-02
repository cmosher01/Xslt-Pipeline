package nu.mine.mosher.xml.validation;


import nu.mine.mosher.xml.sax.ErrorHandlerImpl;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.transform.dom.*;
import javax.xml.validation.*;
import java.io.IOException;
import java.net.URL;


public class ValidationUtils {
    public static Node validate(final Node dom, final URL urlXsd) throws SAXException, IOException {
        final var augmented = new DOMResult();
        augmented.setSystemId(dom.getBaseURI());

        final var validator = schema(urlXsd).newValidator();
        validator.setErrorHandler(new ErrorHandlerImpl());
        validator.validate(new DOMSource(dom, dom.getBaseURI()), augmented);
        return augmented.getNode();
    }



    private static Schema schema(final URL urlXsd) throws SAXException {
        return factory().newSchema(urlXsd);
    }

    private static SchemaFactory factory() {
        return SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
    }
}
