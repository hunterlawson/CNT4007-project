package exceptions;

// This exception should be thrown when errors are encountered regarding command line input
// For example, reading the peerID in from the command line arguments
public class CLIException extends Exception {
    public CLIException(String msg, Exception e) {
        super(msg, e);
    }
    public CLIException(String msg) {
        super(msg);
    }
}
