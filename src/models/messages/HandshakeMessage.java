package models.messages;

import exceptions.InvalidHandshakeException;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class HandshakeMessage {
    final static String HEADER = "P2PFILESHARINGPROJ";
    final static int messageSize = 32;
    int peerId;

    public HandshakeMessage(int peerId) {
        this.peerId = peerId;
    }

    // Construct a handshake message from a byte array
    public HandshakeMessage(byte[] bytes) throws InvalidHandshakeException {
        // Check that the handshake header is correct
        ByteBuffer headerBuffer = ByteBuffer.allocate(18);
        byte[] headerBytes = headerBuffer.put(bytes, 0, 18).array();
        String headerString = new String(headerBytes, StandardCharsets.UTF_8);
        if(!headerString.equals(HEADER)) {
            throw new InvalidHandshakeException(headerString + " does not equal: " + HEADER);
        }

        // The peer ID is stored in the last 4 bytes
        ByteBuffer peerIdBuffer = ByteBuffer.wrap(bytes, bytes.length - 4, 4);
        // Convert the bytes into the integer representation
        this.peerId = peerIdBuffer.getInt();
    }

    public int getPeerId() {
        return peerId;
    }

    public void setPeerId(int peerId) {
        this.peerId = peerId;
    }

    // Return the message represented as a byte array
    public byte[] getMessageBytes() {
        byte[] messageBytes = new byte[messageSize];

        // Add the 18-byte message header
        byte[] headerBytes = HEADER.getBytes();
        for(int i = 0; i < 18; i++) {
            messageBytes[i] = headerBytes[i];
        }

        // Add the 10-byte 0 bits
        for(int i = 18; i < 28; i++) {
            messageBytes[i] = 0x0;
        }

        // Add the 4-byte peerId - convert into a byte array using a ByteBuffer
        byte[] peerIdBytes = ByteBuffer.allocate(4).putInt(peerId).array();
        for(int i = 28; i < 32; i++) {
            messageBytes[i] =peerIdBytes[i - 28];
        }

        return messageBytes;
    }
}
