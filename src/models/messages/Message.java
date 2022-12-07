package models.messages;

import exceptions.InvalidMessageException;

import java.nio.ByteBuffer;
import java.util.BitSet;

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

    // Construct a message with the given type and no payload
    public Message(MessageType type) throws InvalidMessageException {
        this.payload = new byte[0];

        validateMessage(type, this.payload);

        this.type = (byte) type.ordinal();
    }

    // Construct a message from the give bytes
    public Message(byte[] bytes) throws InvalidMessageException {
        // Get the message type field
        byte messageTypeByte = bytes[4];
        if(messageTypeByte >= MessageType.values().length || messageTypeByte < 0) {
            throw new InvalidMessageException("Invalid message type provided: " + messageTypeByte);
        }
        this.type = messageTypeByte;

        // Initialize the payload byte array
        int payloadLength = ByteBuffer.wrap(bytes, 0, 4).getInt();
        this.payload = new byte[payloadLength];
        for(int i = 0; i < payloadLength; i++) {
            this.payload[i] = bytes[5 + i];
        }
    }

    // Make a bitfield message out of the given bitfield stored in a BitSet
    public static Message makeBitfieldMessage(BitSet bitfield) throws InvalidMessageException {
        byte[] payload = new byte[(bitfield.size() + 7) / 8];
        for(int i = 0; i < payload.length; i++) {
            byte b = 0;
            for(int j = 0; j < 8; j++) {
                b |= bitfield.get(i*8 + j) ? 1 : 0;
                if(j == 7) {
                    break;
                }
                b <<= 1;
            }
            payload[i] = b;
        }

        return new Message(MessageType.BITFIELD, payload);
    }

    public BitSet getBitfield() {
        // Create a BitSet object to represent the payload which should be another bitfield
        BitSet bitfield = new BitSet();
        for (int i = 0; i < this.payload.length * 8; i++) {
            if ((this.payload[i / 8] & (1 << (7 - i % 8))) > 0) {
                bitfield.set(i);
            }
        }
        return bitfield;
    }

    // Get the enum type of the message
    public MessageType getType() {
        return MessageType.values()[type];
    }

    // Get the byte representation of the message type
    public byte getTypeBytes() {
        return type;
    }

    // Return the payload as a byte array
    // If they payload is empty (null), return an empty byte array
    public byte[] getPayloadBytes() {
        return this.payload;
    }

    // Validates the message that can be created with the given inputs.
    // Throws an InvalidMessage exception if the arguments do not make a valid exception
    public static void validateMessage(MessageType type, byte[] payload) throws InvalidMessageException {
        // Validate that the message type has the correct payload size
        switch(type) {
            case CHOKE:
            case UNCHOKE:
            case INTERESTED:
            case NOT_INTERESTED: {
                if (payload.length == 0) {
                    throw new InvalidMessageException(type.toString() + " messages require an empty payload");
                }
                break;
            }
            case HAVE:
            case REQUEST: {
                if(payload.length != 4) {
                    throw new InvalidMessageException(type.toString() + " messages require a payload of 4 bytes");
                }
                break;
            }
            // case PIECE:
//            case BITFIELD: {
//                if(payload.length != 2) {
//                    throw new InvalidMessageException(type.toString() + " messages require a payload of 2 bytes");
//                }
//            }
        }
    }

    // Return the message represented as a byte array
    public byte[] getMessageBytes() {
        // Messages consist of:
        // 4 byte length
        // 1 byte type

        // Get the length of the payload and initialize the byte array
        int payloadLength = this.payload.length;
        byte[] messageBytes = new byte[4 + 1 + payloadLength];

        // Add the 4-byte payload length
        byte[] payloadLengthBytes = ByteBuffer.allocate(4).putInt(payloadLength).array();

        for(int i = 0; i < 4; i++) {
            messageBytes[i] = payloadLengthBytes[i];
        }

        // Add the 1-byte message type
        messageBytes[4] = this.type;

        // Add the variable byte payload (can be nothing)
        for(int i = 5; i < 5 + payloadLength; i++) {
            messageBytes[i] = this.payload[i - 5];
        }

        return messageBytes;
    }
}
