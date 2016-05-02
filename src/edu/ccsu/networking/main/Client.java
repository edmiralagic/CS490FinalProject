package edu.ccsu.networking.main;

import edu.ccsu.networking.gui.ClientGUI;
import edu.ccsu.networking.udp.*;

import javax.swing.table.DefaultTableModel;
import java.net.*;

/**
 *
 * @author Deepankar Malhan, Edmir Alagic, Ben Downs
 */
public class Client implements CanReceiveMessage {
    SenderUDP sender;
    ReceiverUDP receiver;
    String expectedMessage = "";

    private int targetPortNum;
    private int portNum;
    private InetAddress targetIP;
    private Thread receiverThread;
    ClientGUI gui;
    private String[] connectionInfo;
    String[] columns = {"File Name", "File Size", "Host IP", "Host Port"};
    String[][] resultsData = {};
    DefaultTableModel searchResults = new DefaultTableModel(resultsData,columns);
    private String[] previousMessage = new String[4];
    
    public Client(ClientGUI gui) {
        System.out.println("CLIENT:: INFO: Client instance created");
        this.gui = gui;
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
        this.portNum = Integer.parseInt(clientPort) + 1000;

        sender.setTargetIP(this.targetIP);
        sender.setTargetPort(this.targetPortNum);
        sender.setPortNum(this.portNum);
        sender.startSender();
        System.out.println("CLIENT:: INFO: Started an instance of sender.");
    }

    public void startReceiverUDP(String port){
        receiver = new ReceiverUDP(this);
        receiver.setPortNum(Integer.parseInt(port));
        receiverThread = new Thread(receiver);
        receiverThread.start();
        System.out.println("CLIENT:: INFO: Started an instance of receiver.");
    }

    public void setSenderSlow(boolean slow){
        sender.setSlowMode(slow);
    }
    
    public void informAndUpdate(String dataToSend) throws Exception {
        //dataByteArray = 200#hostName(20 bytes- padded with spaces if < 20)#data$
        byte[] dataByteArray = new byte[dataToSend.getBytes().length+3];
        //Inform and Update message code == 200
        System.arraycopy("001".getBytes(),0,dataByteArray,0,"001".getBytes().length);
        System.arraycopy(dataToSend.getBytes(),0,dataByteArray,3,dataToSend.getBytes().length);
        //Send the packet to the Transportation layer
        this.expectedMessage = "200";
        sender.rdtSend(dataByteArray);
    }

    public void clientSearchReq(String key) throws Exception{
        byte[] dataByteArray = new byte[key.length()+3];
        System.arraycopy("002".getBytes(),0,dataByteArray,0,"002".getBytes().length);
        System.arraycopy(key.getBytes(),0,dataByteArray,3,key.getBytes().length);
        this.expectedMessage = "300";
        sender.rdtSend(dataByteArray);
    }

    public void clientDownloadReq(String fileReq) throws Exception{
        byte[] dataByteArray = new byte[fileReq.length()+3];
        System.arraycopy("003".getBytes(),0,dataByteArray,0,"003".getBytes().length);
        System.arraycopy(fileReq.getBytes(),0,dataByteArray,3,fileReq.getBytes().length);
        this.expectedMessage = "500";
        sender.rdtSend(dataByteArray);
    }

    public void clientExitReq() throws Exception{
        try {
            System.out.println("CLIENT:: INFO: Attempting to gracefully exit the server.");
            String exit = "Client Exit";
            byte[] dataByteArray = new byte[exit.length() + 3];
            System.arraycopy("004".getBytes(), 0, dataByteArray, 0, "004".getBytes().length);
            System.arraycopy(exit.getBytes(), 0, dataByteArray, 3, exit.getBytes().length);
            this.expectedMessage = "100";
            sender.rdtSend(dataByteArray);
        }
        catch(Exception e){
            System.out.println("CLIENT:: INFO: Failed to exit the server.");
        }
    }

    public void rcvServerOk(){
        System.out.println("CLIENT:: INFO: Server OK message received.");
    }

    public void rcvServerErr(){
        System.out.println("CLIENT:: INFO: Server Error message received.");
    }

    public void clearTableModel(DefaultTableModel myTableModel){
        if (myTableModel.getRowCount() > 0) {
            for (int i = myTableModel.getRowCount() - 1; i > -1; i--) {
                myTableModel.removeRow(i);
            }
        }
    }

    public void rcvSearchResponse(String data){
        try {
            clearTableModel(searchResults);
            String[] files = data.split("\\?"); //split by ? (each file)
            for (String file : files) {
                String[] row = file.split("#");
                System.out.println(row[0] + " " + row[1] + " " + row[2] + " " + row[3]);
                searchResults.addRow(row);
                searchResults.fireTableDataChanged();
            }
            this.gui.updateResults(searchResults);
        }
        catch(Exception e){
            System.out.println("CLIENT:: ERROR: Failed to receive a search response.");
        }
    }

    public void rcvConnectionInfo(String data){
        System.out.println("CLIENT:: INFO: Received data for connection info -> " + data);
        //establish tcp connection with this...
    }

    public void rcvExitResponse(){
        this.gui.hideWindow();
    }
    
    private boolean isExpectedMessage(String method){
        if(this.expectedMessage.isEmpty()){
            return true;
        }
        if(method == "400"){
            return true;
        }
        return (method.equals(this.expectedMessage));
    }

    public void filterMessage(String method, String data, String ip, String port) throws Exception{
        if(isExpectedMessage(method)){
            switch(method) {
                case "200":
                    rcvServerOk();
                    break;
                case "400":
                    rcvServerErr();
                    break;
                case "300":
                    rcvSearchResponse(data);
                    break;
                case "500":
                    rcvConnectionInfo(data);
                    break;
                case "100":
                    rcvExitResponse();
                    break;
            }
        }
        else{
            System.out.println("CLIENT:: ERROR: Method num received does not match the expected method, expected: " + this.expectedMessage + ", actual: " + method);

        }
    }
}
