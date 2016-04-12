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
 * @author Deepankar Malhan, Edmir Alagic, Ben Downs
 */
public class ReceiverUDP extends Thread {

    private final int receiverPort;
    private DatagramSocket receivingSocket = null;
    private String dataString = "";
    private int currentSeq = 0;

    public ReceiverUDP(int port) {
        receiverPort = port;
    }
    
    /**
     * The final method called to close the receiver's Datagram socket.
     */
    public void stopListening() {
        if (receivingSocket != null) {
            receivingSocket.close();
            System.out.println("RECEIVER:: INFO: Closing the receiver socket");
        }
    }
    
    /**
     * Delivers the data to either a String, or displays the final result and calls stopListening to stop listening
     * for more data.
     * @param data 
     */
    public void deliverData(byte[] data) {
        /*0x24 is $ in hex. This value is being used as EOF for a bigger packet which will be delivered in
        multiple UDP datagrams.*/
        if(data[0] == 0x24){
            System.out.println("\n\nFinal result: '" + dataString + "'");
            stopListening();
        }
        else {
            System.out.println("RECEIVER:: SUCCESS: Delivered packet with: '" + new String(data) + "'");
            dataString += new String(data);
        }
    }

    /**
     *  Checks if the packet received has the correct sequence number
     * 
     * @return boolean
     */
    public boolean checkPacketSeq(DatagramPacket packet){
        byte[] packetData = packet.getData();
        int seq = (int)packetData[0];
        if(seq == currentSeq){
            System.out.println("RECEIVER:: INFO: Packet received has the correct seq number!");
            return true;
        }
        System.out.println("RECEIVER:: ERROR: Packet received has incorrect seq number, waiting for the right one.");
        return false;
    }


    /**
     * Start the thread to begin listening
     */
    public void run() {
        try {
            receivingSocket = new DatagramSocket(receiverPort);
            System.out.println("RECEIVER.. Socket created with port " + receiverPort);

            while(true){
                System.out.println("RECEIVER... waiting for packet");

                byte[] buf = new byte[128];
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                receivingSocket.receive(packet);
                int packetSize = packet.getLength();

                if(checkPacketSeq(packet)){
                    System.out.println("RECEIVER... Received a packet with length: " + packetSize + " bytes.");
                    byte[] packetData = Arrays.copyOfRange(packet.getData(),1,packetSize);
                    deliverData(packetData);
                    byte[] seq =  {(byte)currentSeq};
                    DatagramPacket ack = new DatagramPacket(seq, seq.length, packet.getAddress(), packet.getPort());
                    receivingSocket.send(ack);
                    System.out.println("RECEIVER... Sending Ack " + currentSeq + " to IP address " + packet.getAddress() + " and port number " + packet.getPort());
                    currentSeq = (currentSeq ^ 1);
                }
                else{
                    byte[] seq =  {(byte)(currentSeq ^ 1)};
                    DatagramPacket ack = new DatagramPacket(seq, seq.length, packet.getAddress(), packet.getPort());
                    receivingSocket.send(ack);
                    System.out.print("RECEIVER... Sending Ack " + currentSeq + " to IP address " + packet.getAddress() + " and port number " + packet.getPort());
                }
            }
        }
        catch (Exception e) {
            //stopListening();
        }
    }
}
