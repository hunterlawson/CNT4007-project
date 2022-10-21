package exceptions;

// This message should be thrown when creating a message with invalid input
public class InvalidMessage extends Exception {
    public InvalidMessage(String msg) {
        super(msg);
    }
}
