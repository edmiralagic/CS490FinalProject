package edu.ccsu.networking.gui;

import edu.ccsu.networking.udp.SenderUDP;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.nio.charset.Charset;
import java.util.Scanner;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;


public class SenderGUI extends JFrame {


    public static void main(String[] args) throws Exception{
        SenderGUI frameTable = new SenderGUI();
        //System.out.println(InetAddress.getLocalHost().getHostAddress());
    }

    JPanel local_panel = new JPanel();
    JPanel search_panel = new JPanel();
    JPanel slow_panel = new JPanel();

    private String[] localColumns = {"Filename", "Size", "Location"};
    private String[] searchColumns = {"Filename", "Size"};

    private String[][] localData = new String[][] {};
    private String[][] searchData = new String[][] {};

    DefaultTableModel localTableModel = new DefaultTableModel(localData, localColumns);
    DefaultTableModel searchTableModel = new DefaultTableModel(searchData, searchColumns);
    DefaultTableModel searchedTableModel = new DefaultTableModel(searchData, searchColumns);
    DefaultTableModel tempTableModel = new DefaultTableModel();

    JTextField search_field = new JTextField();

    Container c = getContentPane();

    JButton share_button = new JButton("Share");
    JButton refresh_button = new JButton("Refresh");
    JButton dwnld_button = new JButton("Download");

    public void hideWindow(){
        this.setVisible(false);
    }

    public void updateLocalData(File mFile){
        Object[] tempData = {mFile.getName(), String.valueOf(mFile.length()), mFile.getAbsolutePath()};
        localTableModel.addRow(tempData);
        localTableModel.fireTableDataChanged();

        Object[] tempData2 = {mFile.getName(), String.valueOf(mFile.length())};
        searchedTableModel.addRow(tempData);
        searchedTableModel.fireTableDataChanged();
    }

    public void getResults(String search){
        if(search.equalsIgnoreCase(" ") || search.isEmpty()){
            System.out.println("EMPTY SEARCH");
            searchedTableModel = searchTableModel;
            searchedTableModel.fireTableDataChanged();
        }
        else{
            tempTableModel.setColumnIdentifiers(searchColumns);
            for(int i = 0; i < searchedTableModel.getRowCount(); i++){
                if((searchedTableModel.getValueAt(i,0).toString()).toLowerCase().contains(search.toLowerCase())){
                    System.out.println("FOUND AT: " + (searchedTableModel.getValueAt(i,0).toString()));
                    String[] tempRow = { (searchedTableModel.getValueAt(i,0).toString()),(searchedTableModel.getValueAt(i,1).toString()) };
                    tempTableModel.addRow(tempRow);
                    tempTableModel.fireTableDataChanged();
                }
            }
            searchedTableModel = tempTableModel;
            tempTableModel.setRowCount(0);
            searchedTableModel.fireTableDataChanged();
        }
        searchedTableModel.fireTableDataChanged();
    }


    public SenderGUI() {
        super("Welcome to NapsterLITE [C]");

        this.setLocation(250,250);
        this.setDefaultLookAndFeelDecorated(true);
        BorderLayout bl = new BorderLayout();
        this.setLayout(bl);

        share_button.setSize(100,50);
        JCheckBox slowMode = new JCheckBox("Slow");

        JTable localTable = new JTable(localTableModel);
        JTable searchTable = new JTable(searchedTableModel);

        local_panel.setLayout(new BoxLayout(local_panel, BoxLayout.PAGE_AXIS));
        local_panel.setBorder(BorderFactory.createTitledBorder("LOCAL"));
        local_panel.add(new JScrollPane(localTable));
        local_panel.add(share_button);

        search_panel.setLayout(new BoxLayout(search_panel, BoxLayout.PAGE_AXIS));
        search_panel.setBorder(BorderFactory.createTitledBorder("QUERY"));
        search_panel.add(search_field);
        search_panel.add(new JScrollPane(searchTable));
        search_panel.add(refresh_button);
        search_panel.add(dwnld_button);

        slow_panel.setBorder(BorderFactory.createTitledBorder("SLOW"));
        slow_panel.add(slowMode);

        this.add(slow_panel, BorderLayout.CENTER);
        this.add(local_panel, BorderLayout.WEST);
        this.add(search_panel, BorderLayout.EAST);

        this.selectButtonAction();
        this.selectSearchAction();
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.pack();
        this.setResizable(false);
        this.setVisible(true);
    }

    public void selectSearchAction(){
        search_field.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String search = search_field.getText();
                getResults(search);
            }
        });
    }


    public void selectButtonAction(){
        share_button.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                try {
                    JFileChooser chooser = new JFileChooser();
                    chooser.setMultiSelectionEnabled(true);
                    chooser.showOpenDialog(null);

                    File[] files = chooser.getSelectedFiles();

                    for(File mFile : files){
                        updateLocalData(mFile);
                    }

                    byte[] targetAddress = {(byte) 127, (byte) 0, (byte) 0, (byte) 1};
                    SenderUDP sender = new SenderUDP(51001);
                    sender.startSender(targetAddress, 51000);

                    // Send the data
                    //sender.rdtSend(data.getBytes(Charset.forName("UTF-8")));
                }
                catch(Exception e){
                    e.printStackTrace();
                }
                //hideWindow();
            }
        });
    }
}