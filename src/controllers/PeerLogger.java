package controllers;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;

public class PeerLogger {

    private BufferedWriter writer = null;

    //creates log file ~/project/log_peer_[peerID].log
    public PeerLogger(int peerID) {
        String filename = "log_peer_" + peerID + ".log";
        try {
            writer = new BufferedWriter(new FileWriter(filename));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    //writes message to log file (buffer)
    public void writeLog(String message) {
        LocalDateTime now = LocalDateTime.now();
        try {
            writer.write(now + ": " + message);
        } catch (IOException e) {
            System.out.println("Error writing log");
        }
    }

    //writes buffered messages to file
    public void flushBuffer() {
        try {
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    //closes the file
    public void closeFile() {
        try {
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
