package nu.mine.mosher.xml.transform;



import org.w3c.dom.Node;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Map;



public class TransformUtils
{
    public static Node transform(final Node dom, final URL urlXslt, final Map<String, Object> params, boolean initialTemplate) throws TransformerException {
        final TransformerFactory factory = TransformerFactory.newInstance();
        if (initialTemplate) {
            factory.setAttribute("http://saxon.sf.net/feature/initialTemplate", "{http://www.w3.org/1999/XSL/Transform}initial-template");
        }

        final Transformer transform = factory.newTransformer(new StreamSource(urlXslt.toExternalForm()));
        configTransformer(transform, false);
        params.forEach(transform::setParameter);

        final DOMResult result = new DOMResult();
        transform.transform(new DOMSource(dom), result);
        return result.getNode();
    }

    public static void serialize(final Node dom, final BufferedOutputStream to, final boolean pretty) throws IOException, TransformerException {
        final DOMSource source = new DOMSource(dom);
        final StreamResult result = new StreamResult(to);
        final Transformer transformIdentity = TransformerFactory.newInstance().newTransformer();
        configTransformer(transformIdentity, pretty);
        transformIdentity.transform(source, result);
        to.flush();
    }



    private static void configTransformer(final Transformer transform, final boolean pretty) {
        transform.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transform.setOutputProperty(OutputKeys.METHOD, "xml");
        transform.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        if (pretty)
        {
            transform.setOutputProperty(OutputKeys.INDENT, "yes");
            transform.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        }
    }
}
