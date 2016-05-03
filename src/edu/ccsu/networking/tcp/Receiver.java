/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ccsu.networking.tcp;

import java.io.*;
import java.net.*;
import java.util.Arrays;

/**
 *
 * @author Deepankar Malhan, Edmir Alagic, Ben Downs
 */

public class Receiver{
    public static void main(String[] args) throws IOException{

        System.out.println("RECEIVER:: Initialized!");
        Socket socket = new Socket("localhost", 7002);
        System.out.println("RECEIVER:: Socket started!");
        
        InputStream fromServer = socket.getInputStream();
        System.out.println("RECEIVER:: Input stream created from the socket!");

        System.out.println("RECEIVER:: Output stream to the server created by the receiver!");
        System.out.println("RECEIVER:: Output stream to a file created by the receiver!");

        String fileLocation = "C:\\Users\\deepa\\Documents\\test.exe";
        File file = new File(fileLocation);
        BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(file));
        int count = 0;
        
        byte[] buffer = new byte[2048];
        
        while((count = fromServer.read(buffer)) != -1) {
            buffer = Arrays.copyOf(buffer, count);
            output.write(buffer);
            System.out.println("RECEIVER:: Read " + count + " bytes from the input stream!");
        }

        System.out.println("OUT OF LOOP, GOT THE FILE.");
        
        fromServer.close();
        output.flush();
        socket.close();
        System.out.println("RECEIVER:: Closed everything!");
    }
}