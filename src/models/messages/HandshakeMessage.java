package models.messages;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class HandshakeMessage {
    final static String header = "P2PFILESHARINGPROJ";
    final static int messageSize = 32;
    int peerId;

    public HandshakeMessage(int peerId) {
        this.peerId = peerId;
    }

    public int getPeerId() {
        return peerId;
    }

    public void setPeerId(int peerId) {
        this.peerId = peerId;
    }

    public byte[] getMessageBytes() {
        byte[] messageBytes = new byte[messageSize];

        // Add the 18-byte message header
        byte[] headerBytes = header.getBytes();
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
