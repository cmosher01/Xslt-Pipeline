package nu.mine.mosher.xml;



import nu.mine.mosher.xml.dom.DomUtils;
import nu.mine.mosher.xml.transform.TransformUtils;
import nu.mine.mosher.xml.validation.ValidationUtils;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.BufferedOutputStream;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;



@SuppressWarnings({"unused", "OptionalUsedAsFieldOrParameterType"})
public class XsltPipelineCliOptions {
    public Node dom = null;
    public boolean validation = false;
    public List<URL> schema = new ArrayList<>();
    public boolean initialTemplate = false;
    public Map<String, Object> params = new HashMap<>();
    public boolean pretty = true;
    public boolean trace = false;



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
        this.trace = parseBoolean("trace", b);
        traceHR();
        traceBool("trace", this.trace);
    }

    public void pretty(final Optional<String> b) {
        this.pretty = parseBoolean("pretty", b);
        traceHR();
        traceBool("pretty", this.pretty);
    }

    public void dom(final Optional<String> source) throws ParserConfigurationException, IOException, SAXException, TransformerException {
        traceHR();
        if (source.isPresent()) {
            final URL url = asUrl(source.get());
            this.dom = DomUtils.asDom(url, this.validation, this.schema);
            traceUrl("load dom", url);
            traceHR();
        } else {
            this.dom = DomUtils.empty();
            trace("generate empty dom");
            traceHR();
        }
        traceXml(this.dom);
    }

    public void validation(final Optional<String> b) {
        this.validation = parseBoolean("validation", b);
        traceHR();
        traceBool("validation", this.validation);
    }

    public void it(final Optional<String> b) {
        this.initialTemplate = parseBoolean("it", b);
        traceHR();
        traceBool("initial-template", this.initialTemplate);
    }

    public void xsd(final Optional<String> source) throws IOException, ParserConfigurationException, SAXException, TransformerException {
        traceHR();
        if (source.isPresent()) {
            final URL url = asUrl(source.get());
            this.schema.add(url);
            traceUrl("register xsd", url);
            traceHR();
            traceXml(DomUtils.asDom(url, false, Collections.emptyList()));
        } else {
            this.schema = new ArrayList<>();
            trace("dropping all registered schema");
        }
    }

    public void xslt(final Optional<String> source) throws IOException, TransformerException, ParserConfigurationException, SAXException {
        traceHR();
        if (source.isPresent()) {
            final URL url = asUrl(source.get());
            traceUrl("transform with xslt", url);
            traceHR();
            traceXml(DomUtils.asDom(url, false, Collections.emptyList()));
            this.dom = TransformUtils.transform(this.dom, url, this.params, this.initialTemplate);
            this.params = new HashMap<>();
        } else {
            trace("identity transform");
            this.dom = TransformUtils.identity(this.dom);
        }
        traceHR();
        traceXml(this.dom);
    }

    public void validate(final Optional<String> notAllowed) throws IOException, SAXException, ParserConfigurationException, TransformerException {
        if (notAllowed.isPresent()) {
            traceHR();
            throw new IllegalStateException("No value is allowed for option --validate");
        }
        for (final URL xsd : this.schema) {
            traceHR();
            traceUrl("validate with xsd", xsd);
            this.dom = ValidationUtils.validate(this.dom, xsd);
            traceHR();
            traceXml(this.dom);
        }
    }

    public void param(final Optional<String> keyColonValue) {
        traceHR();
        if (!(keyColonValue.isPresent() && keyColonValue.get().contains(":"))) {
            throw new IllegalStateException("Invalid format for option --param=key:value");
        }
        final String[] r2 = Arrays.copyOf(keyColonValue.get().split(":", 2), 2);
        this.params.put(r2[0], Objects.isNull(r2[1]) ? "" : r2[1]);
        trace(String.format("parameter:  %s:%s", r2[0], this.params.get(r2[0])));
    }



    private void traceXml(final Node dom) throws IOException, TransformerException {
        if (!this.trace) {
            return;
        }
        TransformUtils.serialize(dom, new BufferedOutputStream(new FileOutputStream(FileDescriptor.err)), true);
    }

    private void traceUrl(final String label, final URL url) {
        if (!this.trace) {
            return;
        }
        System.err.println(label+": "+url);
    }

    private void traceBool(final String label, final boolean b) {
        if (!this.trace) {
            return;
        }
        System.err.println(label+": "+b);
    }

    private void traceHR() {
        if (!this.trace) {
            return;
        }
        System.err.println("------------------------------------------------------------------------------------------------");
    }

    private void trace(final String message) {
        if (!this.trace) {
            return;
        }
        System.err.println(message);
    }

    private static boolean parseBoolean(final String option, final Optional<String> b) {
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

    private static URL asUrl(final String pathOrUrl) throws IOException
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
