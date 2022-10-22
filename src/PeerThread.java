import controllers.Peer;
import exceptions.InvalidMessage;
import models.messages.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

//NOT COMPLETE
//some methods/functionality should be in peerProcess, others in peerThread

public class PeerThread {
    boolean choked = false;

    int k = 0;
    int m = 0;
    int p = 0;

    PeerLogger logger;

    //this peer's ID
    int peerID1;

    //the connected peer's ID
    int peerID2;

    boolean interested = false;

    ArrayList<Peer> neighbors;

    public void sendMessage(Message message) {

    }

    //determine which piece to request (random)
    public byte[] createRequest() {
        byte[] indexField = null;

        return indexField;
    }

    //returns true if file is completely downloaded
    public boolean isComplete() {
        return false;
    }

    public double getDownloadRate(Peer peer) {
        double rate = 0;

        return rate;
    }

    //determine if peer has interesting piece
    public boolean isInterested() {
        return interested;
    }

    //call after p seconds
    //reselect preferred neighbors
    public void setPreferredNeighbors() throws InvalidMessage {
        if (!isComplete()) {
            //calculate download rate of each

            //pick k neighbors w/ highest download rate
            Collections.sort(neighbors);
            for (int i=0; i<k; i++) {
                //if neighbor is choked, send unchoke message
                if (neighbors.get(i).isChoked()) {
                    //send unchoke message to that neighbor
                    Message outMessage = new Message(Message.MessageType.UNCHOKE, null);
                    //send request message
                    sendMessage(outMessage);
                }
            }

            //send choke messages to the rest of the neighbors (that aren't already)
            for (int i=k; k<neighbors.size(); i++) {
                if (!neighbors.get(i).isChoked()) {
                    Message outMessage = new Message(Message.MessageType.CHOKE, null);
                    //send request message
                    sendMessage(outMessage);
                }
            }
        }
        else {
            //choose preferred neighbors randomly among those interested
        }
    }

    //call every m seconds
    //selects choked neighbor randomly to unchoke
    public void optimisticUnchoke() throws InvalidMessage {
        //get random neighbor
        Random rand = new Random();
        int n = rand.nextInt(neighbors.size());

        //send unchoke message to that neighbor

        Message outMessage = new Message(Message.MessageType.UNCHOKE, null);
        //send request message
        sendMessage(outMessage);
    }

    //might need to be placed somewhere else
    public void readMessage(byte[] message) throws InvalidMessage {

        //read in message
        //parse message
        //get message type (1 byte) convert to int?
        int type = 0;
        switch(type) {
            //choke
            case 0: {
                //stop sending
                //set var choked (of this?) to true
                this.choked = true;
                logger.writeLog("Peer " + peerID1 + " is choked by " + peerID2 + ".");
                break;
            }
            //unchoke
            case 1: {
                //set var choked (of this?) to false
                this.choked = false;
                logger.writeLog("Peer " + peerID1 + " is unchoked by " + peerID2 + ".");
                //create payload of request message
                byte[] payload = createRequest();
                //create message
                Message outMessage = new Message(Message.MessageType.REQUEST, payload);
                //send request message
                sendMessage(outMessage);
                break;
            }
            //interested
            case 2: {
                //update var interested
                logger.writeLog("Peer " + peerID1 + " received the 'interested' message from " + peerID2 + ".");
                interested = true;
                break;
            }
            //not interested
            case 3: {
                //update var interested
                logger.writeLog("Peer " + peerID1 + " received the 'not interested' message from " + peerID2 + ".");
                interested = false;
                break;
            }
            //have
            case 4: {
                //update peer bitfield
                //determine if interested
                //else send uninterested
                logger.writeLog("Peer " + peerID1 + " received the 'have' message from " + peerID2 + " for the piece [x].");
                break;
            }
            //bitfield
            case 5: {
                //update peer bitfield
                //determine if interested
                //else send uninterested

                break;
            }
            //request
            case 6: {
                //send piece message
                break;
            }
            //piece
            case 7: {
                //send request or
                //determine if not interested
                if (isComplete() || isInterested()) {
                    Message outMessage = new Message(Message.MessageType.NOT_INTERESTED, null);
                    logger.writeLog("Peer " + peerID1 + " has downloaded the complete file.");
                    //send request message
                    sendMessage(outMessage);
                }
                else {
                    //create payload of request message
                    byte[] payload = createRequest();
                    //create message
                    Message outMessage = new Message(Message.MessageType.REQUEST, payload);
                    //send request message
                    sendMessage(outMessage);
                }

                break;
            }
        }
    }
}
