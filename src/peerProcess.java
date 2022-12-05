import controllers.App;
import exceptions.CLIException;
import exceptions.ConnectionException;

import java.io.IOException;

public class peerProcess {
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

        // Take command line input - peerID
        if(args.length != 1) {
            throw new CLIException("Incorrect number of arguments given");
        }

        // Set the peer's ID to the argument passed in from the command line
        try {
            int peerID = Integer.parseInt(args[0]);
            app.setPeerID(peerID);
            System.out.println("Application created with peer ID: " + peerID);
        } catch(Exception e) {
            throw new CLIException("Invalid input for argument: Peer ID");
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