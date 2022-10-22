package exceptions;

// This exception should be thrown when any connection-related errors occur
public class ConnectionException extends Exception {
    public ConnectionException(String msg, Exception e) {
        super(msg, e);
    }

    public ConnectionException(String msg) {
        super(msg);
    }
}
