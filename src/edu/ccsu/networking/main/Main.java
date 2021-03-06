package edu.ccsu.networking.main;

import com.sun.corba.se.spi.activation.Server;
import edu.ccsu.networking.gui.ClientGUI;
import edu.ccsu.networking.gui.MainGUI;
import edu.ccsu.networking.gui.ServerGUI;
import edu.ccsu.networking.udp.ReceiverUDP;
import edu.ccsu.networking.udp.SenderUDP;
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

          //ReceiverUDP receiverThread;

        try{
             //Start receiver

               //receiverThread = new ReceiverUDP(51000);
              // receiverThread.start();

             //Create sender
              //Works best with static IP
              //Dynamic IP changes when reconnecting to network (packet loss test)
//            receiverThread = new ReceiverUDP(51000);
////            receiverThread.start();
//            ServerGUI server = new ServerGUI();

            // Create sender
//            byte[] targetAddress = {(byte)127,(byte)0,(byte)0,(byte)1};
//            SenderUDP sender = new SenderUDP(52000);
//            sender.startSender(targetAddress, 51000);
            
//            Scanner scan = new Scanner(System.in);
//            System.out.print("MAIN:: INFO: Please enter the string you want to send: ");
//            sender .rdtSend(scan.nextLine().getBytes());
                MainGUI mainGui = new MainGUI();

//            ClientGUI sender = new ClientGUI();

            // Sleeping simply for demo visualization purposes
            //Thread.sleep(10000);

        }catch(Exception e){
            e.printStackTrace();
        }
    }

}
