package controllers;

import exceptions.AppConfigException;
import models.Peer;

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
    ArrayList<Peer> peers = new ArrayList<Peer>();;

    /*
        Private constructor - singleton class

        (1) Read in the config file
        (2) Read in the peer info file
    */
    private App() throws AppConfigException {
        // Read config file, store the data, and throw any errors
        try {
            BufferedReader br = new BufferedReader(new FileReader(CONFIG_FILENAME));
            // If any of these variables do not exist in the file, throw an error
            numPreferredNeighbors = Integer.parseInt(getConfigValue("NumberOfPreferredNeighbors", br.readLine()));
            unchokingInterval = Integer.parseInt(getConfigValue("UnchokingInterval", br.readLine()));
            optimisticChokingInterval = Integer.parseInt(getConfigValue("OptimisticUnchokingInterval", br.readLine()));
            filename = getConfigValue("FileName", br.readLine());
            fileSize = Integer.parseInt(getConfigValue("FileSize", br.readLine()));
            pieceSize = Integer.parseInt(getConfigValue("PieceSize", br.readLine()));
            br.close();
        } catch(Exception e) {
            throw new AppConfigException("Error reading configuration file", e);
        }

        // Read the peer info file, store them in the peer list, and throw any errors
        try {
            BufferedReader br = new BufferedReader(new FileReader(PEER_FILENAME));
            String line = "";
            // Iterate through the list of peers in the file, create new peer objects, and add them to the "peers" list
            while((line = br.readLine()) != null) {
                String[] values = line.split(" ");
                peers.add(new Peer(
                    Integer.parseInt(values[0]),
                    values[1],
                    Integer.parseInt(values[2]),
                    Integer.parseInt(values[3]) == 1 ? true : false
                ));
            }
            br.close();
        } catch(Exception e) {
            throw new AppConfigException("Error reading peer info file", e);
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

        String[] pieces =line.split(" ");
        if(!pieces[0].equals(variableName)) {
            throw new AppConfigException("Incorrect name for variable: [" + variableName + "]");
        }
        if(pieces.length != 2) {
            throw new AppConfigException("Incorrect number of values for [" + variableName + "]: " + pieces.length);
        }

        return pieces[1];
    }

    public void run() throws Exception {

    }
}
