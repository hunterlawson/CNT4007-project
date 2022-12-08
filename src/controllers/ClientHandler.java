package controllers;

import models.messages.HandshakeMessage;
import models.messages.Message;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.*;
import java.lang.*;

public class ClientHandler extends Thread {
    Peer thisPeer, targetPeer;
    Socket socket;
    DataInputStream inStream;
    DataOutputStream outStream;
    PeerLogger logger;
    boolean choked = true;

    public ClientHandler(Peer thisPeer, Peer targetPeer,
                         Socket socket, DataInputStream inStream, DataOutputStream outStream, PeerLogger logger) throws Exception {
        this.thisPeer = thisPeer;
        this.targetPeer = targetPeer;
        this.socket = socket;

        this.inStream = inStream;
        this.outStream = outStream;

        this.logger = logger;
    }

    @Override
    public void run() {
        try {
            int thisId = this.thisPeer.getId();
            int targetId = this.targetPeer.getId();
            System.out.println("Running the client handler thread");
            // Send the bitfield message
            Message sendBitfieldMessage = Message.makeBitfieldMessage(App.bitfieldMap.get(thisId));
            sendMessage(this.outStream, sendBitfieldMessage);

            // Receive the bitfield message
            Message receiveBitfieldMessage = receiveMessage(inStream);
            BitSet receivedBitfield = receiveBitfieldMessage.getBitfield();
            App.bitfieldMap.put(targetId, receivedBitfield);
            System.out.println("Received bitfield: " + receivedBitfield.toString());
            logger.writeLog("Peer " + thisPeer.id + " received bitfield from: " + targetId);

            // Determine interest from the received bitfield
            BitSet comparisonSet = receivedBitfield;
            BitSet thisPeerBitfield = App.bitfieldMap.get(thisId);
            comparisonSet.andNot(thisPeerBitfield);

            // If there are bits we don't have, send an INTERESTED message
            // Otherwise, send NOT_INTERESTED
            Message sendInterestMessage;
            if(comparisonSet.cardinality() > 0) {
                sendInterestMessage = new Message(Message.MessageType.INTERESTED);
            } else {
                sendInterestMessage = new Message(Message.MessageType.NOT_INTERESTED);
            }

            sendMessage(this.outStream, sendInterestMessage);

            // If we were interested, add the peer to our neighbors:
            App.neighbors.put(targetId, false);

            // Receive and handle all messages
            while(App.running) {
                // Send any messages that we need to send

                // Send a request to a random piece from the target
                if(targetPeer.isHasFile() && !thisPeer.isHasFile()) {
                    int randomPieceIndex = getRandomPiece();
                    if(randomPieceIndex == -1) {
                        thisPeer.setHasFile(true);
                    } else {
                        byte[] requestPayload = ByteBuffer.allocate(4).putInt(randomPieceIndex).array();
                        Message requestMessage = new Message(Message.MessageType.REQUEST, requestPayload);

                        sendMessage(outStream, requestMessage);
                    }
                }

                // Receive any messages
                Message receivedMessage = receiveMessage(this.inStream);
                switch (receivedMessage.getType()) {
                    case INTERESTED -> {
                        System.out.println("Received INTERESTED message from: " + targetId);
                        logger.writeLog("Peer " + thisPeer.id + " received INTERESTED message from: " + targetId);
                        // Mark the neighbor peer as interested
                        App.interestedNeighbors.put(targetId, true);
                    }
                    case NOT_INTERESTED -> {
                        System.out.println("Received NOT_INTERESTED message from: " + targetId);
                        logger.writeLog("Peer " + thisPeer.id + " received NOT_INTERESTED message from: " + targetId);
                        // Mark the neighbor peer as not interested
                        App.interestedNeighbors.put(targetId, false);
                    }
                    case CHOKE -> {
                        System.out.println("Received CHOKE message from: " + targetId);
                        logger.writeLog("Peer " + thisPeer.id + " received CHOKE message from: " + targetId);
                        this.choked = true;
                        App.neighbors.put(targetId, true);
                    }
                    case UNCHOKE -> {
                        System.out.println("Received UNCHOKE message from: " + targetId);
                        logger.writeLog("Peer " + thisPeer.id + " received UNCHOKE message from: " + targetId);
                        this.choked = false;
                        App.neighbors.put(targetId, false);
                    }
                    case REQUEST -> {
                        System.out.println("Received REQUEST message from: " + targetId);
                        logger.writeLog("Peer " + thisPeer.id + " received REQUEST message from: " + targetId);
                        int pieceIndex = ByteBuffer.wrap(receivedMessage.getPayloadBytes()).getInt();
                        // Update the bitfield for that peer to reflect the new requested piece
                        updateBitfield(pieceIndex, targetId);
                        System.out.println("REQUESTING INDEX: " + pieceIndex);

                        // Send the requested piece
                        byte[] pieceBytes = App.readData(pieceIndex);
                        ByteBuffer pieceMessagePayload = ByteBuffer.allocate(pieceBytes.length + 4);
                        pieceMessagePayload.putInt(pieceIndex);
                        pieceMessagePayload.put(pieceBytes);
                        Message pieceMessage = new Message(Message.MessageType.PIECE, pieceMessagePayload.array());
                        sendMessage(outStream, pieceMessage);
                    }
                    case HAVE -> {
                        // Update the bitfield for the peer with the piece index that it has
                        System.out.println("Received HAVE message from: " + targetId);
                        logger.writeLog("Peer " + thisPeer.id + " received HAVE message from: " + targetId);
                        int pieceIndex = ByteBuffer.wrap(receivedMessage.getPayloadBytes()).getInt();
                        updateBitfield(pieceIndex, targetId);
                    }
                    case PIECE -> {
                        System.out.println("Received PIECE message from: " + targetId);
                        byte[] payloadBytes = receivedMessage.getPayloadBytes();
                        int payloadSize = payloadBytes.length - 4;
                        int pieceIndex = ByteBuffer.wrap(payloadBytes, 0, 4).getInt();
                        byte[] pieceBytes = ByteBuffer.wrap(payloadBytes, 4, payloadSize).array();
                        System.out.println("Piece size: " + payloadSize);

                        // Write to the file
                        App.writeData(pieceIndex, pieceBytes);

                        // Mark that we have received that piece in our bitfield
                        System.out.println("Piece index: " + pieceIndex);
                    }
                }
            }
        } catch(Exception e) {
            System.out.println("Error running the client handler thread: " + e.toString());
        }
    }

    // Return the index of a random piece that this client does not have and the other does
    int getRandomPiece() {
        BitSet comparisonSet = App.bitfieldMap.get(this.targetPeer.getId());
        BitSet thisPeerBitfield = App.bitfieldMap.get(this.thisPeer.getId());
        comparisonSet.andNot(thisPeerBitfield);

//        ArrayList<Integer> indices = new ArrayList<Integer>();
//        for(int i = comparisonSet.nextSetBit(0); i >= 0; i = comparisonSet.nextSetBit(i + 1)) {
//            indices.add(i);
//        }
//
//        if(indices.size() == 0) {
//            return -1;
//        }
//
//        int randomIndex = indices.get((int)(Math.random() * indices.size()));

        System.out.println("Num bits: " + comparisonSet.length());

        int randomIndex = comparisonSet.nextSetBit(0);

        if(randomIndex == -1) {
            return -1;
        }


        thisPeerBitfield.set(randomIndex);
        App.bitfieldMap.put(this.thisPeer.getId(), thisPeerBitfield);
        return randomIndex;
    }

    // Set the bit at the corresponding index for the peer with ID peerId
    void updateBitfield(int pieceIndex, int peerId) {
        BitSet targetBitfield = App.bitfieldMap.get(peerId);
        targetBitfield.set(pieceIndex);
        App.bitfieldMap.put(peerId, targetBitfield);
    }

    void sendMessage(DataOutputStream outStream, Message message) throws Exception {
        outStream.write(message.getMessageBytes());
        outStream.flush();
    }

    Message receiveMessage(DataInputStream inStream) throws Exception {
        // First 4 bytes of a message are the payload size
        int messageSize = ByteBuffer.wrap(inStream.readNBytes(4)).getInt();
        System.out.println("Received message with size: " + messageSize);

        // Next byte is the message type
        byte messageTypeByte = inStream.readByte();

        // Read the message payload
        byte[] payload = new byte[messageSize];
        inStream.readNBytes(payload, 0, messageSize);

        // Next byte is the message type
        Message.MessageType type = Message.MessageType.values()[messageTypeByte];

        return new Message(type, payload);
    }
}