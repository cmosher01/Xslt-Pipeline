package nu.mine.mosher.xml.transform;


import org.w3c.dom.Node;

import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import java.io.*;
import java.net.URL;
import java.util.Map;


public class TransformUtils {
    public static Node identity(final Node dom) throws TransformerException {
        final TransformerFactory factory = TransformerFactory.newInstance();
        final Transformer transform = factory.newTransformer();
        configTransformer(transform);

        final DOMResult result = new DOMResult();
        transform.transform(new DOMSource(dom), result);
        return result.getNode();
    }

    public static void serialize(final Node dom, final BufferedOutputStream to, final boolean pretty, final boolean xmldecl) throws IOException, TransformerException {
        final DOMSource source = new DOMSource(dom);
        final StreamResult result = new StreamResult(to);
        final Transformer transformIdentity = TransformerFactory.newInstance().newTransformer();
        configTransformer(transformIdentity, pretty, xmldecl);
        transformIdentity.transform(source, result);
        to.flush();
    }

    public static Node transform(final Node dom, final URL urlXslt, final Map<String, Object> params, boolean initialTemplate) throws TransformerException {
        final TransformerFactory factory = TransformerFactory.newInstance();

        if (initialTemplate) {
            factory.setAttribute("http://saxon.sf.net/feature/initialTemplate", "{http://www.w3.org/1999/XSL/Transform}initial-template");
        }

        final Transformer transform = factory.newTransformer(new StreamSource(urlXslt.toExternalForm()));
        configTransformer(transform);
        params.forEach(transform::setParameter);

        final DOMResult result = new DOMResult();
        transform.transform(new DOMSource(dom), result);
        return result.getNode();
    }



    private static void configTransformer(final Transformer transform) {
        configTransformer(transform, false, true);
    }

    private static void configTransformer(final Transformer transform, final boolean pretty, final boolean xmldecl) {
        transform.setOutputProperty(OutputKeys.METHOD, "xml");
        transform.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

        transform.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, xmldecl ? "no" : "yes");

        if (pretty) {
            transform.setOutputProperty(OutputKeys.INDENT, "yes");
            transform.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        }
    }
}
