package exceptions;

// This message should be thrown when creating a message with invalid input
public class InvalidMessageException extends Exception {
    public InvalidMessageException(String msg) {
        super(msg);
    }
}
