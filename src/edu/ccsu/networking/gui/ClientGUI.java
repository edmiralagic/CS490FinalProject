/**
 * @author Deepankar Malhan, Edmir Alagic, Ben Downs
 * 
 * This class contains all the method for the Client GUI, i.e. ActionListeners for all the buttons, setting up
 * all the GUI elements, updating both TableModels (local files table and search results table)
 */
package edu.ccsu.networking.gui;

import edu.ccsu.networking.main.Client;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.table.DefaultTableModel;

public class ClientGUI extends JFrame {
    
    // Connect this exact instance of ClientGUI to one exact instance of Client class.
    Client client = new Client(this);

    JPanel local_panel = new JPanel();
    JPanel search_panel = new JPanel();
    JPanel slow_panel = new JPanel();

    JPanel local_button_panel = new JPanel();
    JPanel search_button_panel = new JPanel();
    JPanel exit_button_panel = new JPanel();
    
    // All the columns for the files on the Client's localhost.
    private String[] localColumns = {"File Name", "File Size", "Location"};
    
    // All the columns that will be displayed inside the search panel.
    private String[] searchColumns = {"File Name", "File Size", "Host IP", "Host Port"};

    private String[][] localData = new String[][] {};
    private String[][] searchData = new String[][] {};

    // All the local files will be stored in one big String with delimiters splitting the various columns.
    // '#' between columns and '?' after a row is finished and a new one is beginning.
    private String localFiles = "";

    DefaultTableModel localTableModel = new DefaultTableModel(localData, localColumns);
    DefaultTableModel searchTableModel = new DefaultTableModel(searchData, searchColumns);
    DefaultTableModel searchedTableModel = new DefaultTableModel(searchData, searchColumns);
    DefaultTableModel tempTableModel = new DefaultTableModel(searchData, searchColumns);
    JTextField search_field = new JTextField();

    JTable localTable = new JTable(localTableModel);
    JTable searchTable = new JTable(searchedTableModel);

    Container c = getContentPane();
    
    // Share button is for the files that the client wants to share with the directory server,i.e. files allowed to
    // be downloaded by other peers.
    JButton share_button = new JButton("Share");
    // Add button to add the files from localhost into a list (and localTable)
    JButton add_button = new JButton("Add");
    // Remove files which the client no longer wants to share with others.
    JButton remove_button = new JButton("Remove");

    JButton search_button = new JButton("Search");
    JButton dwnld_button = new JButton("Download");
    JButton exit_button = new JButton("Exit");
    
    // A check box to put the client side in slow mode, i.e. a 4 second Thread.sleep after sending one packet out of
    // multiple packets in a message
    JCheckBox slowMode = new JCheckBox("Slow");

    /**
     * Hides this GUI window to hidden
     */
    public void hideWindow(){
        this.setVisible(false);
    }

    /**
     * Takes a File as the parameter and adds it to the localTable for the client.
     * Includes 3 values into the localTable: 1. Name  2. Length in String  3. Absolute path to be used by TCP 
     * @param mFile 
     */
    public void updateLocalData(File mFile){
        String[] tempData = {mFile.getName(), String.valueOf(mFile.length()), mFile.getAbsolutePath()};
        localTableModel.addRow(tempData);
        localTableModel.fireTableDataChanged();
        this.client.updateTableModel(localTableModel);

        constrLocalString();
    }

    /**
     * Updates both the localhost files shared with the directory server table and search results table. 
     */
    public void updateTables(){
        localTableModel.fireTableDataChanged();
        searchedTableModel.fireTableDataChanged();
    }

    /**
     * This method constructs the big String which stores all the localhost files shared.
     * The format of the String is: FileName01#FileLength01?FileName02#FileLength02?
     */
    public void constrLocalString(){
        // Reinitializing String every time the method is called, so that the files which are no longer being shared are
        // not included in the new localString.
        localFiles = "";
        for(int r = 0; r < localTableModel.getRowCount(); r++){
            localFiles += localTableModel.getValueAt(r,0) + "#" + localTableModel.getValueAt(r,1) + "?";
        }
    }
    
    /**
     * Updates the original TableModel by clearing it, and copying over each row from the newTableModel to
     * the oldTableModel (the original)
     * 
     * @param oldTableModel
     * @param newTableModel 
     */
    public void updateTableModel(DefaultTableModel oldTableModel, DefaultTableModel newTableModel){
        clearTableModel(oldTableModel);
        for(int r = 0; r < newTableModel.getRowCount(); r++){
            
            // Stores each row of the newTableModle in a temporary String array and adds this array to the old
            // TableModel after clearing the whole oldTableModel at the beginning of the method call.
            
            String[] tempData = {(newTableModel.getValueAt(r,0).toString()),(newTableModel.getValueAt(r,1).toString()),(newTableModel.getValueAt(r,2).toString()),(newTableModel.getValueAt(r,3).toString())};
            oldTableModel.addRow(tempData);
        }
        oldTableModel.fireTableDataChanged();
    }

    public String[][] getLocalTableModel(){
        localTableModel.fireTableDataChanged();
        String[][] temp = new String[localTableModel.getRowCount()][3];
        for(int r = 0; r < localTableModel.getRowCount(); r++){
            temp[r][0] = localTableModel.getValueAt(r,0).toString();
            temp[r][1] = localTableModel.getValueAt(r,1).toString();
            temp[r][2] = localTableModel.getValueAt(r,2).toString();
        }
        return temp;
    }

    /**
     * Clears the whole TableModel given to this method by removing all the rows in the table.
     * Used as a helper method by updateTableModel.
     * 
     * @param myTableModel 
     */
    public void clearTableModel(DefaultTableModel myTableModel){
        if (myTableModel.getRowCount() > 0) {
            for (int i = myTableModel.getRowCount() - 1; i > -1; i--) {
                myTableModel.removeRow(i);
            }
        }
    }

    /**
     * Updates the searchTableModel with the new results TableModel. NOT NEEDED, CALL updateTableModel INSTEAD
     * @param results
     */
    public void updateResults(DefaultTableModel results){
        if(results.getRowCount() == 0){
            // QUESTION:: Why this invocation when it will go into the clearTableModel and do nothing?
            clearTableModel(searchedTableModel);
        }
        // REMARK:: Calling it here anyways, why the if statement before?
        clearTableModel(searchedTableModel);
        // REMARK:: No need to clear before calling updateTableModel, already clears it before doing anything
        updateTableModel(searchedTableModel, results);
        searchedTableModel.fireTableDataChanged();
    }
    
    /**
     * Constructor which initializes the GUI with the Server IP address and port # for the Server and Client
     * 
     * @param targetIP
     * @param targetPort
     * @param clientPort 
     */
    public ClientGUI(String targetIP, String targetPort, String clientPort){
        super("Welcome to NapsterLITE [C]");

        System.out.println("GUI:: INFO: CLIENT GUI STARTED.");
        
        // Start the receiver and sender for the Client side.
        try{
            client.startSenderUDP(targetIP, targetPort, clientPort);
            client.startReceiverUDP(clientPort);
        }
        catch(Exception e){
            System.out.println("GUI:: ERROR: Failed to start sender or receiver udp (client side).");
        }

        this.setLocation(250,250);
        this.setDefaultLookAndFeelDecorated(true);
        BorderLayout bl = new BorderLayout();
        this.setLayout(bl);

        local_button_panel.setLayout(new BoxLayout(local_button_panel, BoxLayout.LINE_AXIS));
        local_button_panel.add(remove_button);
        local_button_panel.add(add_button);
        local_button_panel.add(share_button);

        local_panel.setLayout(new BoxLayout(local_panel, BoxLayout.PAGE_AXIS));
        local_panel.setBorder(BorderFactory.createTitledBorder("LOCAL"));
        local_panel.add(new JScrollPane(localTable));
        local_panel.add(local_button_panel);

        search_button_panel.setLayout(new BoxLayout(search_button_panel, BoxLayout.LINE_AXIS));
        search_button_panel.add(search_button);
        search_button_panel.add(dwnld_button);

        search_panel.setLayout(new BoxLayout(search_panel, BoxLayout.PAGE_AXIS));
        search_panel.setBorder(BorderFactory.createTitledBorder("QUERY"));
        search_panel.add(search_field);
        search_panel.add(new JScrollPane(searchTable));
        search_panel.add(search_button_panel);

        exit_button_panel.setLayout(new BoxLayout(exit_button_panel, BoxLayout.LINE_AXIS));
        exit_button_panel.setBorder(BorderFactory.createTitledBorder("LEAVE THE SERVER"));
        exit_button_panel.add(exit_button, BorderLayout.CENTER);

        slow_panel.setBorder(BorderFactory.createTitledBorder("SLOW"));
        slow_panel.add(slowMode);

        this.add(slow_panel, BorderLayout.CENTER);
        this.add(local_panel, BorderLayout.WEST);
        this.add(search_panel, BorderLayout.EAST);
        this.add(exit_button_panel, BorderLayout.SOUTH);

        this.addButtonAction();
        this.exitButtonAction();
        this.searchButtonAction();
        this.removeButtonAction();
        this.shareButtonAction();
        this.dwnldButtonAction();
        this.slowModeAction();
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.pack();
        this.setResizable(false);
        this.setVisible(true);
    }

    /**
     * An ActionListener for the Slow Mode check box. 
     * If checked, set the client to Slow Mode; else not. Slow Mode: 4 second delay in the sender of Client after every packet is sent.
     */
    public void slowModeAction(){
        slowMode.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(slowMode.isSelected()){
                    client.setSenderSlow(true);
                }
                else{
                    client.setSenderSlow(false);
                }
            }
        });
    }
   
    /**
     * When the Exit button is clicked in the CLientGUI, tries to send a clientExitRequest to the server.
     */
    public void exitButtonAction(){
        exit_button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try{
                    client.clientExitReq();
                }
                catch(Exception er){
                    System.out.println("GUI:: ERROR: Failed to send client exit request (client side).");
                }
            }
        });
    }

    /**
     * Adds an ActionListener to the Search Button.
     * Validates if the keyword is empty, if not send a 002 Client Search Request to the Server.
     * Else, show a popup to the user.
     */
    public void searchButtonAction(){
        search_button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String keyword = search_field.getText();
                if(keyword.isEmpty()){
                    // REMARK:: We need to display a popup instead of printing out to the terminal.
                    System.out.println("GUI:: ERROR: Please enter a keyword to search for (client side).");
                    //JOptionPane.showMessageDialog(frame, "Eggs are not supposed to be green.");
                }
                else{
                    try {
                        client.clientSearchReq(keyword);
                        System.out.println("GUI:: INFO: Search request sent for keyword: " + keyword + " (client side).");
                    }
                    catch(Exception ec){
                        System.out.println("GUI:: ERROR: Search request failed to send (client side).");
                    }
                }
            }
        });
    }

    /**
     * Remove the selected row from the table by removing the row, updating the table, and updating the localString.
     */
    public void removeButtonAction(){
        remove_button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int row = localTable.getSelectedRow();
                if(row == -1) {
                    System.out.println("GUI:: ERROR: You must select a file before you click the remove button (client side).");
                }
                else {
                    localTableModel.removeRow(row);
                    localTableModel.fireTableDataChanged();
                    constrLocalString();
                }
            }
        });
    }

    public void dwnldButtonAction(){
        dwnld_button.addActionListener(new ActionListener() {
           public void actionPerformed(ActionEvent e) {
               int row = searchTable.getSelectedRow();
               if(row == -1)
               {
                   System.out.println("GUI:: ERROR: You must select a file before you click the download button (client side).");
               }
               else
               {
                   String fileName = (searchTable.getModel().getValueAt(row, 0).toString());
                   String fileSize = (searchTable.getModel().getValueAt(row, 1).toString());
                   String fileReq = fileName + "#" + fileSize;
                   try {
                       client.clientDownloadReq(fileReq);
                       System.out.println("GUI:: INFO: Download request sent for File name: " + fileName + " and File size: " + fileSize + " bytes (client side).");
                   }
                   catch(Exception eb){
                       System.out.println("GUI:: ERROR: Client download request failed (client side).");
                   }

               }
           }
       });
    }

    public void shareButtonAction(){
        share_button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(localTableModel.getRowCount() == 0){
                    System.out.println("GUI:: ERROR: You must select some files before you can share anything (client side).");
                }
                else{
                    try {
                        client.informAndUpdate(localFiles);
                    }
                    catch(Exception ea){
                        System.out.println("GUI:: ERROR: Could not send inform and update message (client side).");
                    }
                }
            }
        });
    }

    public void addButtonAction(){
        add_button.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                JFileChooser chooser = new JFileChooser();
                chooser.setMultiSelectionEnabled(true);
                chooser.showOpenDialog(null);

                File[] files = chooser.getSelectedFiles();

                for(File mFile : files){
                    updateLocalData(mFile);
                }
            }
        });
    }
}