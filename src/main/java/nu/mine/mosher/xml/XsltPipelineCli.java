package nu.mine.mosher.xml;



import nu.mine.mosher.gnopt.Gnopt;
import nu.mine.mosher.xml.transform.TransformUtils;

import javax.xml.transform.TransformerException;
import java.io.BufferedOutputStream;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;

public class XsltPipelineCli {
    public static void main(final String... args) throws IOException, TransformerException, Gnopt.InvalidOption {
        final XsltPipelineCliOptions opts = Gnopt.process(XsltPipelineCliOptions.class, args);

        if (opts.trace) {
            hr();
            System.err.println("final output");
            hr();
            System.err.flush();
        }

        TransformUtils.serialize(opts.dom, new BufferedOutputStream(new FileOutputStream(FileDescriptor.out)), opts.pretty);

        System.out.flush();
        System.err.flush();
    }

    private static void hr() {
        System.err.println("------------------------------------------------------------------------------------------------");
    }
}
