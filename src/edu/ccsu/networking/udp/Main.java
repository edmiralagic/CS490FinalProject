package edu.ccsu.networking.udp;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Scanner;

/**
 * These classes contain a very simple example of a UDT send and receive similar
 * to the RDT 1.0 covered in class.
 * A receiver thread is started that begins listening on 49000 that waits
 * for packets to delivers.  A sending socket is then
 * opened for sending data to the receiver.  
 * 
 * With a real sender a large message will need to be broken into packets so
 * this demo also shows one way to break a larger message up this way.
 * 
 * @author Chad Williams
 */

public class Main {

    public static void main(String[] args){

        RDT10Receiver receiverThread = null;

        try{
             //Start receiver
            receiverThread = new RDT10Receiver("Receiver", 3020);
            receiverThread.start();

            // Create sender
            byte[] targetAddress = {(byte)127,(byte)0,(byte)0,(byte)1};
            RDT10Sender sender = new RDT10Sender(2010);
            sender.startSender(targetAddress, 3020);

            Scanner scan = new Scanner(System.in);
            System.out.println("Please enter a message you want to send: ");
            String data = scan.nextLine();

                // Send the data
                sender.rdtSend(data.getBytes());

                // Sleeping simply for demo visualization purposes
                Thread.sleep(10000);  

        }catch(Exception e){
            e.printStackTrace();
        }
    }

}
