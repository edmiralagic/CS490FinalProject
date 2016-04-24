package edu.ccsu.networking.gui;

import edu.ccsu.networking.udp.SenderUDP;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.Inet4Address;
import java.net.InetAddress;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.table.DefaultTableModel;

public class ClientGUI extends JFrame {


    public static void main(String[] args) throws Exception{
        //ClientGUI frame = new ClientGUI();

    }

    SenderUDP sender = new SenderUDP();

    JPanel local_panel = new JPanel();
    JPanel search_panel = new JPanel();
    JPanel slow_panel = new JPanel();

    JPanel local_button_panel = new JPanel();
    JPanel search_button_panel = new JPanel();

    private int targetPortNum;
    private int portNum;
    private InetAddress targetIP;

    private String[] localColumns = {"Filename", "Size", "Location"};
    private String[] searchColumns = {"Filename", "Size"};

    private String[][] localData = new String[][] {};
    private String[][] searchData = new String[][] {};

    DefaultTableModel localTableModel = new DefaultTableModel(localData, localColumns);
    DefaultTableModel searchTableModel = new DefaultTableModel(searchData, searchColumns);
    DefaultTableModel searchedTableModel = new DefaultTableModel(searchData, searchColumns);
    DefaultTableModel tempTableModel = new DefaultTableModel(searchData, searchColumns);
    JTextField search_field = new JTextField();

    Container c = getContentPane();

    JButton share_button = new JButton("Share");
    JButton select_button = new JButton("Select");

    JButton refresh_button = new JButton("Refresh");
    JButton dwnld_button = new JButton("Download");

    JCheckBox slowMode = new JCheckBox("Slow");

    public void hideWindow(){
        this.setVisible(false);
    }

    public void updateLocalData(File mFile){
        String[] tempData = {mFile.getName(), String.valueOf(mFile.length()), mFile.getAbsolutePath()};
        localTableModel.addRow(tempData);
        localTableModel.fireTableDataChanged();

    }

    public void updateTableModel(DefaultTableModel oldTableModel, DefaultTableModel newTableModel){
        clearTableModel(oldTableModel);
        for(int r = 0; r < newTableModel.getRowCount(); r++){
            String[] tempData = {(newTableModel.getValueAt(r,0).toString()),(newTableModel.getValueAt(r,1).toString())};
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

    public void getResults(String search){
        if(search.equalsIgnoreCase(" ") || search.isEmpty()){
            System.out.println("EMPTY SEARCH");
            updateTableModel(searchedTableModel, searchTableModel);
        }
        else {
            for (int r = 0; r < searchedTableModel.getRowCount(); r++) {
                if ((searchedTableModel.getValueAt(r, 0).toString()).toLowerCase().contains(search.toLowerCase())) {
                    String[] tempRow = {(searchedTableModel.getValueAt(r, 0).toString()), (searchedTableModel.getValueAt(r, 1).toString())};
                    tempTableModel.addRow(tempRow);
                    tempTableModel.fireTableDataChanged();
                }
            }
            updateTableModel(searchedTableModel, tempTableModel);
            clearTableModel(tempTableModel);
        }
        searchedTableModel.fireTableDataChanged();
    }


    public ClientGUI(String targetIP, String targetPort, String clientPort){
        super("Welcome to NapsterLITE [C]");

        try {
            this.targetIP = Inet4Address.getByName(targetIP);
        }
        catch(Exception e){
            System.out.println("GUI:: ERROR: Failed to convert Target IP address (client side).");
        }

        this.targetPortNum = Integer.parseInt(targetPort);
        this.portNum = Integer.parseInt(clientPort);

        sender.setTargetIP(this.targetIP);
        sender.setTargetPort(this.targetPortNum);
        sender.setPortNum(this.portNum);

        this.setLocation(250,250);
        this.setDefaultLookAndFeelDecorated(true);
        BorderLayout bl = new BorderLayout();
        this.setLayout(bl);

        JTable localTable = new JTable(localTableModel);
        JTable searchTable = new JTable(searchedTableModel);

        local_button_panel.setLayout(new BoxLayout(local_button_panel, BoxLayout.LINE_AXIS));
        local_button_panel.add(select_button);
        local_button_panel.add(share_button);

        local_panel.setLayout(new BoxLayout(local_panel, BoxLayout.PAGE_AXIS));
        local_panel.setBorder(BorderFactory.createTitledBorder("LOCAL"));
        local_panel.add(new JScrollPane(localTable));
        local_panel.add(local_button_panel);

        search_button_panel.setLayout(new BoxLayout(search_button_panel, BoxLayout.LINE_AXIS));
        search_button_panel.add(refresh_button);
        search_button_panel.add(dwnld_button);

        search_panel.setLayout(new BoxLayout(search_panel, BoxLayout.PAGE_AXIS));
        search_panel.setBorder(BorderFactory.createTitledBorder("QUERY"));
        search_panel.add(search_field);
        search_panel.add(new JScrollPane(searchTable));
        search_panel.add(search_button_panel);


        slow_panel.setBorder(BorderFactory.createTitledBorder("SLOW"));
        slow_panel.add(slowMode);

        this.add(slow_panel, BorderLayout.CENTER);
        this.add(local_panel, BorderLayout.WEST);
        this.add(search_panel, BorderLayout.EAST);

        this.selectButtonAction();
        this.searchButtonAction();
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
                   sender.setSlowMode(true);
               }
               else{
                   sender.setSlowMode(false);
               }
           }
       });
    }

    public void searchButtonAction(){
        search_field.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String search = search_field.getText();
                getResults(search);
            }
        });
    }

    public void selectButtonAction(){
        select_button.addActionListener(new ActionListener()
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