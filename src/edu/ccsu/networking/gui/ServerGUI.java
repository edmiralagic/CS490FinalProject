package edu.ccsu.networking.gui;

import edu.ccsu.networking.main.Server;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.table.DefaultTableModel;

public class ServerGUI extends JFrame {


    Server server = new Server(this);

    JPanel table_panel = new JPanel();
    JPanel clients_panel = new JPanel();
    JPanel slow_panel = new JPanel();
    JPanel slow_clients_panel = new JPanel();

    private int portNum;

    private String[] tableColumns = {"Filename", "Size", "IP Address", "Port Number"};

    private String[][] directoryData = new String[][] {};

    DefaultTableModel directory = new DefaultTableModel(directoryData, tableColumns);

    Container c = getContentPane();

    JButton update_button = new JButton("Update");
    JCheckBox slowMode = new JCheckBox("Slow");
    JLabel numClients = new JLabel("0 clients in the current network.");

    public void hideWindow(){
        this.setVisible(false);
    }

    public void updateLocalData(File mFile){
        String[] tempData = {mFile.getName(), String.valueOf(mFile.length()), mFile.getAbsolutePath()};
        directory.addRow(tempData);
        directory.fireTableDataChanged();

    }

    public void updateDirectory(DefaultTableModel newDirectory){
        updateTableModel(directory, newDirectory);
    }

    public void updateClients(int num){
        numClients.setText(String.valueOf(num) + " clients in the current network.");
        this.invalidate();
        this.validate();
        this.repaint();
    }

    public void updateDirectory(){
        this.directory.fireTableDataChanged();
        System.out.println("GUI:: INFO: The directory has been refreshed (server side).");
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

    public ServerGUI(String portNumber){
        super("Welcome to NapsterLITE [S]");

        System.out.println("GUI:: INFO: SERVER GUI STARTED.");

        this.portNum = Integer.parseInt(portNumber);

        try {
            server.startReceiverUDP(portNumber);
            server.startSenderUDP(portNumber);
        }
        catch(Exception e){
            System.out.println("GUI:: ERROR: Failed to start sender or receiver udp (server side).");
        }
        this.setLocation(250,250);
        this.setDefaultLookAndFeelDecorated(true);
        BorderLayout bl = new BorderLayout();
        this.setLayout(bl);

        JTable localTable = new JTable(directory);

        table_panel.setLayout(new BoxLayout(table_panel, BoxLayout.PAGE_AXIS));
        table_panel.setBorder(BorderFactory.createTitledBorder("DIRECTORY"));
        table_panel.add(new JScrollPane(localTable));
        table_panel.add(update_button);

        clients_panel.setBorder(BorderFactory.createTitledBorder("CLIENTS"));
        clients_panel.add(numClients);

        slow_clients_panel.setLayout(new BoxLayout(slow_clients_panel, BoxLayout.PAGE_AXIS));
        slow_clients_panel.add(clients_panel);
        slow_clients_panel.add(slow_panel);

        slow_panel.setBorder(BorderFactory.createTitledBorder("SLOW"));
        slow_panel.add(slowMode);

        this.add(table_panel, BorderLayout.WEST);
        this.add(slow_clients_panel, BorderLayout.EAST);

        this.slowModeAction();
        this.updButtonAction();
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.pack();
        this.setResizable(false);
        this.setVisible(true);
    }

    public void slowModeAction(){
        slowMode.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(slowMode.isSelected()){
                    server.setSlowMode(true);
                }
                else{
                    server.setSlowMode(false);
                }
            }
        });
    }

    public void updButtonAction(){
        update_button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                updateDirectory();
            }
        });
    }

}