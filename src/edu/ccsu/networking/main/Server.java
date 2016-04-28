package edu.ccsu.networking.main;

import edu.ccsu.networking.udp.ReceiverUDP;
import edu.ccsu.networking.udp.SenderUDP;

import javax.swing.table.DefaultTableModel;
import java.net.Inet4Address;
import java.net.InetAddress;

/**
 * Created by edmiralagic on 4/27/16.
 */
public class Server implements CanReceiveMessage {
    SenderUDP sender;
    ReceiverUDP receiver;
    byte expectedMessage;

    private int targetPortNum;
    private int portNum;
    private InetAddress targetIP;
    private Thread receiverThread;
    String[] columns = {"File Name", "File Size", "Host IP", "Host Port"};
    DefaultTableModel directory = new DefaultTableModel(null, columns);


    public Server() {
        //expectedMessage = (byte)200;
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

    public void setTargetIP(InetAddress ip){
        this.targetIP = ip;
    }

    public void setSlowMode(boolean slow){
        receiver.setSlowMode(slow);
    }

    public void setTargetPortNum(int port){
        this.targetPortNum = port;
        try {
            startSenderUDP(targetIP.getHostAddress(), Integer.toString(targetPortNum), Integer.toString(portNum));
        }
        catch(Exception e){
            System.out.println("SERVER:: ERROR: Could not start sender instance.");
        }
    }

    public void startReceiverUDP(String port){
        receiver = new ReceiverUDP(this);
        receiver.setPortNum(Integer.parseInt(port));
        receiverThread = new Thread(receiver);
        receiverThread.start();
        System.out.println("CLIENT:: INFO: Started an instance of receiver.");
    }

    public void rcvInformAndUpdate(String data, String ip, String port){
        String[] files = data.split("\\?"); //split by ? (each file)
        for(String file : files){
            String[] temp = file.split("#");
            String[] row = {temp[0], temp[1], ip, port};
            directory.addRow(row);
            directory.fireTableDataChanged();
        }
        System.out.println("\n\n");
        for(int r = 0; r < directory.getRowCount(); r++){
            System.out.println(directory.getValueAt(r,0) + " / " + directory.getValueAt(r,1) + " / " + directory.getValueAt(r,2) + " / " + directory.getValueAt(r,3));
        }
        System.out.println("\n\n");
    }

    @Override
    public void filterMessage(int method, String data, String ip, String port) {
        if(method == 1){
            rcvInformAndUpdate(data,ip,port);
        }
    }
}
