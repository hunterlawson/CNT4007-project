package controllers;

import models.messages.HandshakeMessage;
import models.messages.Message;

import java.io.*;
import java.net.*;
import java.util.*;
import java.lang.*;

public class ClientHandler extends Thread {
    Peer thisPeer, targetPeer;
    BitSet bitfield;
    Socket socket;
    DataInputStream inStream;
    DataOutputStream outStream;

    public ClientHandler(Peer thisPeer, Peer targetPeer, BitSet bitfield,
                         Socket socket, DataInputStream inStream, DataOutputStream outStream) throws Exception {
        this.thisPeer = thisPeer;
        this.targetPeer = targetPeer;
        this.socket = socket;
        this.bitfield = bitfield;

        this.inStream = inStream;
        this.outStream = outStream;
    }

    @Override
    public void run() {
        try {
            // Send the bitfield message
            System.out.println("bubba bubba bubba");
        } catch(Exception e) {
            System.out.println("Error running the client handler thread: " + e.getMessage());
        }
    }

    void sendMessage(Message message) throws Exception {
        this.outStream.write(message.getMessageBytes());
    }

    Message receiveMessage() throws Exception {
        byte[] messageBytes = this.inStream.readAllBytes();
        return new Message(messageBytes);
    }
}