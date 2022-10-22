package controllers;

import exceptions.AppConfigException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

public class App {
    private static App instance = null;

    // File names
    static final String CONFIG_FILENAME = "Common.cfg";
    static final String PEER_FILENAME = "PeerInfo.cfg";

    // Config variables
    int numPreferredNeighbors;
    int unchokingInterval;
    int optimisticChokingInterval;
    String filename;
    int fileSize;
    int pieceSize;

    // Peer data
    ArrayList<Peer> peers = new ArrayList<Peer>();

    // Private constructor - singleton class
    // (1) Read in the config file
    // (2) Read in the peer info file
    // Performs any validation of configuration files and throws any errors that might occur
    // This constructor does not initiate any functionality of the application, it just deals with configuration
    private App() throws AppConfigException {
        // Read config file, store the data, and throw any errors
        try {
            BufferedReader br = new BufferedReader(new FileReader(CONFIG_FILENAME));
            // If any of these variables do not exist in the file, throw an error
            // Use the getConfigValue function to retrieve the variables from the file
            // The function will validate if any are missing or in the wrong order
            numPreferredNeighbors = Integer.parseInt(getConfigValue("NumberOfPreferredNeighbors", br.readLine()));
            unchokingInterval = Integer.parseInt(getConfigValue("UnchokingInterval", br.readLine()));
            optimisticChokingInterval = Integer.parseInt(getConfigValue("OptimisticUnchokingInterval", br.readLine()));
            filename = getConfigValue("FileName", br.readLine());
            fileSize = Integer.parseInt(getConfigValue("FileSize", br.readLine()));
            pieceSize = Integer.parseInt(getConfigValue("PieceSize", br.readLine()));
            br.close();
        } catch(Exception e) {
            throw new AppConfigException("Error reading configuration file: " + CONFIG_FILENAME, e);
        }

        // Read the peer info file, store them in the peer list, and throw any errors
        try {
            BufferedReader br = new BufferedReader(new FileReader(PEER_FILENAME));
            String line = "";
            // Iterate through the list of peers in the file, create new peer objects, and add them to the "peers" list
            while((line = br.readLine()) != null) {
                // For every peer in the file, we should create a new peer object and store it
                Peer p = parsePeer(line);
                peers.add(p);
            }
            br.close();
        } catch(Exception e) {
            throw new AppConfigException("Error reading peer info file: " + PEER_FILENAME, e);
        }
    }

    // Singleton class static instance is returned instead of constructing a new instance
    public static App getApp() throws AppConfigException {
        // Initialize the app and throw any errors
        if(instance == null) {
            try {
                instance = new App();
            } catch(Exception e) {
                throw new AppConfigException("Error initializing the application", e);
            }
        }

        return instance;
    }

    // Read a configuration variable from a line in the config file, handle any errors that might occur
    String getConfigValue(String variableName, String line)  throws AppConfigException {
        if(line == null) {
            throw new AppConfigException("Variable: [" + variableName + "]");
        }

        // Check that the current variable has the correct name
        String[] pieces =line.split(" ");
        if(!pieces[0].equals(variableName)) {
            throw new AppConfigException("Incorrect name for variable: [" + variableName + "]");
        }

        // Check that the current variable has the correct number of values (1)
        if(pieces.length != 2) {
            throw new AppConfigException("Incorrect number of values for [" + variableName + "]: " + pieces.length);
        }

        return pieces[1];
    }

    // Return a peer object created from the information in the data string
    // Parse the string and validate that it contains the necessary peer information
    // Throw an error if the string does not have the required data
    Peer parsePeer(String data) throws AppConfigException {
        String[] values = data.split(" ");

        // Verify that there is the correct number of values in the data string
        if(values.length != 4) {
            throw new AppConfigException("Incorrect amount of values given for peer");
        }

        // Create the peer object with the parsed values
        return new Peer(
                Integer.parseInt(values[0]),
                values[1],
                Integer.parseInt(values[2]),
                Integer.parseInt(values[3]) == 0 ? false : true
        );
    }

    // Run the peer application
    // Spawn additional client/server threads and handle incoming connections
    // Choke/unchoke connections as necessary
    public void run() throws Exception {
        while(true) {

        }
    }
}
