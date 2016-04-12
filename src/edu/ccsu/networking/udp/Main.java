package edu.ccsu.networking.udp;

import java.util.Scanner;
import java.nio.charset.Charset;

/**
 * The main method takes in a <b>String</b> from the user and
 * send it through the <b>SenderUDP</b> to another host
 * which is running <b>ReceiverUDP</b>. Sender class breaks
 * down the String into packets of specific lengths, and 
 * guarantees reliable delivery through the use of
 * Timeouts, Sequence #s, and ACKs.
 * 
 * @author Deepankar Malhan, Edmir Alagic, Ben Downs
 */

public class Main {

    public static void main(String[] args){

          ReceiverUDP receiverThread;

        try{
             //Start receiver
            receiverThread = new ReceiverUDP(51000);
            receiverThread.start();

            // Create sender
            //  Works best with static IP
            //  Dynamic IP changes when reconnecting to network (packet loss test)
            byte[] targetAddress = {(byte)127,(byte)0,(byte)0,(byte)1};
            SenderUDP sender = new SenderUDP(51001);
            sender.startSender(targetAddress, 51000);

            Scanner scan = new Scanner(System.in);
            System.out.println("Please enter a message you want to send: ");
            String data = scan.nextLine();

            // Send the data
            sender.rdtSend(data.getBytes(Charset.forName("UTF-8")));

            // Sleeping simply for demo visualization purposes
            Thread.sleep(10000);

        }catch(Exception e){
            e.printStackTrace();
        }
    }

}
