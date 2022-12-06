import controllers.App;
import exceptions.CLIException;
import exceptions.ConnectionException;

import java.io.IOException;

public class peerProcess {
    public static void main(String[] args) throws Exception {
        System.out.println("Working directory: " + System.getProperty("user.dir"));
        System.out.println("Starting application - Loading configuration files");

        // Take command line input - peerID
        if(args.length != 1) {
            throw new CLIException("Incorrect number of arguments given");
        }

        // Set the peer's ID to the argument passed in from the command line
        int peerId;
        try {
            peerId = Integer.parseInt(args[0]);
        } catch(Exception e) {
            throw new CLIException("Invalid input for argument: Peer ID");
        }

        // Create an instance of the peer application
        App app;
        try {
            app = App.getApp(peerId);
        } catch(Exception e) {
            throw e;
        }

        // Run the File Share Application
        System.out.println("Starting application - Starting peer application");
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