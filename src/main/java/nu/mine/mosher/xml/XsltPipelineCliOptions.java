package nu.mine.mosher.xml;



import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Paths;
import java.util.*;



@SuppressWarnings({"unused", "OptionalUsedAsFieldOrParameterType"})
public class XsltPipelineCliOptions {
    private final XsltPipeline pipeline = new XsltPipeline();



    public static void help(final Optional<String> unused) {
        System.err.println("Usage:");
        System.err.println();
        System.err.println("xslt-pipeline statement...");
        System.err.println();
        System.err.println("Statements:");
        System.err.println();
        System.err.println("--dom=input.xml");
        System.err.println("    parses an XML input URL/file into a DOM document (w/ or w/o validation using currently registered schema)");
        System.err.println("--dom");
        System.err.println("    generates an empty DOM document");
        System.err.println("--validation={true|false}");
        System.err.println("    sets validation mode on or off (doesn't actually validate)");
        System.err.println("    this option is for \"DOM validation\" not the Validator class");

        System.err.println("--xsd=ns_schema.xsd");
        System.err.println("    registers the given schema (can be used multiple times)");
        System.err.println("--xsd");
        System.err.println("    drops all currently registered schemas");
        System.err.println("--validate");
        System.err.println("    runs the Validator (not DOM validation) against the current DOM document, once for each registered schema");
        System.err.println("    this is not affected by the --validation={true|false} option");

        System.err.println("--xslt=ss.xslt");
        System.err.println("    runs current DOM document through the XSLT transformation");
        System.err.println("--xslt");
        System.err.println("    runs current DOM document through the identity XSLT transformation");
        System.err.println("--it={true|false}");
        System.err.println("    sets XSLT to start processing with xsl:initial-template, or not");
        System.err.println("--param=key:value");
        System.err.println("    sets parameter for XSLT processing");

        System.err.println("--pretty={true|false}");
        System.err.println("    indent final output XML; default: true");
        System.err.println("--trace={true|false}");
        System.err.println("    turn on or off tracing of statements on stderr; default: false");
    }

    public void trace(final Optional<String> b) {
        this.pipeline.trace(parseBoolean("trace", b));
    }

    public void pretty(final Optional<String> b) {
        this.pipeline.pretty(parseBoolean("pretty", b));
    }

    public void dom(final Optional<String> source) throws ParserConfigurationException, IOException, SAXException, TransformerException {
        if (source.isPresent()) {
            final URL url = asUrl(source.get());
            this.pipeline.dom(url);
        } else {
            this.pipeline.dom();
        }
    }

    public void validation(final Optional<String> b) {
        this.pipeline.validation(parseBoolean("validation", b));
    }

    public void it(final Optional<String> b) {
        this.pipeline.initialTemplate(parseBoolean("it", b));
    }

    public void xsd(final Optional<String> source) throws IOException, ParserConfigurationException, SAXException, TransformerException {
        if (source.isPresent()) {
            final URL url = asUrl(source.get());
            this.pipeline.xsd(url);
        } else {
            this.pipeline.xsd();
        }
    }

    public void xslt(final Optional<String> source) throws IOException, TransformerException, ParserConfigurationException, SAXException {
        if (source.isPresent()) {
            final URL url = asUrl(source.get());
            this.pipeline.xslt(url);
        } else {
            this.pipeline.xslt();
        }
    }

    public void validate(final Optional<String> notAllowed) throws IOException, SAXException, TransformerException {
        if (notAllowed.isPresent()) {
            throw new IllegalStateException("No value is allowed for option --validate");
        }
        this.pipeline.validate();
    }

    public void param(final Optional<String> keyColonValue) {
        if (!(keyColonValue.isPresent() && keyColonValue.get().contains(":"))) {
            throw new IllegalStateException("Invalid format for option --param=key:value");
        }
        final String[] r2 = Arrays.copyOf(keyColonValue.get().split(":", 2), 2);
        this.pipeline.param(r2[0], Objects.isNull(r2[1]) ? "" : r2[1]);
    }

    void serialize(final BufferedOutputStream out) throws IOException, TransformerException {
        this.pipeline.serialize(out);
    }



    static boolean parseBoolean(final String option, final Optional<String> b) {
        boolean r;
        if (b.isPresent() && b.get().equalsIgnoreCase("true")) {
            r = true;
        } else if (b.isPresent() && b.get().equalsIgnoreCase("false")) {
            r = false;
        } else {
            throw new IllegalArgumentException(String.format("Invalid value for option --%s={true|false}", option));
        }
        return r;
    }

    static URL asUrl(final String pathOrUrl) throws IOException
    {
        Throwable urlExcept;
        try {
            return new URI(pathOrUrl).toURL();
        } catch (final Throwable e) {
            urlExcept = e;
        }

        Throwable pathExcept;
        try {
            return Paths.get(pathOrUrl).toUri().toURL();
        } catch (final Throwable e) {
            pathExcept = e;
        }

        final IOException except = new IOException("Invalid path or URL: " + pathOrUrl);
        except.addSuppressed(pathExcept);
        except.addSuppressed(urlExcept);
        throw except;
    }
}
