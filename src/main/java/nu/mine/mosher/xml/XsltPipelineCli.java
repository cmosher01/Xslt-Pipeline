package nu.mine.mosher.xml;

import nu.mine.mosher.xml.dom.DomUtils;
import nu.mine.mosher.xml.transform.TransformUtils;
import org.w3c.dom.*;
import org.xml.sax.*;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import java.io.*;
import java.net.*;
import java.nio.file.Paths;
import java.util.*;

public class XsltPipelineCli
{
    public static void main(final String... args) throws IOException, TransformerException, SAXException, ParserConfigurationException {
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
                System.out.println(String.format("setting parameter: %s=%s", r2[0], params.get(r2[0])));
            } else {
                final String filetype = getFileType(arg);
                if (filetype.equalsIgnoreCase("xml")) {
                    dom = DomUtils.asDom(asUrl(arg), false);
                    dumpXmlForTrace(dom);
                } else if (filetype.equalsIgnoreCase("xsl") || filetype.equalsIgnoreCase("xslt")) {
                    dom = TransformUtils.transform(dom, asUrl(arg), params, initialTemplate);
                    dumpXmlForTrace(dom);
                    params = new HashMap<>();
                    initialTemplate = false;
                } else {
                    throw new IllegalArgumentException(arg);
                }
            }
        }

        System.err.flush();
        TransformUtils.serialize(dom, new BufferedOutputStream(new FileOutputStream(FileDescriptor.out)), true);

        System.err.flush();
        System.out.flush();
    }

    private static void dumpXmlForTrace(Node dom) throws IOException, TransformerException
    {
        hr();
        TransformUtils.serialize(dom, new BufferedOutputStream(new FileOutputStream(FileDescriptor.err)), true);
        hr();
    }

    private static void hr() {
        System.err.println("------------------------------------------------------------------------------------------------");
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
            System.err.println(String.format("URL: %s", url));
            return url;
        } catch (final Throwable e) {
            urlExcept = e;
        }

        Throwable pathExcept;
        try {
            final URL url = Paths.get(pathOrUrl).toUri().toURL();
            System.err.println(String.format("URL: %s", url));
            return url;
        } catch (final Throwable e) {
            pathExcept = e;
        }

        final IOException except = new IOException("Invalid path or URL: " + pathOrUrl);
        except.addSuppressed(pathExcept);
        except.addSuppressed(urlExcept);
        throw except;
    }

    private static String getFileType(String arg) {
        return arg.split("\\.(?=[^.]+$)")[1];
    }
}
