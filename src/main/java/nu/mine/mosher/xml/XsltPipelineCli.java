package nu.mine.mosher.xml;



import nu.mine.mosher.gnopt.Gnopt;

import javax.xml.transform.TransformerException;
import java.io.*;

public class XsltPipelineCli {
    public static void main(final String... args) throws IOException, TransformerException, Gnopt.InvalidOption {
        final XsltPipelineCliOptions opts = Gnopt.process(XsltPipelineCliOptions.class, args);
        opts.serialize(new BufferedOutputStream(new FileOutputStream(FileDescriptor.out)));
        System.out.flush();
        System.err.flush();
    }
}
