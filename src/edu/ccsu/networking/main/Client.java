package edu.ccsu.networking.main;

import edu.ccsu.networking.gui.ClientGUI;
import edu.ccsu.networking.udp.*;

import javax.swing.table.DefaultTableModel;
import java.io.*;
import java.net.*;
import java.util.Arrays;

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
    String[] localColumns = {"File Name", "File Size", "Location"};
    String[][] resultsData = {};
    DefaultTableModel searchResults = new DefaultTableModel(resultsData,columns);
    DefaultTableModel localTable = new DefaultTableModel(resultsData, localColumns);
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

    /**
     * Start a new Receiver for receiving all the messages from the Server for this instance of the Client.
     * 
     * @param port 
     */
    public void startReceiverUDP(String port){
        receiver = new ReceiverUDP(this);
        receiver.setPortNum(Integer.parseInt(port));
        
        //Start a new Receiver for all the UDP messages that the Client will get from the Server
        receiverThread = new Thread(receiver);
        receiverThread.start();
        System.out.println("CLIENT:: INFO: Started an instance of receiver.");
    }

    /**
     * Sets the value for the Slow mode for the client according to the Slow Mode check box in ClientGUI instance
     * connected to this Client instance.
     * 
     * @param slow 
     */
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

    /**
     *  Receive the 500 status Download info in response to Download Request 
     * @param data 
     */
    public void rcvConnectionInfo(String data){
        try {
            Thread.sleep(2000);
            String[] fileInfo = data.split("#");
            Socket socket = new Socket(fileInfo[2], 7002);
            InputStream fromServer = socket.getInputStream();

            String fileName = fileInfo[0];
            File file = new File(fileName);
            BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(file));
            int count = 0;

            byte[] buffer = new byte[2048];

            while ((count = fromServer.read(buffer)) != -1) {
                buffer = Arrays.copyOf(buffer, count);
                output.write(buffer);
            }
            fromServer.close();
            output.flush();
            socket.close();
        }
        catch(Exception e){
            System.out.println("CLIENT:: ERROR: Could not receive TCP file");
        }
    }
    
    /**
     * This method is invoked when we have successfully received the Exit ACK from the server.
     * This is the end point for this Client instance.
     */
    public void rcvExitResponse(){
        this.gui.hideWindow();
        System.exit(0);
        //REMARK:: Memory leak! What about closing all the ports here for the Client?
    }
    
    /**
     * Used as a helper method by filterMessage to check if the packet has the expected method # in header.
     * 
     * @param method
     * @return True of the packet received is expected, else False.
     */
    private boolean isExpectedMessage(String method){
       // REMARK:: WHAT DA HELL BRAH?
//        if(this.expectedMessage.isEmpty()){
//            return true;
//        }
        // REMARK:: Would be better if we call rcvServerErr here instead of sending back true!
        if(method.equalsIgnoreCase("400") || method.equalsIgnoreCase("600")){
            return true;
        }
        else{
            return (method.equalsIgnoreCase(this.expectedMessage));
        }
    }

    public void updateTableModel(DefaultTableModel newTableModel){
        clearTableModel(this.localTable);
        for(int r = 0; r < newTableModel.getRowCount(); r++){
            String[] tempData = {(newTableModel.getValueAt(r,0).toString()),(newTableModel.getValueAt(r,1).toString()),(newTableModel.getValueAt(r,2).toString())};
            this.localTable.addRow(tempData);
        }
        this.localTable.fireTableDataChanged();
    }


    public String[] getFileInfo(String keyword){
        try {
            //retrieveLocalTable();
            this.localTable.fireTableDataChanged();
            String fileName = keyword.split("#")[0];
            System.out.println(this.localTable.getRowCount());
            for (int r = 0; r < this.localTable.getRowCount(); r++) {
                if (this.localTable.getValueAt(r, 0).toString().equalsIgnoreCase(fileName)) {
                    String[] info = {(this.localTable.getValueAt(r, 0).toString()), (this.localTable.getValueAt(r, 1).toString()), (this.localTable.getValueAt(r, 2).toString())};
                    return info;
                }
            }
        }
        catch(Exception e){
            System.out.println("CLIENT:: ERROR: Failed to get file info for TCP connection.");
            e.printStackTrace();
        }
        return null;
    }

    public void startTCPSender(String reqFile){
        try {
            System.out.println("startTCPsender is started!");
            ServerSocket serverSocket = new ServerSocket(7002);
            Socket clientSocket = serverSocket.accept();
            
            File file = new File("C:\\Users\\deepa\\Videos\\Atlas, The Next Generation.mp4"); //file location

            BufferedInputStream fileInputStream = new BufferedInputStream(new FileInputStream(file));
            OutputStream outputStream = clientSocket.getOutputStream();
            int count = 0;
            
            byte[] buffer = new byte[8000];
            while((count = fileInputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, count);
            }
            
            fileInputStream.close();
            outputStream.close();
            outputStream.flush();
            serverSocket.close();
            clientSocket.close();
        }
        catch(Exception e){
            System.out.println("CLIENT:: ERROR: Failed to establish the TCP connection.");
        }
    }

    /**
     * Filters all the messages coming in and calls the appropriate method according to the 
     * method # in the packet headers.
     * 
     * @param method
     * @param data
     * @param ip
     * @param port
     * @throws Exception 
     */
    @Override
    public void filterMessage(String method, String data, String ip, String port) throws Exception{
        // REMARK:: Is this called after every packet is received or after the Client gets the whole message
        // REMARK2:: Why does it need the IP and Port?
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
                case "600":
                    startTCPSender(data);
                    break;
            }
        }
        else{
            System.out.println("CLIENT:: ERROR: Method num received does not match the expected method, expected: " + this.expectedMessage + ", actual: " + method);

        }
    }
}
