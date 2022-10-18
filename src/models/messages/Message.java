package models.messages;

import exceptions.InvalidMessage;

import java.nio.ByteBuffer;

public class Message {
    public enum MessageType {
        CHOKE,
        UNCHOKE,
        INTERESTED,
        NOT_INTERESTED,
        HAVE,
        BITFIELD,
        REQUEST,
        PIECE;

    }
    byte type;
    byte[] payload;

    /*
        Construct a message with the given type and payload.
        This function also validates the created message to ensure that it follows the rules:

        CHOKE, UNCHOKE, INTERESTED, NOT_INTERESTED: No payload
     */
    public Message(MessageType type, byte[] payload) throws InvalidMessage {
        validateMessage(type, payload);

        this.type = (byte) type.ordinal();
        this.payload = payload;
    }

    public byte getType() {
        return type;
    }

    public byte[] getPayload() {
        return payload;
    }

    /*
        Validates the message that can be created with the given inputs.
        Throws an InvalidMessage exception if the arguments do not make a valid exception
     */
    public static void validateMessage(MessageType type, byte[] payload) throws InvalidMessage {
        switch(type) {
            case CHOKE:
            case UNCHOKE:
            case INTERESTED:
            case NOT_INTERESTED: {
                if (payload.length != 0) {
                    throw new InvalidMessage(type.toString() + " messages require an empty payload");
                }
                break;
            }
            case HAVE:
            case REQUEST:
            case PIECE: {
                if(payload.length != 4) {
                    throw new InvalidMessage(type.toString() + " messages require a payload of 4 bytes");
                }
                break;
            }

        }
    }

    public byte[] getMessageBytes() {
        // Messages consist of:
        // 4 byte length
        // 1 byte type
        // variable length payload
        byte[] messageBytes = new byte[4 + 1 + payload.length];

        // Add the 4-byte payload length
        byte[] payloadLengthBytes = ByteBuffer.allocate(4).putInt(payload.length).array();
        for(int i = 0; i < 4; i++) {
            messageBytes[i] = payloadLengthBytes[i];
        }

        // Add the 1-byte message type
        messageBytes[4] = type;

        // Add the variable byte payload
        for(int i = 6; i < 6 + payload.length; i++) {
            messageBytes[i] = payload[i - 6];
        }

        return messageBytes;
    }
}
