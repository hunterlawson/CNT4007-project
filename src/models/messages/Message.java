package models.messages;

import exceptions.InvalidMessageException;

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

    // Construct a message with the given type and payload and validate the message
    public Message(MessageType type, byte[] payload) throws InvalidMessageException {
        validateMessage(type, payload);

        this.type = (byte) type.ordinal();
        this.payload = payload;
    }

    // Construct a message from the give bytes
    public Message(byte[] bytes) throws InvalidMessageException {
        // Get the message type field
        byte messageTypeByte = bytes[4];
        if(messageTypeByte >= MessageType.values().length || messageTypeByte < 0) {
            throw new InvalidMessageException("Invalid message type provided: " + messageTypeByte);
        }
        this.type = messageTypeByte;

        int payloadLength = ByteBuffer.wrap(bytes, 0, 4).getInt() - 1;
        this.payload = new byte[payloadLength];
        for(int i = 0; i < payloadLength; i++) {
            this.payload[i] = bytes[5 + i];
        }
    }

    public MessageType getType() {
        return MessageType.values()[type];
    }

    public byte getTypeBytes() {
        return type;
    }

    public byte[] getPayload() {
        return payload;
    }

    /*
        Validates the message that can be created with the given inputs.
        Throws an InvalidMessage exception if the arguments do not make a valid exception
     */
    public static void validateMessage(MessageType type, byte[] payload) throws InvalidMessageException {
        // Validate that the message type has the correct payload size
        switch(type) {
            case CHOKE:
            case UNCHOKE:
            case INTERESTED:
            case NOT_INTERESTED: {
                if (payload.length != 0) {
                    throw new InvalidMessageException(type.toString() + " messages require an empty payload");
                }
                break;
            }
            case HAVE:
            case REQUEST:
            case PIECE: {
                if(payload.length != 4) {
                    throw new InvalidMessageException(type.toString() + " messages require a payload of 4 bytes");
                }
                break;
            }
            case BITFIELD: {
                if(payload.length != 2) {
                    throw new InvalidMessageException(type.toString() + " messages require a payload of 2 bytes");
                }
            }
        }
    }

    // Return the message represented as a byte array
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
