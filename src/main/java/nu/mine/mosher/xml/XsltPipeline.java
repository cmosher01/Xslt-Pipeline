package nu.mine.mosher.xml;


import nu.mine.mosher.xml.dom.DomUtils;
import nu.mine.mosher.xml.transform.TransformUtils;
import nu.mine.mosher.xml.validation.ValidationUtils;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.*;
import java.net.URL;
import java.util.*;


@SuppressWarnings({"unused"})
public class XsltPipeline {
    public void dom(final BufferedInputStream in) throws ParserConfigurationException, IOException, SAXException, TransformerException {
        traceHR();
        this.dom = DomUtils.asDom(in, this.validation, this.schema);
        trace("load dom from input stream");
        traceHR();
        traceXml(this.dom);
    }

    public void dom(final URL url) throws ParserConfigurationException, IOException, SAXException, TransformerException {
        traceHR();
        this.dom = DomUtils.asDom(url, this.validation, this.schema);
        traceUrl("load dom", url);
        traceHR();
        traceXml(this.dom);
    }

    public void dom() throws ParserConfigurationException, IOException, TransformerException {
        traceHR();
        this.dom = DomUtils.empty();
        trace("generate empty dom");
        traceHR();
        traceXml(this.dom);
    }

    public void initialTemplate(final boolean initialTemplate) {
        this.initialTemplate = initialTemplate;
        traceHR();
        traceBool("initial-template", this.initialTemplate);
    }

    public void param(final String key, final Object value) {
        traceHR();
        final String s = Objects.toString(value);
        this.params.put(key, s);
        trace(String.format("parameter:  %s:%s", key, s));
    }

    public void pretty(final boolean pretty) {
        this.pretty = pretty;
        traceHR();
        traceBool("pretty", this.pretty);
    }

    public void xmldecl(final boolean xmldecl) {
        this.xmldecl = xmldecl;
        traceHR();
        traceBool("xmldecl", this.xmldecl);
    }

    public void serialize(final BufferedOutputStream out) throws IOException, TransformerException {
        traceHR();
        trace("final output");
        traceHR();
        if (Objects.isNull(this.dom)) {
            trace("[No DOM defined.]");
            return;
        }
        traceXml(this.dom);
        System.err.flush();
        TransformUtils.serialize(this.dom, out, this.pretty, this.xmldecl);
    }

    public void trace(final boolean trace) {
        this.trace = trace;
        traceHR();
        traceBool("trace", this.trace);
    }

    public void validate() throws IOException, SAXException, TransformerException {
        for (final URL xsd : this.schema) {
            traceHR();
            traceUrl("validate with xsd", xsd);
            this.dom = ValidationUtils.validate(this.dom, xsd);
            traceHR();
            traceXml(this.dom);
        }
    }

    public void validation(final boolean validation) {
        this.validation = validation;
        traceHR();
        traceBool("validation", this.validation);
    }

    public void xsd(final URL source) throws IOException, ParserConfigurationException, SAXException, TransformerException {
        traceHR();
        this.schema.add(source);
        traceUrl("register xsd", source);
        traceHR();
        traceXml(source);
    }

    public void xsd() {
        traceHR();
        this.schema = new ArrayList<>();
        trace("dropping all registered schema");
    }

    public void xslt(final URL source) throws IOException, TransformerException, ParserConfigurationException, SAXException {
        traceHR();
        traceUrl("transform with xslt", source);
        traceHR();
        traceXml(source);
        this.dom = TransformUtils.transform(this.dom, source, this.params, this.initialTemplate);
        this.params = new HashMap<>();
        traceHR();
        traceXml(this.dom);
    }

    public void xslt() throws IOException, TransformerException {
        traceHR();
        trace("identity transform");
        this.dom = TransformUtils.identity(this.dom);
        traceHR();
        traceXml(this.dom);
    }

    public Node accessDom() {
        return this.dom;
    }

    public void traceDom() throws IOException, TransformerException {
        traceXml(this.dom);
    }



    private Node dom;
    private boolean initialTemplate;
    private Map<String, Object> params = new HashMap<>();
    private boolean pretty;
    private boolean xmldecl;
    private List<URL> schema = new ArrayList<>();
    private boolean trace;
    private boolean validation;

    private void traceXml(final URL source) throws IOException, SAXException, ParserConfigurationException, TransformerException {
        if (!this.trace) {
            return;
        }
        if (Objects.isNull(source)) {
            return;
        }
        traceXml(DomUtils.asDom(source, false, Collections.emptyList()));
    }

    private void traceXml(final Node dom) throws IOException, TransformerException {
        if (!this.trace) {
            return;
        }
        if (Objects.isNull(dom)) {
            return;
        }
        TransformUtils.serialize(dom, new BufferedOutputStream(new FileOutputStream(FileDescriptor.err)), true, true);
    }

    private void traceUrl(final String label, final URL url) {
        if (!this.trace) {
            return;
        }
        var u = Objects.isNull(url) ? "<null>" : url.toExternalForm();
        if (Objects.isNull(label) || label.isBlank()) {
            System.err.println(u);
        } else {
            System.err.println(label + ": " + u);
        }
    }

    private void traceBool(final String label, final boolean b) {
        if (!this.trace) {
            return;
        }
        if (Objects.isNull(label) || label.isBlank()) {
            System.err.println("" + b);
        } else {
            System.err.println(label + ": " + b);
        }
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
        if (Objects.isNull(message) || message.isBlank()) {
            return;
        }
        System.err.println(message);
    }
}
