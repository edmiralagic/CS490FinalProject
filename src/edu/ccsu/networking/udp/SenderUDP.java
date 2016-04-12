package edu.ccsu.networking.udp;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.*;
import java.util.Arrays;


public class SenderUDP extends Thread {

    private final byte EOF = 0x24;
    private int receiverPortNumber;
    private int senderPortNumber;
    private DatagramSocket socket;
    private InetAddress targetAddress;
    private int currentSeq;
    private boolean receivedAck;

    public SenderUDP(int portNumber) {
        this.senderPortNumber = portNumber;
        receiverPortNumber = 0;
        socket = null;
        targetAddress = null;
        currentSeq = 0;
        receivedAck = false;
    }

    /**
     *  Creates a new socket with the specified port number for the sender.
     *  The IP address and receiver port number are for the target client.
     * 
     * @param targetAddress
     * @param receiverPortNumber
     * @throws java.net.SocketException
     * @throws java.net.UnknownHostException
     */
    public void startSender(byte[] targetAddress, int receiverPortNumber) throws SocketException, UnknownHostException {
        try{
        socket = new DatagramSocket(senderPortNumber);
        }
        catch(SocketException se) {
            System.out.println("SENDER:: ERROR: Sending socket was not opened.");
        }
        this.targetAddress = InetAddress.getByAddress(targetAddress);
        this.receiverPortNumber = receiverPortNumber;
    }

    /**
     *  Method checkAck compares
     *  Closes the socket if it is
     *  open
     */
    public void stopSender(){
        if (socket!=null){
            System.out.println("SENDER... Closing the sender socket!");
            socket.close();
        }
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
        System.out.println("SENDER... Received Ack is " + testSeq + ", expected ack is " + currentSeq);
        if(testSeq == currentSeq){
            currentSeq = currentSeq ^ 1;
            return true;
        }
        return false;
    }

    /**
     *  Method receiveAck waits for
     *  a packet to be sent from the
     *  receiver with a sequence number
     *  and then uses checkAck method
     */
    public void receiveAck(DatagramPacket packet) throws SocketException, IOException, InterruptedException{
        System.out.println("SENDER... Waiting for Ack from Receiver");
        while(!receivedAck){
            try {
                byte[] buf = new byte[16];
                DatagramPacket ack = new DatagramPacket(buf, buf.length);
                socket.receive(ack);
                System.out.println("SENDER... Recieved ACK with Sequence Number: " + (int)ack.getData()[0]);
                if(checkAck(ack)){
                    System.out.println("SENDER... Received Ack checks out!");
                    receivedAck = true;
                }
                else {
                    receivedAck = false;
                    System.out.println("SENDER... Resending the packet!");
                    break;
                }
            }
            catch(NullPointerException e){
                System.out.println("SENDER... NULL POINTER EXCEPTION from received ack.");
                receivedAck = false;
            }
        }
    }

    /**
     *  Method sendEOFPacket makes a packet
     *  containing the end of file string
     */
    public void sendEOFPacket() throws SocketException, IOException, InterruptedException{
        byte[] packetDataEOF = {EOF};
        DatagramPacket eof = makePacket(packetDataEOF);
        System.out.print("SENDER:: INFO: Sending EOF to IP address: " + targetAddress);
        System.out.print(" and port number " + receiverPortNumber + "\n");
        socket.send(eof);
    }

    /**
     *  Method makePacket makes a packet containing the length of the packet
     *  as the first index, the sequence number as the second index, and
     *  the data from the byte stream following after (max total size is 128)
     *  @param data in the form of a byte array
     */
    public DatagramPacket makePacket(byte[] data){
        byte[] packetData = new byte[(data.length + 2)];
        //packetData[0] = (byte)packetData.length;
        packetData[0] = (byte)currentSeq;
        System.arraycopy(data, 0, packetData, 1, data.length);
 
        System.out.println("SENDER:: INFO: Making a packet with packet size " + packetData.length + " bytes and with seq # " + currentSeq);
        DatagramPacket packet = new DatagramPacket(packetData, packetData.length, targetAddress, receiverPortNumber);
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
           System.out.println("SENDER... Sending packet '" + new String(packet.getData()) + "' to IP address " + targetAddress + " and port number " + receiverPortNumber);
           socket.send(packet);
           long tStart = System.currentTimeMillis();
           receiveAck(packet);
           long tEnd = System.currentTimeMillis();
           long rtt = tEnd - tStart;
           System.out.println("SENDER... RTT calculated: " + rtt + "ms");
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
        byte[] packetData = new byte[126];
        int bytesRead = byteStream.read(packetData);
        //THIS DIRECTLY ABOVE AND BELOW MUST STAY, VERY IMPORTANT
        if (bytesRead<packetData.length){
            packetData = Arrays.copyOf(packetData, bytesRead);
        }
        return packetData;
    }
    
    /**
     * Receive data and pass it to the current state
     *
     * @param data
     */
    public void rdtSend(byte[] data) throws SocketException, IOException, InterruptedException {

        ByteArrayInputStream byteStream = new ByteArrayInputStream(data);
        boolean sent = false;

        while (byteStream.available()>0){
            byte[] packetData = makePacketData(byteStream);

            while(!sent) {
                try {
                    DatagramPacket packet = makePacket(packetData);
                    sendPacket(packet);
                    socket.setSoTimeout(1);
                    sent = true;
                    System.out.println("SENDER... Received ack before the timeout, nice!");
                } catch (SocketException e) {
                    sent = false;
                    System.out.println("SENDER... Socket timed out, sending packet again.");
                }
            }
            sent = false;


            Thread.sleep(1200);
        }

        sendEOFPacket();
        stopSender();
    }
}
