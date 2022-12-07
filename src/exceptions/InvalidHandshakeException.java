package exceptions;

// This exception should be thrown when a handshake is invalid
public class InvalidHandshakeException extends Exception {
    public InvalidHandshakeException(String msg, Exception e) {
        super(msg, e);
    }

    public InvalidHandshakeException(String msg) {
        super(msg);
    }
}
