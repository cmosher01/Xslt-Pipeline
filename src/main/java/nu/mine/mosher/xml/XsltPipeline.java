package nu.mine.mosher.xml;

import org.slf4j.*;
import org.slf4j.bridge.SLF4JBridgeHandler;
import org.w3c.dom.*;
import org.xml.sax.*;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import java.io.*;
import java.net.*;
import java.nio.file.Paths;
import java.util.*;

public class XsltPipeline {
    static {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
        java.util.logging.Logger.getLogger("").setLevel(java.util.logging.Level.FINEST);
    }

    private static final Logger LOG = LoggerFactory.getLogger(XsltPipeline.class);

    public static void main(final String... args) throws IOException, TransformerException, SAXException, ParserConfigurationException {
        if (LOG.isDebugEnabled()) {
            System.setProperty("jaxp.debug", "1");
        }

        Map<String, Object> params = new HashMap<>();
        Node dom = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        boolean initialTemplate = false;
        for (String arg : args) {
            if (arg == null) {
                arg = "";
            }
            if (arg.startsWith("--")) {
                if (arg.equalsIgnoreCase("--it")) {
                    initialTemplate = true;
                } else if (arg.equalsIgnoreCase("--help")) {
                    help();
                } else {
                    throw new IllegalArgumentException("Unrecognized option: "+arg);
                }
            } else if (arg.contains("=")) {
                final String[] r2 = Arrays.copyOf(arg.split("=", 2), 2);
                params.put(r2[0], Objects.isNull(r2[1]) ? "" : r2[1]);
                LOG.trace("setting parameter: {}={}", r2[0], params.get(r2[0]));
            } else {
                final String filetype = getFileType(arg);
                if (filetype.equalsIgnoreCase("xml")) {
                    dom = asDom(asUrl(arg), false);
                    if (LOG.isTraceEnabled()) {
                        hr();
                        serialize(dom, new BufferedOutputStream(new FileOutputStream(FileDescriptor.err)));
                        hr();
                    }
                } else if (filetype.equalsIgnoreCase("xsl") || filetype.equalsIgnoreCase("xslt")) {
                    dom = transform(dom, asUrl(arg), params, initialTemplate);
                    if (LOG.isTraceEnabled()) {
                        hr();
                        serialize(dom, new BufferedOutputStream(new FileOutputStream(FileDescriptor.err)));
                        hr();
                    }
                    params = new HashMap<>();
                    initialTemplate = false;
                } else {
                    throw new IllegalArgumentException(arg);
                }
            }
        }

        if (!LOG.isTraceEnabled()) {
            System.err.flush();
            System.out.flush();
            serialize(dom, new BufferedOutputStream(new FileOutputStream(FileDescriptor.out)));
        }

        System.err.flush();
        System.out.flush();
    }

    private static String getFileType(String arg) {
        return arg.split("\\.(?=[^.]+$)")[1];
    }

    private static void hr() {
        System.err.flush();
        System.err.println("------------------------------------------------------------------------------------------------");
        System.err.flush();
    }

    private static void help() {
        System.err.println("Usage:");
        System.err.println();
        System.err.println("    xslt-pipeline input.xml param=value... transform.xslt...");
    }

    private static URL asUrl(final String pathOrUrl) throws IOException {
        Throwable urlExcept;
        try {
            final URL url = new URI(pathOrUrl).toURL();
            LOG.trace("URL: {}", url);
            System.err.flush();
            return url;
        } catch (final Throwable e) {
            urlExcept = e;
        }

        Throwable pathExcept;
        try {
            final URL url = Paths.get(pathOrUrl).toUri().toURL();
            LOG.trace("URL: {}", url);
            System.err.flush();
            return url;
        } catch (final Throwable e) {
            pathExcept = e;
        }

        final IOException except = new IOException("Invalid path or URL: " + pathOrUrl);
        except.addSuppressed(pathExcept);
        except.addSuppressed(urlExcept);
        throw except;
    }


    public static Document asDom(final URL xml, final boolean validate) throws ParserConfigurationException, IOException, SAXException {
        final DocumentBuilder builder = documentBuilderFactory(validate).newDocumentBuilder();
        builder.setErrorHandler(new MyErrorHandler());

        return builder.parse(xml.toExternalForm());
    }

    private static DocumentBuilderFactory documentBuilderFactory(final boolean validate) throws ParserConfigurationException {
        final DocumentBuilderFactory factoryDocBuild = DocumentBuilderFactory.newInstance();

        factoryDocBuild.setValidating(validate);
        factoryDocBuild.setFeature("http://apache.org/xml/features/validation/schema", validate);

        factoryDocBuild.setNamespaceAware(true);
        factoryDocBuild.setExpandEntityReferences(true);
        factoryDocBuild.setCoalescing(true);
        factoryDocBuild.setIgnoringElementContentWhitespace(false);
        factoryDocBuild.setIgnoringComments(false);

        factoryDocBuild.setFeature("http://apache.org/xml/features/honour-all-schemaLocations", true);
        factoryDocBuild.setFeature("http://apache.org/xml/features/warn-on-duplicate-entitydef", true);
        factoryDocBuild.setFeature("http://apache.org/xml/features/standard-uri-conformant", true);
        factoryDocBuild.setFeature("http://apache.org/xml/features/xinclude", true);
        factoryDocBuild.setFeature("http://apache.org/xml/features/validate-annotations", true);
        factoryDocBuild.setFeature("http://apache.org/xml/features/validation/schema-full-checking", true);
        factoryDocBuild.setFeature("http://apache.org/xml/features/validation/warn-on-duplicate-attdef", true);
        factoryDocBuild.setFeature("http://apache.org/xml/features/validation/warn-on-undeclared-elemdef", true);
        factoryDocBuild.setFeature("http://apache.org/xml/features/scanner/notify-char-refs", true);
        factoryDocBuild.setFeature("http://apache.org/xml/features/scanner/notify-builtin-refs", true);
        factoryDocBuild.setFeature("http://apache.org/xml/features/dom/defer-node-expansion", false);

        factoryDocBuild.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaLanguage", "http://www.w3.org/2001/XMLSchema");

        return factoryDocBuild;
    }

    private static class MyErrorHandler implements ErrorHandler {
        public void warning(SAXParseException spe) throws SAXException {
            throw new SAXException("WARN " + getParseExceptionInfo(spe), spe);
        }

        public void error(SAXParseException spe) throws SAXException {
            throw new SAXException("ERROR " + getParseExceptionInfo(spe), spe);
        }

        public void fatalError(SAXParseException spe) throws SAXException {
            throw new SAXException("FATAL " + getParseExceptionInfo(spe), spe);
        }

        private static String getParseExceptionInfo(SAXParseException spe) {
            return String.format("uri: %s, line: %d, msg: %s", spe.getSystemId(), spe.getLineNumber(), spe.getMessage());
        }
    }

    private static Node transform(final Node dom, final URL urlXslt, final Map<String, Object> params, boolean initialTemplate) throws TransformerException {
        final TransformerFactory factory = TransformerFactory.newInstance();
        if (initialTemplate) {
            factory.setAttribute("http://saxon.sf.net/feature/initialTemplate", "{http://www.w3.org/1999/XSL/Transform}initial-template");
        }

        final Transformer transformer = factory.newTransformer(new StreamSource(urlXslt.toExternalForm()));
        params.forEach(transformer::setParameter);

        final DOMResult result = new DOMResult();
        transformer.transform(new DOMSource(dom), result);
        return result.getNode();
    }

    private static void configTransformer(Transformer transformer) {
        if (LOG.isTraceEnabled()) {
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        }
    }

    private static void serialize(final Node dom, final BufferedOutputStream to) throws IOException, TransformerException {
        final DOMSource source = new DOMSource(dom);
        final StreamResult result = new StreamResult(to);
        final Transformer transformer = TransformerFactory.newInstance().newTransformer();
        configTransformer(transformer);
        transformer.transform(source, result);
        to.flush();
    }
}
