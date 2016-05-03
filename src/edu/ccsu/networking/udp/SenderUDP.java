package edu.ccsu.networking.udp;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.*;
import java.util.Arrays;

/**
 *  Simple sender thread that receives data from
 *  above (Main.java) in the form of a ByteStream
 *  and constructs a packet (or multiple packets
 *  depending on the data size). The sender then sends
 *  said packet(s) to the receiver and waits for
 *  Ack in response.
 *  @author Deepankar Malhan, Edmir Alagic, Ben Downs
 */

public class SenderUDP extends Thread {

    private int targetPort;
    private int senderPortNumber;
    private DatagramSocket socket;
    private InetAddress targetIP;
    private int currentSeq;
    private boolean receivedAck;
    private long timeout;
    private boolean slowMode;
    private String method;
    private int flag;

    public SenderUDP() {
        targetPort = 0;
        socket = null;
        targetIP = null;
        currentSeq = 0;
        timeout = 100; //initial timeout set to 100
        receivedAck = false;
        slowMode = false;
        method = "";
        flag = 1;
    }

    /**
     *  Creates a new socket with the specified port number for the sender.
     *  The IP address and receiver port number are for the target client.
     *
     * @throws java.net.SocketException
     * @throws java.net.UnknownHostException
     */
    public void startSender() throws SocketException, UnknownHostException {
        try{
            socket = new DatagramSocket(senderPortNumber);
            System.out.println("SENDER:: INFO: Socket opened with port number: " + senderPortNumber);
        }
        catch(SocketException se) {
            System.out.println("SENDER:: ERROR: Sender socket was not opened.");
        }
    }

    public void setTargetIP(InetAddress targetIP){
        this.targetIP = targetIP;
        System.out.println("SENDER:: INFO: Target IP address set to " + this.targetIP.toString());
    }

    public void setTargetPort(int port){
        this.targetPort = port;
        System.out.println("SENDER:: INFO: Target Port Number set to " + this.targetPort);
    }

    public void setPortNum(int port){
        this.senderPortNumber = port;
        System.out.println("SENDER:: INFO: Port number set to " + this.senderPortNumber);
    }

    public void setSlowMode(boolean slow){
        this.slowMode = slow;
        System.out.println("SENDER:: INFO: Slow Mode set to " + this.slowMode);
    }

    /**
     *  Method checkAck compares
     *  Closes the socket if it is
     *  open
     */
    public void stopSender(){
        if (socket != null){
            System.out.println("SENDER:: STATUS: Closing the sender socket!");
            socket.close();
        }
    }

    public void setSenderPort(int port){
        this.senderPortNumber = port;
    }

    /**
     *  Method checkAck compares
     *  the first index (index = 0) of the
     *  packet that was received to the
     *  currentSeq global variable and returns
     *  true if they are the same, otherwise returns
     *  false
     */
    public boolean checkAck(DatagramPacket ack){
        int testSeq = (int)ack.getData()[0];
        System.out.println("SENDER:: INFO: Received Ack is " + testSeq + ", expected ack is " + currentSeq);
        if(testSeq == currentSeq){
            System.out.println("SENDER:: INFO: Received Ack matches expected Ack!");
            currentSeq = (currentSeq ^ 1);
            return true;
        }
        System.out.println("SENDER:: ERROR: Received Ack does not match expected Ack.. Attempting to resend.");
        return false;
    }

    /**
     *  Method receiveAck waits for
     *  a packet to be sent from the
     *  receiver with a sequence number
     *  and then uses checkAck method
     */
    public void receiveAck(DatagramPacket packet) throws SocketException, IOException, InterruptedException{
        System.out.println("SENDER:: STATUS: Waiting for Ack from Receiver");
        while(!receivedAck){
            try {
                byte[] buf = new byte[16];
                DatagramPacket ack = new DatagramPacket(buf, buf.length);
                socket.receive(ack);

                System.out.println("SENDER:: INFO: Recieved ACK with Sequence Number: " + (int)ack.getData()[0]);
                System.out.println("SENDER:: INFO: Received ACK from: " + targetIP + ", port #: " + targetPort);
                
                if(checkAck(ack)){
                    receivedAck = true;
                }
                else {
                    receivedAck = false;
                    break;
                }
            }
            catch(NullPointerException e){
                System.out.println("SENDER:: ERROR: NULL POINTER EXCEPTION from received ack.");
                receivedAck = false;
            }
        }
    }

    /**
     *  Calculates the new timeout value
     *  taking into consideration the old
     *  timeout value and the new currentRtt
     *  value
     *  I.E dynamic timeout
     *  @param currentRtt
     */
    public void adjustTimeout(long currentRtt){
        double temp = ((double)timeout * 0.8) + ((double)currentRtt * 0.2);
        timeout = (long)temp;
    }

    /**
     *  Method makePacket makes a packet containing the length of the packet
     *  as the first index, the sequence number as the second index, and
     *  the data from the byte stream following after (max total size is 128)
     *  @param data in the form of a byte array
     */
    public DatagramPacket makePacket(byte[] data){
        byte[] packetData = new byte[(data.length + 5)];
        packetData[0] = (byte)currentSeq;
        packetData[1] = (byte)flag;
        byte[] methodArray = method.getBytes();
        System.arraycopy(methodArray,0,packetData,2,methodArray.length);
        System.arraycopy(data,0,packetData,5,data.length);
        System.out.println("\n\nSENDER:: STATUS: Making a packet with packet size " + packetData.length + " bytes and with seq # " + currentSeq);
        DatagramPacket packet = new DatagramPacket(packetData, packetData.length, targetIP, targetPort);
        return packet;
    }

    /**
     *  Method sendPacket
     *  Sends the packet,
     *  starts the RTT timer,
     *  then calls the receiveAck method
     *  and once its received, ends the timer
     *  @param  packet
     */
    public void sendPacket(DatagramPacket packet) throws SocketException, IOException, InterruptedException{
       while(!receivedAck) {
           System.out.println("SENDER:: STATUS: Sending packet '" + new String(packet.getData()) + "' with packet size: " + packet.getLength() + " bytes to IP address " + targetIP + " and port number " + targetPort);
           socket.send(packet);
           long tStart = System.currentTimeMillis();
           //socket.setSoTimeout((int)timeout);
           //Thread.sleep(100);
           System.out.println("SENDER:: STATUS: Set timeout to " + timeout + " ms");
           try {
               receiveAck(packet);
               long rtt = System.currentTimeMillis() - tStart;
               //adjustTimeout(rtt);
               System.out.println("SENDER:: INFO: RTT calculated: " + rtt + "ms" /* + ", timeout adjusted to " + timeout + " ms"*/);
           }
           catch(SocketTimeoutException e){
               System.out.println("SENDER:: ERROR: Socket timed out, sending packet again.");
               receivedAck = false;
           }
       }
        receivedAck = false;
    }

    /**
     *  Receive data and turn it into an
     *  array of bytes that will be used to create
     *  a packet
     *  @param byteStream from above (data from above)
     */
    public byte[] makePacketData(ByteArrayInputStream byteStream) throws SocketException, IOException, InterruptedException{
        byte[] packetData = new byte[125];
        int bytesRead = byteStream.read(packetData);
        //THIS DIRECTLY ABOVE AND BELOW MUST STAY, VERY IMPORTANT
        if (bytesRead<packetData.length){
            flag = 0; //end of file
            packetData = Arrays.copyOf(packetData, bytesRead);
        }
        System.out.println("SENDER:: INFO: Packet data created: " + new String(packetData));
        return packetData;
    }
    
    /**
     * Receive data and pass it to the current state
     *
     * @param data
     */
    public void rdtSend(byte[] data) throws SocketException, IOException, InterruptedException {
        byte[] methodArray = {data[0], data[1], data[2]};
        this.method = new String(methodArray);
        System.out.println("SENDER:: INFO: Saved method as " + method + " .");

        byte[] newData = new byte[data.length - 3];
        try {
            System.arraycopy(data,3,newData,0,data.length - 3);
        }
        catch(Exception e){
            System.out.println(e);
            System.exit(0);
        }
        ByteArrayInputStream byteStream = new ByteArrayInputStream(newData);
        System.out.println("SENDER:: INFO: The total length of the data within this message is " + newData.length + " bytes.");

        while (byteStream.available() > 0){
            byte[] packetData = makePacketData(byteStream);
            DatagramPacket packet = makePacket(packetData);
            sendPacket(packet);
            if(slowMode){
                Thread.sleep(4000);
            }
        }

        flag = 1; //reset the flag after the whole message is sent
    }

}
