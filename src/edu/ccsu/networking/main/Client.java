package edu.ccsu.networking.main;

import edu.ccsu.networking.udp.*;
import java.io.*;
import java.net.*;
import java.util.Arrays;
/**
 *
 * @author Deepankar Malhan, Edmir Alagic, Ben Downs
 */
public class Client{   
    SenderUDP sender;
    ReceiverUDP receiver;
    byte expectedMessage;
    
    public Client() {
    expectedMessage = (byte) 200;
    sender = new SenderUDP();
    receiver = new ReceiverUDP();
    }
    
    public void informAndUpdate(String dataToSend) throws UnsupportedEncodingException, UnknownHostException, SocketException, IOException, InterruptedException {
        //dataByteArray = 200#hostName(20 bytes- padded with spaces if < 20)#data$
        byte[] dataByteArray = new byte[dataToSend.length()+24];
        String hostName = InetAddress.getLocalHost().getHostName();
        byte[] hostNameArray = new byte[20];
        //Put hostName into a temp hostNameArray that can be copied into the dataByteArray
        hostNameArray = hostName.getBytes("UTF-8");
        if(hostName.getBytes().length<20) {
            Arrays.fill(hostNameArray, hostName.getBytes().length, hostNameArray.length, (byte)0x20);
        }
        //Inform and Update message code == 200
        dataByteArray[0] = (byte)200;
        //0x23 == "#"
        dataByteArray[1] = (byte) 0x23;
        //Fill in the host name
        System.arraycopy(hostNameArray,0 , dataByteArray, 2, hostNameArray.length);
        //The # after host name
        dataByteArray[23] = (byte) 0x23;
        //Put in the data
        System.arraycopy(dataToSend.getBytes("UTF-8"), 0, dataByteArray, 24, dataToSend.getBytes("UTF-8").length);
        //Put in the EOF Symbol $ == 0x24
        dataByteArray[dataByteArray.length-1] = (byte)0x24;
        
        //Send the packet to the Transportation layer
        sender.rdtSend(dataByteArray);
    }
    
    private boolean isExpectedMessage(byte[] dataReceived){
        return (dataReceived[0] == expectedMessage);
    }
}
