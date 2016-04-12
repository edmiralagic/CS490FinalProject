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
    private String dataString;
    private int currentSeq;

    public ReceiverUDP(int port) {
        receiverPort = port;
        dataString = "";
        currentSeq = 0;
    }
    
    /**
     *  The final method called to close the receiver's Datagram socket.
     */
    public void stopListening() {
        if (receivingSocket != null) {
            receivingSocket.close();
            System.out.println("RECEIVER:: STATUS: Closing the receiver socket");
        }
    }
    
    /**
     *  Delivers the data to either a String, or displays the final result and calls stopListening to stop listening
     *  for more data.
     *  @param data
     */
    public void deliverData(byte[] data) {
        /*0x24 is $ in hex. This value is being used as EOF for a bigger packet which will be delivered in
        multiple UDP datagrams.*/
        if(data[0] == 0x24){
            System.out.println("\n\nRECEIVER:: FINAL: '" + dataString + "'\n\n");
            stopListening();
        }
        else {
            System.out.println("RECEIVER:: INFO: Delivered packet with: '" + new String(data) + "'");
            dataString += new String(data);
        }
    }

    /**
     *  Checks if the packet received has the correct sequence number
     * 
     *  @return boolean
     */
    public boolean checkPacketSeq(DatagramPacket packet){
        byte[] packetData = packet.getData();
        int seq = (int)packetData[0];
        if(seq == currentSeq){
            System.out.println("RECEIVER:: INFO: Packet received has the correct seq number!");
            return true;
        }
        System.out.println("RECEIVER:: ERROR: Packet received has incorrect seq number! Expected: " + currentSeq + " Actual: " + seq);
        return false;
    }


    /**
     * Start the thread to begin listening
     */
    public void run() {
        try {
            receivingSocket = new DatagramSocket(receiverPort);
            System.out.println("RECEIVER:: INFO: Socket created with port " + receiverPort);

            while(true){
                System.out.println("RECEIVER:: STATUS: Waiting for packet");
                
                //Create a buffer of 128 as the MTU is 128 bytes for this implementation. 
                //Then receive a packet from the sender and check it's sequence #
                byte[] buf = new byte[128];
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                receivingSocket.receive(packet);
                int packetSize = packet.getLength();

                //If the received sequence number matches the currentSeq variable
                if(checkPacketSeq(packet)){
                    System.out.println("RECEIVER:: INFO: Received a packet with length: " + packetSize + " bytes.");
                    
                    //Extract data from the packet and deliver it. (ignoring first index which is sequence num)
                    byte[] packetData = Arrays.copyOfRange(packet.getData(),1,packetSize);
                    deliverData(packetData);
                    
                    //Create a packet with the ACK, and send it back to the sender.
                    byte[] seq =  {(byte)currentSeq};
                    DatagramPacket ack = new DatagramPacket(seq, seq.length, packet.getAddress(), packet.getPort());
                    receivingSocket.send(ack);
                    
                    System.out.println("RECEIVER:: STATUS: Sending Ack " + currentSeq + " to IP address " + packet.getAddress() + " and port number " + packet.getPort());
                    
                    //Change the sequence # from 0 to 1 or vice versa because the correct packet was delivered to receiver.
                    currentSeq = (currentSeq ^ 1);
                }
                else{
                    //Not changing the expected seq #, this code creates a ACK of opposite # than expected #, and 
                    //sends it to the sender.
                    byte[] seq =  {(byte)(currentSeq ^ 1)};
                    DatagramPacket ack = new DatagramPacket(seq, seq.length, packet.getAddress(), packet.getPort());
                    receivingSocket.send(ack);
                    System.out.print("RECEIVER:: STATUS: Sending Ack " + (currentSeq^1) + " to IP address " + packet.getAddress() + " and port number " + packet.getPort());
                }
            }
        }
        catch (Exception e) {
            System.out.println("RECEIVER:: ERROR: Exception occured at the receiver side!");
        }
    }
}