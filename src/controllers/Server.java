package controllers;

import java.io.*;
import java.net.*;
import java.util.*;
import java.lang.*;
import java.util.logging.Handler;

class Server implements Runnable
{
    //add in our test values to temporarily work with function
    private static int peerID = 1000;
    private static String hostName = "";
    private static int listeningPort = 6789;
    private static boolean hasFile = false;
    private static int numOfNeighbors = 0;
    private static ServerSocket currServer;

    public static void main(String argv[]) throws Exception
    {
        //makes the new socket listen at the port

        ServerSocket listeningSocket = new ServerSocket(listeningPort);

        numOfNeighbors++;
        //while the port is active the following will execute

        while(true)
        {
            currServer = listeningSocket;
            Socket connectionSocket = currServer.accept();



            //if the host has a file perform the following

            if(hasFile)
            {

                //reads in the data from an input stream and sends the new data to the client

                BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));

                DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());

            }
        }
    }

    @Override
    //runs the runnable, prints out a successful return statement
    public void run()
    {
        System.out.println(peerID + "has been successfully connected!");
    }

    //closes the new server when it is done being used
    public void endConnection() throws IOException {
        currServer.close();
    }
}