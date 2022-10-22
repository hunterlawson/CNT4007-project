package controllers;

import java.io.*;
import java.net.*;
import java.util.*;
import java.lang.*;

public class Client {
    public void main() throws Exception {

        //sample values to test functionality

        int peerID = 1000;
        String hostName = "";
        int listeningPort = 6789;
        boolean hasFile = false;
        int numOfNeighbors = 0;
        String sentence;
        String modifiedSentence;

        //reads in the user input
        BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));

        //creates a socket for the client
        Socket clientSocket = new Socket(hostName, listeningPort);

        //outputs the data from the client to the server and reads in the new data
        DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
        BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));


        sentence = inFromUser.readLine();
        outToServer.writeBytes(sentence + '\n');
        modifiedSentence = inFromServer.readLine();

        //prints out the new data
        System.out.println("FROM SERVER: " + modifiedSentence);

        //closes the client socket
        clientSocket.close();
    }

}