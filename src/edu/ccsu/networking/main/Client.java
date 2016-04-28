package edu.ccsu.networking.main;

import edu.ccsu.networking.udp.*;
import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import java.util.Arrays;
/**
 *
 * @author Deepankar Malhan, Edmir Alagic, Ben Downs
 */
public class Client implements CanReceiveMessage {
    SenderUDP sender;
    ReceiverUDP receiver;
    byte expectedMessage;

    private int targetPortNum;
    private int portNum;
    private InetAddress targetIP;
    private Thread receiverThread;
    
    public Client() {
        expectedMessage = (byte)200;
        //receiver = new ReceiverUDP();
    }

    public void startSenderUDP(String targetIP, String targetPort, String clientPort) throws Exception{
        sender = new SenderUDP();
        try {
            this.targetIP = Inet4Address.getByName(targetIP);
        }
        catch(Exception e){
            System.out.println("CLIENT:: ERROR: Failed to convert Target IP address.");
        }

        this.targetPortNum = Integer.parseInt(targetPort);
        this.portNum = Integer.parseInt(clientPort);

        sender.setTargetIP(this.targetIP);
        sender.setTargetPort(this.targetPortNum);
        sender.setPortNum(this.portNum);
        sender.startSender();
        System.out.println("CLIENT:: INFO: Started an instance of sender.");
    }

    public void startReceiverUDP(String port){
        receiver = new ReceiverUDP(this);
        receiver.setPortNum(Integer.getInteger(port));
        receiverThread = new Thread(receiver);
        System.out.println("CLIENT:: INFO: Started an instance of receiver.");
    }

    public void setSenderSlow(boolean slow){
        sender.setSlowMode(slow);
    }
    
    public void informAndUpdate(String dataToSend) throws Exception {
        //dataByteArray = 200#hostName(20 bytes- padded with spaces if < 20)#data$
        byte[] dataByteArray = new byte[dataToSend.getBytes().length+1];
        //Inform and Update message code == 200
        dataByteArray[0] = (byte)1;
        //Fill in the host name
        System.arraycopy(dataToSend.getBytes(),0,dataByteArray,1,dataToSend.getBytes().length);
        //Send the packet to the Transportation layer
        sender.rdtSend(dataByteArray);
    }

    public void clientSearchReq(String key) throws Exception{
        byte[] dataByteArray = new byte[key.length()+1];
        dataByteArray[0] = (byte)2;
        System.arraycopy(key.getBytes(Charset.forName("UTF-8")), 0, dataByteArray, 1, key.getBytes(Charset.forName("UTF-8")).length);
        sender.rdtSend(dataByteArray);
    }

    public void clientDownloadReq(String fileReq) throws Exception{
        byte[] dataByteArray = new byte[fileReq.length()+1];
        dataByteArray[0] = (byte)3;
        System.arraycopy(fileReq.getBytes(Charset.forName("UTF-8")), 0, dataByteArray, 1, fileReq.getBytes(Charset.forName("UTF-8")).length);
        sender.rdtSend(dataByteArray);
    }

    public void clientExitReq() throws Exception{
        byte[] dataByteArray = new byte[1];
        dataByteArray[0] = (byte)4;
        sender.rdtSend(dataByteArray);
    }
    
    private boolean isExpectedMessage(byte[] dataReceived){
        return (dataReceived[0] == expectedMessage);
    }

    public void filterMessage(int method, String data, String ip, String port) {

    }
}
