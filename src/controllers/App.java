package controllers;

import exceptions.AppConfigException;
import models.messages.HandshakeMessage;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;

public class App {
    private static App instance = null;
    public static boolean running = true;

    // Store the peerId and a boolean that represents if the neighbor is choked or not
    public static HashMap<Integer, Boolean> neighbors = new HashMap<>();
    public static HashMap<Integer, Boolean> interestedNeighbors = new HashMap<>();
    public static HashMap<Integer, BitSet> bitfieldMap = new HashMap<>();

    PeerLogger logger;

    //file object
    static public RandomAccessFile file;

    // File names
    static final String COMMON_CONFIG_FILENAME = "Common.cfg";
    static final String PEER_INFO_FILENAME = "PeerInfo.cfg";

    // Config variables
    static int numPreferredNeighbors;
    int unchokingInterval;
    int optimisticChokingInterval;
    String filename;
    static int fileSize;
    static int pieceSize;
    // Total number of pieces needed to download the entire file
    static int numPieces;

    // This application's peer information
    Peer thisPeer = null;

    // Peer data
    ArrayList<Peer> peers = new ArrayList<>();

    // Private constructor - singleton class
    // This constructor is called in the public getApp function
    private App(int peerId) throws Exception {
        // Read in the config file data and store it in the app
        readConfigFiles();

        logger = new PeerLogger(peerId);

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
        numPieces = (int)Math.ceil((double)fileSize / (double)pieceSize);

        System.out.println("NumPieces: " + numPieces);

        // Insert this peer's bitfield into the bitfieldMap
        BitSet thisBitfield = new BitSet();
        // If this peer has the entire file, then the bitField is all 1's
        if(thisPeer.isHasFile()) {
            thisBitfield.set(0, numPieces);
        } else {
            thisBitfield.clear(0, numPieces);
        }


        System.out.println("Created bitfield of size: " + thisBitfield.length());

        bitfieldMap.put(this.thisPeer.getId(), thisBitfield);

        // Create this peer's file
        newPeerFile(this.thisPeer);
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

    HandshakeMessage receiveHandshakeMessage(DataInputStream inStream) throws Exception {
        // Handshake messages are always 32 bytes in size
        byte[] messageBytes = inStream.readNBytes(32);
        return new HandshakeMessage(messageBytes);
    }

    void sendHandshakeMessage(DataOutputStream outStream, HandshakeMessage message) throws Exception {
        outStream.write(message.getMessageBytes());
        outStream.flush();
    }

    public void newPeerFile(Peer thisPeer) throws Exception {
        File newPeerDirectory = new File("peer_" + thisPeer.id + "/");
        if(!newPeerDirectory.exists()) {
            newPeerDirectory.mkdirs();
        }
        file = new RandomAccessFile("peer_" + thisPeer.id + "/" + filename, "rw");

        System.out.println("file created");
    }

    public static synchronized byte[] readData(int pieceIndex) throws IOException {
        int startPosition = pieceIndex * pieceSize;
        byte[] pieceBytes;
        if(pieceIndex == numPieces - 1){
            pieceBytes = new byte[fileSize % numPieces];
        } else {
            pieceBytes = new byte[pieceSize];
        }

        System.out.println("Reading file: " + startPosition + ", " + pieceBytes.length);

        file.seek(startPosition);
        ByteBuffer dataBuffer = ByteBuffer.allocate(pieceBytes.length);
        for(int i = 0; i < pieceBytes.length; i++) {
            dataBuffer.put(file.readByte());
        }
        //System.out.println(pieceBytes[i]);

        return dataBuffer.array();
    }

    public static synchronized void writeData(int pieceIndex, byte[] pieceBytes) throws IOException {
        int startingPosition = pieceIndex * pieceSize;
        file.seek(startingPosition);
        file.write(pieceBytes, 0, pieceBytes.length);
    }

    // Run the peer application
    // Spawn additional client/server threads and handle incoming connections
    // Choke/unchoke connections as necessary
    public void run() throws Exception {
        // Get all peers that started before this peer
        ArrayList<Peer> previousPeers = new ArrayList<>();
        for(Peer p : peers) {
            if(p.id != thisPeer.id) {
                previousPeers.add(p);
            } else {
                break;
            }
        }

        // Connect to all previous peers (if there are any)
        System.out.println("Connecting to " + previousPeers.size() +  " peers");
        for(int i = 0; i < previousPeers.size(); i++) {
            Peer targetPeer = previousPeers.get(i);

            // Connect to the target peer
            Socket socket = new Socket(targetPeer.hostname, targetPeer.port);
            System.out.println("Connected to peer ID: " + targetPeer.getId() + " on port: " + targetPeer.getPort());
            logger.writeLog("Peer " + this.thisPeer.id + " makes a connection to Peer " + targetPeer.id);

            // Create the data output and input streams
            DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
            DataInputStream inputStream = new DataInputStream(socket.getInputStream());

            // Send the handshake message
            HandshakeMessage sendHandshake = new HandshakeMessage(thisPeer.getId());
            sendHandshakeMessage(outputStream, sendHandshake);
            System.out.println("Handshake sent to peer ID: " + targetPeer.getId());

            // Wait for a response handshake message
            HandshakeMessage receiveHandshake = receiveHandshakeMessage(inputStream);
            int targetId = receiveHandshake.getPeerId();
            System.out.println("Received handshake from peer ID: " + targetId);
            logger.writeLog("Peer " + this.thisPeer.id + " received handshake from " + targetId);
            // Check that the ID from the response handshake is correct
            if(receiveHandshake.getPeerId() != targetPeer.getId()) {
                System.out.println("The response handshake ID does not match!");
                return;
            }

            // Otherwise, we have established a proper connection
            // Spawn a handler thread to handle further interactions with the other peer
            ClientHandler clientHandler = new ClientHandler(this.thisPeer, targetPeer,
                    socket, inputStream, outputStream, logger);

            clientHandler.start();
        }

        // Peer server connection listener
        ServerSocket serverSocket = new ServerSocket(this.thisPeer.port);

        while(running) {
            System.out.println("Starting server socket listener...");
            Socket socket = serverSocket.accept();
            System.out.println("Accepted TCP connection");

            DataInputStream inputStream = new DataInputStream(socket.getInputStream());
            DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());

            // Receive handshake messages from other peers
            HandshakeMessage receiveHandshake = receiveHandshakeMessage(inputStream);
            int targetId = receiveHandshake.getPeerId();
            System.out.println("Handshake message received from peer with ID: " + targetId);
            logger.writeLog("Peer " + this.thisPeer.id + " received handshake from " + targetId);
            // Send a response handshake
            System.out.println("Sending a handshake to peer with ID: " + receiveHandshake.getPeerId());
            HandshakeMessage sendHandshake = new HandshakeMessage(this.thisPeer.getId());
            sendHandshakeMessage(outputStream, sendHandshake);

            // Get the peer associated with the peerID in the handshake message
            Peer targetPeer = this.thisPeer;
            for(Peer peer : peers) {
                if(peer.getId() == receiveHandshake.getPeerId()) {
                    targetPeer = peer;
                    break;
                }
            }

            // We've now established a connection
            // Spawn a handler thread to handle the rest of the connection
            ClientHandler clientHandler = new ClientHandler(this.thisPeer, targetPeer,
                    socket, inputStream, outputStream, logger);

            clientHandler.start();
        }
    }
}
