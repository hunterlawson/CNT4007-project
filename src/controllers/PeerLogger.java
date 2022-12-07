package controllers;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class PeerLogger {

//    Logger logger = Logger.getLogger("PeerLogger");
//    FileHandler fh;
    int peerID;
    private BufferedWriter writer = null;

    //creates log file ~/project/log_peer_[peerID].log
    public PeerLogger(int peerID) {
        String filename = "log_peer_" + peerID + ".log";
        try {
            writer = new BufferedWriter(new FileWriter(filename));
//            fh = new FileHandler(filename);
//            logger.addHandler(fh);
//            SimpleFormatter formatter = new SimpleFormatter();
//            fh.setFormatter(formatter);
        } catch (IOException e) {
            System.out.println("Error creating logger");
        }
    }

    //writes message to log file (buffer)
    public synchronized void writeLog(String message) {
        LocalDateTime now = LocalDateTime.now();
        try {
            writer.write(now + ": " + message + "\n");
            flushBuffer();
        } catch (IOException e) {
            System.out.println("Error writing log");
        }
    }
//    public void writeLog(String message) {
//        //LocalDateTime now = LocalDateTime.now();
//        try {
//            //writer.write(now + ": " + message);
//            logger.info(message);
//        } catch (Exception e) {
//            System.out.println("Error writing log");
//        }
//    }

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
