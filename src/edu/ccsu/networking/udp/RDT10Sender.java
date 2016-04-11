package edu.ccsu.networking.udp;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.*;
import java.util.Arrays;

/**
 * Simple sender, takes passed data breaks it into packets and sends them
 * to the receiver
 * @author Chad Williams
 */
public class RDT10Sender extends Thread {

    private int receiverPortNumber = 0;
    private int portNumber = 0;
    private DatagramSocket socket = null;
    private InetAddress internetAddress = null;
    private String eof = "EOF";
    private int currentSeq = 0;
    private boolean receivedAck = false;

    public RDT10Sender(int portNumber) {
        this.portNumber = portNumber;
    }

    public void startSender(byte[] targetAddress, int receiverPortNumber) throws SocketException, UnknownHostException {
        socket = new DatagramSocket(portNumber);
        internetAddress = InetAddress.getByAddress(targetAddress);
        this.receiverPortNumber = receiverPortNumber;
    }
    
    public void stopSender(){
        if (socket!=null){
            System.out.println("SENDER... Done sending!");
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
        System.out.println("SENDER... Recieved Ack is " + testSeq + ", expected ack is " + currentSeq);
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
        byte[] packetDataEOF = eof.getBytes();
        DatagramPacket eof = makePacket(packetDataEOF);
        System.out.print("SENDER... Sending EOF to IP address " + internetAddress);
        System.out.print(" and port number " + receiverPortNumber + "\n");
        socket.send(eof);
    }

    /**
     *  Method makePacket makes a packet
     *  containing the length of the packet
     *  as the first index and the sequence number
     *  as the second index and
     *  the data from the byte stream
     *  following after (max total size is 128)
     */
    public DatagramPacket makePacket(byte[] data){
        byte[] packetData = new byte[(data.length + 2)];
        packetData[1] = (byte)currentSeq;
        packetData[0] = (byte)packetData.length;
        for(int i = 2; i < packetData.length; i++){
            packetData[i] = data[i-2];
        }
        System.out.println("SENDER.. Making a packet with packet size " + packetData.length + " bytes and with seq # " + currentSeq);
        DatagramPacket packet = new DatagramPacket(packetData, packetData.length, internetAddress, receiverPortNumber);
        return packet;
    }

    public void sendPacket(DatagramPacket packet) throws SocketException, IOException, InterruptedException{
       while(!receivedAck) {
           System.out.println("SENDER... Sending packet '" + new String(packet.getData()) + "' to IP address " + internetAddress + " and port number " + receiverPortNumber);
           socket.send(packet);
           long tStart = System.currentTimeMillis();
           receiveAck(packet);
           long tEnd = System.currentTimeMillis();
           long rtt = tEnd - tStart;
           System.out.println("SENDER... RTT calculated: " + rtt + "ms" + "\n\n");
       }
        receivedAck = false;
    }
    
    /**
     * Receive data and pass it to the current state
     *
     * @param data
     */
    public void rdtSend(byte[] data) throws SocketException, IOException, InterruptedException {

        ByteArrayInputStream byteStream = new ByteArrayInputStream(data);
        boolean sent = true;

        while (byteStream.available()>0){
            byte[] packetData = new byte[126];
            int bytesRead = byteStream.read(packetData);
            //THIS DIRECTLY ABOVE AND BELOW MUST STAY, VERY IMPORTANT
            if (bytesRead<packetData.length){
                packetData = Arrays.copyOf(packetData, bytesRead);
            }
//            while(sent) {
//                sent = false;
//                try {
                    DatagramPacket packet = makePacket(packetData);
                    sendPacket(packet);
//                    socket.setSoTimeout(2000);
//                } catch (SocketException e) {
//                    sent = true;
//                }
//            }
            // Minor pause for easier visualization only
            Thread.sleep(1200);
        }

        sendEOFPacket();
        stopSender();
    }
}
