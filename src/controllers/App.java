package controllers;

import exceptions.AppConfigException;
import models.messages.HandshakeMessage;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.BitSet;

public class App {
    private static App instance = null;

    // File names
    static final String COMMON_CONFIG_FILENAME = "Common.cfg";
    static final String PEER_INFO_FILENAME = "PeerInfo.cfg";

    // Config variables
    int numPreferredNeighbors;
    int unchokingInterval;
    int optimisticChokingInterval;
    String filename;
    int fileSize;
    int pieceSize;
    // Total number of pieces needed to download the entire file
    int numPieces;

    // This application's peer information
    Peer thisPeer = null;

    // Peer data
    ArrayList<Peer> peers = new ArrayList<>();

    // Bitfield - used to store what pieces this peer has
    BitSet bitfield;

    // Private constructor - singleton class
    // This constructor is called in the public getApp function
    private App(int peerId) throws AppConfigException {
        // Read in the config file data and store it in the app
        readConfigFiles();

        // Set the current peer using the peerId passed in and searching the list of peers given in the file
        for(Peer p : peers) {
            if(p.id == peerId) {
                this.thisPeer = p;
            }
        }

        if(this.thisPeer == null) {
            throw new AppConfigException("A peer with the given ID does not exist: " + peerId);
        }

        // Calculate the total number of pieces = ceil(fileSize / pieceSize)
        this.numPieces = (int)Math.ceil((double)fileSize / (double)pieceSize);
        // Initialize the bitField with the corresponding number of bits
        this.bitfield = new BitSet(this.numPieces);
        // If this peer has the entire file, then the bitField is all 1's
        if(this.thisPeer.hasFile) {
            bitfield.set(0, bitfield.length());
        }

        System.out.println("This peer has the bitfield: ");
        for(int i = 0; i < bitfield.length(); i++) {
            System.out.print(bitfield.get(i));
        }
    }

    // Performs any validation of configuration files and throws any errors that might occur
    // This constructor does not initiate any functionality of the application, it just deals with configuration
    private void readConfigFiles() throws AppConfigException {
        // Read config file, store the data, and throw any errors
        try {
            BufferedReader br = new BufferedReader(new FileReader(COMMON_CONFIG_FILENAME));
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
            throw new AppConfigException("Error reading configuration file: " + COMMON_CONFIG_FILENAME, e);
        }

        // Read the peer info file, store them in the peer list, and throw any errors
        try {
            BufferedReader br = new BufferedReader(new FileReader(PEER_INFO_FILENAME));
            // Iterate through the list of peers in the file, create new peer objects, and add them to the "peers" list
            String line = "";
            while((line = br.readLine()) != null) {
                // For every peer in the file, we should create a new peer object and store it
                Peer p = parsePeer(line);
                peers.add(p);
            }
            br.close();
        } catch(Exception e) {
            throw new AppConfigException("Error reading peer info file: " + PEER_INFO_FILENAME, e);
        }
    }

    // Singleton class static instance is returned instead of constructing a new instance
    public static App getApp(int peerId) throws AppConfigException {
        // Initialize the app and throw any errors
        if(instance == null) {
            try {
                instance = new App(peerId);
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
        int peerId = Integer.parseInt(values[0]);
        String hostName = values[1];
        int port = Integer.parseInt(values[2]);
        boolean hasFile = Integer.parseInt(values[3]) == 0 ? false : true;

        return new Peer(
                peerId,
                hostName,
                port,
                hasFile
        );
    }

    // Run the peer application
    // Spawn additional client/server threads and handle incoming connections
    // Choke/unchoke connections as necessary
    public void run() throws Exception {
//        Client client;
//        Server server;

        HandshakeMessage handshakeMessage = new HandshakeMessage(thisPeer.id);

        // Get all peers that started before this peer
        ArrayList<Peer> connections = new ArrayList<>();
        for(Peer p : peers) {
            if(p.id != thisPeer.id) {
                connections.add(p);
            } else {
                break;
            }
        }

        // Connect to peers (if there are any)
        System.out.println("Connecting to " + connections.size() +  " peers");
        for(int i = 0; i < connections.size(); i++) {
            Client clientThread = new Client();

            clientThread.start();
        }

        // Listen for peers
        ServerSocket serverSocket = new ServerSocket(6969);
        while(true) {
            Socket connectionSocket = serverSocket.accept();
            DataInputStream inputStream = new DataInputStream(connectionSocket.getInputStream());

            // The first message should be a handshake - attempt to construct a handshake message
            byte[] messageBytes = inputStream.readAllBytes();

            System.out.println("Received " + messageBytes.length + " bytes");

            // DEBUG print out the bytes
            String hex = "";
            for(byte b : messageBytes) {
                hex += String.format("%02X", b);
            }
            System.out.println(hex);

            try {
                HandshakeMessage hsMessage = new HandshakeMessage(messageBytes);
                System.out.println("Connected with client ID: " + hsMessage.getPeerId());
            } catch(Exception e) {
                throw e;
            }
        }
    }
}
