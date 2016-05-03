package edu.ccsu.networking.main;

import edu.ccsu.networking.gui.ServerGUI;
import edu.ccsu.networking.udp.ReceiverUDP;
import edu.ccsu.networking.udp.SenderUDP;

import javax.swing.table.DefaultTableModel;
import java.net.InetAddress;
import java.util.ArrayList;

/**
 * Created by edmiralagic on 4/27/16.
 */
public class Server implements CanReceiveMessage {
    private SenderUDP sender;
    private ReceiverUDP receiver;

    private Thread receiverThread;
    String[] columns = {"File Name", "File Size", "Host IP", "Host Port"};
    String[][] directoryData = {};
    DefaultTableModel directory = new DefaultTableModel(directoryData, columns);
    DefaultTableModel temp = new DefaultTableModel(directoryData, columns);
    ServerGUI gui;
    ArrayList<String> clients = new ArrayList<String>();

    public Server(ServerGUI gui) {
        System.out.println("SERVER:: INFO: Server instance created");
        this.gui = gui;
    }

    public void startSenderUDP(String port) throws Exception{
        this.sender = new SenderUDP();
        this.sender.setPortNum(Integer.parseInt(port) + 1000);
        this.sender.startSender();
        System.out.println("SERVER:: INFO: Started an instance of sender.");
    }

    public void updateSender(String ip, String port){
        try {
            System.out.println("SERVER:: INFO: Attempting to update target ip to " + ip + " and the target port to " + port);
            sender.setTargetIP(InetAddress.getByName(ip));
            sender.setTargetPort(Integer.parseInt(port) - 1000);
        }
        catch(Exception e){
            System.out.println("SERVER:: ERROR: Failed to update sender info.");
        }
    }

    public void setSlowMode(boolean slow){
        receiver.setSlowMode(slow);
    }

    public void startReceiverUDP(String port){
        receiver = new ReceiverUDP(this);
        receiver.setPortNum(Integer.parseInt(port));
        receiverThread = new Thread(receiver);
        receiverThread.start();
        System.out.println("SERVER:: INFO: Started an instance of receiver.");
    }

    public void rcvInformAndUpdate(String data, String ip, String port) throws Exception{
        System.out.println("SERVER:: INFO: Looks like we got an informAndUpdate message.");
        try {
            String[] files = data.split("\\?"); //split by ? (each file)
            for (String file : files) {
                String[] temp = file.split("#");
                String[] row = {temp[0], temp[1], ip, port};
                directory.addRow(row);
                directory.fireTableDataChanged();
            }
            this.gui.updateDirectory(directory);
            addClientCount(ip, port);
            serverResponse("200");
        }
        catch(Exception e){
            serverResponse("400");
        }
    }

    public void refreshDirectory(){
        this.directory.fireTableDataChanged();
        System.out.println("SERVER:: INFO: Directory has been refreshed.");
    }

    public void addClientCount(String ip, String port){
        if(!(this.clients.contains(ip+port))){
            clients.add(ip+port);
            this.gui.updateClients(clients.size());
        }
    }

    public void rmvClientCount(String ip, String port){
        for(int i = 0; i < clients.size(); i++){
            if(this.clients.get(i).equals(ip+port)){
                this.clients.remove(i);
            }
        }
        this.gui.updateClients(clients.size());
    }

    public void updateTableModel(DefaultTableModel oldTableModel, DefaultTableModel newTableModel){
        clearTableModel(oldTableModel);
        for(int r = 0; r < newTableModel.getRowCount(); r++){
            String[] tempData = {(newTableModel.getValueAt(r,0).toString()),(newTableModel.getValueAt(r,1).toString()),(newTableModel.getValueAt(r,2).toString()),(newTableModel.getValueAt(r,3).toString())};
            oldTableModel.addRow(tempData);
        }
        oldTableModel.fireTableDataChanged();
    }

    public void clearTableModel(DefaultTableModel myTableModel){
        if (myTableModel.getRowCount() > 0) {
            for (int i = myTableModel.getRowCount() - 1; i > -1; i--) {
                myTableModel.removeRow(i);
            }
        }
    }

    public void rcvClientSearchReq(String data, String ip, String port) throws Exception{
        System.out.println("SERVER:: INFO: Looks like we got a search request.");
        String results = "";
        for(int r = 0; r < directory.getRowCount(); r++){
            if((directory.getValueAt(r, 0).toString()).toLowerCase().contains(data.toLowerCase())){
                results += directory.getValueAt(r,0).toString() + "#" + directory.getValueAt(r,1).toString() + "#" + directory.getValueAt(r,2).toString() + "#" + directory.getValueAt(r,3).toString() + "?";
            }
        }
        System.out.println("SERVER:: INFO: Here are the results (formatted): " + results);
        clientSearchResponse(results);
        results = "";
    }

    public void sendHostClientReq(String info){
        try {
            System.out.println("SERVER:: INFO: Attempting to send host TCP connection info.");
            byte[] dataByteArray = new byte[info.length() + 3];

            System.arraycopy("600".getBytes(),0,dataByteArray,0,"600".getBytes().length);
            System.arraycopy(info.getBytes(), 0, dataByteArray, 3, info.getBytes().length);
            this.sender.rdtSend(dataByteArray);
        }
        catch(Exception e){
            System.out.println("SERVER:: ERROR: Failed to send host TCP connection info.");
            serverResponse("400");
        }
    }

    public void rcvDownloadReq(String data, String ip, String port){
        try {
            System.out.println("SERVER:: INFO: Attempting to retrieve the connection info for the requested file.");
            directory.fireTableDataChanged();
            String[] row = data.split("#");
            String info = "";
            for (int r = 0; r < directory.getRowCount(); r++) {
                if (directory.getValueAt(r, 0).equals(row[0]) && directory.getValueAt(r, 1).equals(row[1])) {
                    info += directory.getValueAt(r, 0).toString() + "#" + directory.getValueAt(r, 1).toString() + "#" + directory.getValueAt(r, 2).toString() + "#" + directory.getValueAt(r, 3).toString();
                    updateSender(directory.getValueAt(r, 2).toString(),directory.getValueAt(r, 3).toString());
                    break;
                }
            }
            sendHostClientReq(info);
            Thread.sleep(5000);
            updateSender(ip,port);
            clientConnectionInfo(info);
        }
        catch(Exception e){
            System.out.println("SERVER:: ERROR: Failed to retrieve the connection info.");
            serverResponse("400");
        }
    }

    public void rcvExitReq(String data, String ip, String port){
        try {
            System.out.println("SERVER:: INFO: Attempting to delete user from directory.");
            directory.fireTableDataChanged();
            int rows = directory.getRowCount();
            for (int r = 0; r < rows; r++){
                if (!(directory.getValueAt(r,2).toString()).equalsIgnoreCase(ip) && !(directory.getValueAt(r,3).toString()).equalsIgnoreCase(port)){
                   String[] row = {directory.getValueAt(r,0).toString(),directory.getValueAt(r,1).toString(),directory.getValueAt(r,2).toString(),directory.getValueAt(r,3).toString()};
                    temp.addRow(row);
                }
            }
            this.updateTableModel(directory, temp);
            this.gui.updateDirectory(directory);
            rmvClientCount(ip,port);
            exitResponse();
        }
        catch(Exception e){
            System.out.println("SERVER:: ERROR: Failed to delete user from directory.");
            serverResponse("400");
        }
    }

    public void clientSearchResponse(String results){
        try {
            System.out.println("SERVER:: INFO: Attempting to send search response.");
            byte[] dataByteArray = new byte[results.length() + 3];

            System.arraycopy("300".getBytes(),0,dataByteArray,0,"300".getBytes().length);
            System.arraycopy(results.getBytes(), 0, dataByteArray, 3, results.getBytes().length);
            this.sender.rdtSend(dataByteArray);
        }
        catch(Exception e){
            System.out.println("SERVER:: ERROR: Failed to send client search response.");
            serverResponse("400");
        }
    }

    public void serverResponse(String method){
        try {
            System.out.println("SERVER:: INFO: Attempting to send informAndUpdate response.");
            String server = "Server Error";
            if (method.equalsIgnoreCase("200")) {
                server = "Server OK";
            }

            byte[] dataByteArray = new byte[server.length() + 3];
            System.arraycopy(method.getBytes(),0,dataByteArray,0,method.getBytes().length);
            System.arraycopy(server.getBytes(),0,dataByteArray,3,server.getBytes().length);

            this.sender.rdtSend(dataByteArray);
        }
        catch(Exception e){
            System.out.println("SERVER:: ERROR: Failed to send informAndUpdate response.");
        }
    }

    public void clientConnectionInfo(String info){
        try {
            System.out.println("SERVER:: INFO: Attempting to send connection info.");
            byte[] dataByteArray = new byte[info.length() + 3];

            System.arraycopy("500".getBytes(),0,dataByteArray,0,"500".getBytes().length);
            System.arraycopy(info.getBytes(), 0, dataByteArray, 3, info.getBytes().length);
            this.sender.rdtSend(dataByteArray);
        }
        catch(Exception e){
            System.out.println("SERVER:: ERROR: Failed to send client connection info.");
            serverResponse("400");
        }
    }

    public void exitResponse(){
        try {
            System.out.println("SERVER:: INFO: Attempting to exit OK.");
            String exit = "Exit OK";
            byte[] dataByteArray = new byte[exit.length() + 3];

            System.arraycopy("100".getBytes(),0,dataByteArray,0,"100".getBytes().length);
            System.arraycopy(exit.getBytes(), 0, dataByteArray, 3, exit.getBytes().length);
            this.sender.rdtSend(dataByteArray);
        }
        catch(Exception e){
            System.out.println("SERVER:: ERROR: Failed to send exit OK.");
            serverResponse("400");
        }
    }


    @Override
    public void filterMessage(String method, String data, String ip, String port) throws Exception {
        this.updateSender(ip,port); //update the sender info so that we send to the address that we just got a message from
        switch(method) {
            case "001":
                rcvInformAndUpdate(data,ip,port);
                break;
            case "002":
                rcvClientSearchReq(data,ip,port);
                break;
            case "003":
                rcvDownloadReq(data,ip,port);
                break;
            case "004":
                rcvExitReq(data,ip,port);
                break;
        }
    }
}
