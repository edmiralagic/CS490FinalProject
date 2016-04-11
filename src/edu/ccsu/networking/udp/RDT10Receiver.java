package edu.ccsu.networking.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Arrays;

/**
 * Simple receiver thread that starts up and endlessly listens for packets on
 * the specified and delivers them. Recall this simple implementation does not
 * handle loss or corrupted packets.
 *
 * @author Chad Williams
 */
public class RDT10Receiver extends Thread {

    private int port;
    private DatagramSocket receivingSocket = null;
    private String dataString = "";
    private int currentSeq = 0;

    public RDT10Receiver(String name, int port) {
        super(name);
        this.port = port;
    }

    public void stopListening() {
        if (receivingSocket != null) {
            receivingSocket.close();
            System.out.println("RECEIVER... Stopping receiver...");
        }
    }

    public void deliverData(byte[] data) {
        if(new String(data).equalsIgnoreCase("eof")){
            System.out.println("\n\nFinal result: '" + dataString + "'");
            //System.exit(0);
            stopListening();
        }
        else {
            System.out.println("RECEIVER... delivered packet with: '" + new String(data) + "'");
            dataString += new String(data);
        }
    }

    /**
     *  Checks if the packet received
     *  has the correct sequence number
     *  returns a boolean
     */
    public boolean checkPacketSeq(DatagramPacket packet){
        byte[] packetData = packet.getData();
        int seq = (int)packetData[1];
        if(seq == currentSeq){
            System.out.println("RECEIVER... Packet received has the correct seq number!");
            return true;
        }
        System.out.println("RECEIVER... Packet received has incorrect seq number, waiting for the right one.");
        return false;
    }


    /**
     * Start the thread to begin listening
     */
    public void run() {
        try {
            receivingSocket = new DatagramSocket(port);
            System.out.println("RECEIVER.. Socket created with port " + port);
            while(true) {
                System.out.println("RECEIVER... waiting for packet");
                byte[] buf = new byte[128];
                // receive request
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                receivingSocket.receive(packet);
                int packetSize = Math.abs((int)packet.getData()[0]);

                if(checkPacketSeq(packet)){
                    System.out.println("RECEIVER... Received a packet with length: " + packetSize + " bytes.");
                    byte[] packetData = Arrays.copyOfRange(packet.getData(),2,packetSize);
                    deliverData(packetData);
                    byte[] seq =  {(byte)currentSeq};
                    DatagramPacket ack = new DatagramPacket(seq, seq.length, packet.getAddress(), packet.getPort());
                    receivingSocket.send(ack);
                    System.out.println("RECEIVER... Sending Ack " + currentSeq + " to IP address " + packet.getAddress() + " and port number " + packet.getPort());
                    currentSeq = currentSeq ^ 1;
                }
                else{
                    byte[] seq =  {(byte)currentSeq};
                    DatagramPacket ack = new DatagramPacket(seq, seq.length, packet.getAddress(), packet.getPort());
                    receivingSocket.send(ack);
                    System.out.print("RECEIVER... Sending Ack " + currentSeq + " to IP address " + packet.getAddress());
                    System.out.print(" and port number " + packet.getPort() + "\n");
                }
            }
        } catch (Exception e) {
            //stopListening();
        }
    }
}
