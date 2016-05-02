package edu.ccsu.networking.gui;

import edu.ccsu.networking.main.Client;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.table.DefaultTableModel;

public class ClientGUI extends JFrame {

    Client client = new Client(this);

    JPanel local_panel = new JPanel();
    JPanel search_panel = new JPanel();
    JPanel slow_panel = new JPanel();

    JPanel local_button_panel = new JPanel();
    JPanel search_button_panel = new JPanel();
    JPanel exit_button_panel = new JPanel();

    private String[] localColumns = {"File Name", "File Size", "Location"};
    private String[] searchColumns = {"File Name", "File Size", "Host IP", "Host Port"};

    private String[][] localData = new String[][] {};
    private String[][] searchData = new String[][] {};

    private String localFiles = "";

    DefaultTableModel localTableModel = new DefaultTableModel(localData, localColumns);
    DefaultTableModel searchTableModel = new DefaultTableModel(searchData, searchColumns);
    DefaultTableModel searchedTableModel = new DefaultTableModel(searchData, searchColumns);
    DefaultTableModel tempTableModel = new DefaultTableModel(searchData, searchColumns);
    JTextField search_field = new JTextField();

    JTable localTable = new JTable(localTableModel);
    JTable searchTable = new JTable(searchedTableModel);

    Container c = getContentPane();

    JButton share_button = new JButton("Share");
    JButton add_button = new JButton("Add");
    JButton remove_button = new JButton("Remove");

    JButton search_button = new JButton("Search");
    JButton dwnld_button = new JButton("Download");
    JButton exit_button = new JButton("Exit");

    JCheckBox slowMode = new JCheckBox("Slow");

    public void hideWindow(){
        this.setVisible(false);
    }

    public void updateLocalData(File mFile){
        String[] tempData = {mFile.getName(), String.valueOf(mFile.length()), mFile.getAbsolutePath()};
        localTableModel.addRow(tempData);
        localTableModel.fireTableDataChanged();

        constrLocalString();
    }

    public void updateTables(){
        localTableModel.fireTableDataChanged();
        searchedTableModel.fireTableDataChanged();
    }

    public void constrLocalString(){
        localFiles = "";
        for(int r = 0; r < localTableModel.getRowCount(); r++){
            localFiles += localTableModel.getValueAt(r,0) + "#" + localTableModel.getValueAt(r,1) + "?";
        }
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

    public void updateResults(DefaultTableModel results){
        if(results.getRowCount() == 0){
            clearTableModel(searchedTableModel);
        }
        clearTableModel(searchedTableModel);
        updateTableModel(searchedTableModel, results);
        searchedTableModel.fireTableDataChanged();
    }

    public ClientGUI(String targetIP, String targetPort, String clientPort){
        super("Welcome to NapsterLITE [C]");

        System.out.println("GUI:: INFO: CLIENT GUI STARTED.");
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

    public void searchButtonAction(){
        search_button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String keyword = search_field.getText();
                if(keyword.isEmpty()){
                    System.out.println("GUI:: ERROR: Please enter a keyword to search for (client side).");
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