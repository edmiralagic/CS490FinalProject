package edu.ccsu.networking.main;

import edu.ccsu.networking.udp.ReceiverUDP;
import edu.ccsu.networking.udp.SenderUDP;

import javax.swing.table.DefaultTableModel;
import java.net.InetAddress;

/**
 * Created by edmiralagic on 4/27/16.
 */
public class Server{
    private SenderUDP sender;
    private ReceiverUDP receiver;
    
    private byte expectedMessage;
    private int targetPortNum;
    private int serverPortNum;
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
            this.targetIP = InetAddress.getByName(targetIP);
        }
        catch(Exception e){
            System.out.println("CLIENT:: ERROR: Failed to convert Target IP address.");
        }
           //Why not have int arguments for port numbers?
        this.targetPortNum = Integer.parseInt(targetPort);
        this.serverPortNum = Integer.parseInt(clientPort);

        sender.setTargetIP(this.targetIP); //Why do we need this line, if the if statement is already setting the value?
        sender.setTargetPort(this.targetPortNum); //Same thing as above?
        sender.setPortNum(this.serverPortNum); //Same thing as above?
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
            //How is this not an infinite loop b/w setTargetPortNum and startSenderUDP?
            startSenderUDP(targetIP.getHostAddress(), Integer.toString(targetPortNum), Integer.toString(serverPortNum));
        }
        catch(Exception e){
            System.out.println("SERVER:: ERROR: Could not start sender instance.");
        }
    }

    public void startReceiverUDP(String port){
        receiver = new ReceiverUDP();
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
        //We need to do a try catch statement to check if update failed, i.e., server ERROR 400 msg sent back.
    }
}
