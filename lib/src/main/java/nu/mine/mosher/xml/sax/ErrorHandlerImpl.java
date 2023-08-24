package nu.mine.mosher.xml.sax;


import org.xml.sax.*;


public class ErrorHandlerImpl implements ErrorHandler {
    public void error(SAXParseException spe) throws SAXException {
        throw new SAXException("ERROR " + getParseExceptionInfo(spe), spe);
    }

    public void fatalError(SAXParseException spe) throws SAXException {
        throw new SAXException("FATAL " + getParseExceptionInfo(spe), spe);
    }

    public void warning(SAXParseException spe) throws SAXException {
        throw new SAXException("WARN " + getParseExceptionInfo(spe), spe);
    }



    private static String getParseExceptionInfo(SAXParseException spe) {
        return String.format("uri: %s, line: %d, msg: %s", spe.getSystemId(), spe.getLineNumber(), spe.getMessage());
    }
}
