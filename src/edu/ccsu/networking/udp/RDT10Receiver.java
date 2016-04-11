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
public class RDT10Receiver extends Thread implements Runnable {

    private int port;
    private DatagramSocket receivingSocket = null;
    private String dataString = "";
    private int currentSeq = 1;
    private byte[] seq =  {(byte)currentSeq};

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
            System.exit(0);
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
        if( seq == currentSeq){
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

            while(true) {
                System.out.println("RECEIVER... waiting for packet");
                byte[] buf = new byte[128];
                // receive request
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                receivingSocket.receive(packet);
                int packetSize = Math.abs((int)packet.getData()[0]);
                if(checkPacketSeq(packet)){
                    System.out.println("DATA LENGTH: " + packetSize);
                    byte[] packetData = Arrays.copyOfRange(packet.getData(), 2, packetSize);
                    deliverData(packetData);

                    if(currentSeq == 0){
                        currentSeq = 1;
                    }
                    else{
                        currentSeq = 0;
                    }
                    DatagramPacket Ack = new DatagramPacket(seq, seq.length, packet.getAddress(), packet.getPort());
                    receivingSocket.send(Ack);
                    System.out.print("RECEIVER... Sending Ack " + currentSeq + " to IP address " + packet.getAddress());
                    System.out.print(" and port number " + packet.getPort() + "\n");
                }
                else{
                    DatagramPacket Ack = new DatagramPacket(seq, seq.length, packet.getAddress(), packet.getPort());
                    receivingSocket.send(Ack);
                    System.out.print("RECEIVER... Sending Ack " + currentSeq + " to IP address " + packet.getAddress());
                    System.out.print(" and port number " + packet.getPort() + "\n");
                    if(currentSeq == 0){
                        currentSeq = 1;
                    }
                    else{
                        currentSeq = 0;
                    }
                }



            }
        } catch (Exception e) {
            stopListening();
        }
    }
}
