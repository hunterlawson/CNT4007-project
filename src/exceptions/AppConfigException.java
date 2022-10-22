package exceptions;

// This exception should be thrown whenever the application encounters an error while it is
// initializing its values from the configuration files (in the App class constructor)
public class AppConfigException extends Exception {
    public AppConfigException(String msg, Exception err) {
        super(msg, err);
    }

    public AppConfigException(String msg) {
        super(msg);
    }
}
