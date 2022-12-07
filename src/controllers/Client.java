package controllers;

import models.messages.HandshakeMessage;

import java.io.*;
import java.net.*;
import java.util.*;
import java.lang.*;

public class Client extends Thread {
    Peer thisPeer, targetPeer;
    BitSet bitfield;
    Socket socket;

    public Client(Peer thisPeer, Peer targetPeer, BitSet bitfield) {
        this.thisPeer = thisPeer;
        this.targetPeer = targetPeer;
        this.bitfield = bitfield;
    }

    @Override
    public void run() {
        // Create a socket to the other peer's server listener
        try {
            this.socket = new Socket(targetPeer.hostname, targetPeer.port);
            System.out.println("Connected to peer ID: " + targetPeer.getId() + " on port: " + targetPeer.getPort());

            // Create the data output and input streams
            DataOutputStream outputStream = new DataOutputStream(this.socket.getOutputStream());
            DataInputStream inputStream = new DataInputStream(this.socket.getInputStream());

            // Send the handshake message
            HandshakeMessage sendHandshake = new HandshakeMessage(thisPeer.getId());
            outputStream.write(sendHandshake.getMessageBytes());
            outputStream.flush();
            System.out.println("Handshake sent");

            // Get the response handshake
//            System.out.println("Awaiting response handshake");
//            byte[] messageBytes = inputStream.readAllBytes();
//            HandshakeMessage receiveHandshake = new HandshakeMessage(messageBytes);
//
//            System.out.println("Received handshake from peer ID: " + receiveHandshake.getPeerId());

            socket.close();
        } catch(Exception e) {
            System.out.println("Error sending the handshake: " + e.getMessage());
            return;
        }
    }

    public Socket getSocket() {
        return this.socket;
    }
}