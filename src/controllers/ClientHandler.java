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
    BitSet thisBitfield, targetBitfield;
    Socket socket;
    DataInputStream inStream;
    DataOutputStream outStream;

    public ClientHandler(Peer thisPeer, Peer targetPeer, BitSet bitfield,
                         Socket socket, DataInputStream inStream, DataOutputStream outStream) throws Exception {
        this.thisPeer = thisPeer;
        this.targetPeer = targetPeer;
        this.socket = socket;
        this.thisBitfield = bitfield;

        this.inStream = inStream;
        this.outStream = outStream;
    }

    @Override
    public void run() {
        try {
            System.out.println("Running the client handler thread");
            // Send the bitfield message
            Message sendBitfieldMessage = Message.makeBitfieldMessage(this.thisBitfield);
            sendMessage(this.outStream, sendBitfieldMessage);

            // Receive the bitfield message
            Message receiveBitfieldMessage = receiveMessage(inStream);
            this.targetBitfield = receiveBitfieldMessage.getBitfield();
            System.out.println("Received bitfield: " + this.targetBitfield.toString());

            // Determine interest from the received bitfield
            BitSet comparisonSet = this.targetBitfield;
            comparisonSet.andNot(this.thisBitfield);

            // If there are bits we don't have, send an INTERESTED message
            // Otherwise, send NOT_INTERESTED
            Message sendInterestMessage;
            if(comparisonSet.cardinality() > 0) {
                sendInterestMessage = new Message(Message.MessageType.INTERESTED);
            } else {
                sendInterestMessage = new Message(Message.MessageType.NOT_INTERESTED);
            }

            sendMessage(this.outStream, sendInterestMessage);

            // Receive interest messages
            Message receiveInterestMessage = receiveMessage(this.inStream);
            switch(receiveInterestMessage.getType()) {
                case INTERESTED: {
                    System.out.println("Received INTERESTED message from: " + this.targetPeer.getId());
                    break;
                }
                case NOT_INTERESTED: {
                    System.out.println("Received NOT_INTERESTED message from: " + this.targetPeer.getId());
                    break;
                }
            }

        } catch(Exception e) {
            System.out.println("Error running the client handler thread: " + e.toString());
        }
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