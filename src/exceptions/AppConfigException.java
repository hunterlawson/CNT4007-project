package exceptions;

public class AppConfigException extends Exception {
    public AppConfigException(String msg, Exception err) {
        super(msg, err);
    }
    public AppConfigException(String msg) {
        super(msg);
    }
}
