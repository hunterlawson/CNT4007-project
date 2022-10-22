import controllers.App;
import exceptions.ConnectionException;

import java.io.IOException;

public class Main {


    public static void main(String[] args) throws Exception {
        System.out.println("Working directory: " + System.getProperty("user.dir"));
        System.out.println("Starting application - Loading configuration files");

        // Exceptions occurred when creating the application should be forwarded to the calling process
        // These exceptions can't be handled at runtime because configuring the application should not fail
        App app;
        try {
            app = App.getApp();
        } catch(Exception e) {
            throw e;
        }

        System.out.println("Starting application - Starting peer listener");

        // Exceptions occurred when running the application can be caught depending on the type
        try {
            app.run();
        } catch(ConnectionException e) {
            throw new Exception("An error has occurred with the connection", e);
        } catch(IOException e) {
            throw new Exception("An error has occurred with the file system", e);
        } catch(Exception e) {
            throw e;
        }
    }

}