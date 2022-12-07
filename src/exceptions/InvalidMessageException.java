package exceptions;

// This exception should be thrown when creating a message with invalid input
public class InvalidMessageException extends Exception {
    public InvalidMessageException(String msg, Exception e) {
        super(msg, e);
    }

    public InvalidMessageException(String msg) {
        super(msg);
    }
}
