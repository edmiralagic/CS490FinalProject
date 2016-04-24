package edu.ccsu.networking.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class MainGUI extends JFrame {


    public static void main(String[] args) throws Exception{
        MainGUI frame = new MainGUI();

    }

    JPanel client_panel = new JPanel();
    JPanel server_panel = new JPanel();

    JPanel target_ip_panel = new JPanel();
    JPanel target_port_panel = new JPanel();
    JPanel client_port_panel = new JPanel();
    JPanel server_port_panel = new JPanel();

    JTextField target_ip_field = new JTextField(15);
    JTextField target_port_field = new JTextField(15);
    JTextField client_port_field = new JTextField(15);

    JLabel target_ip_label = new JLabel("Server IP Address: ");
    JLabel target_port_label = new JLabel("Server Port Number: ");
    JLabel client_port_label = new JLabel("Client Port Number: ");

    JTextField server_port_field = new JTextField(15);

    JLabel server_port_label = new JLabel("Server Port Number: ");

    Container c = getContentPane();

    JButton client_button = new JButton("Start Client");
    JButton server_button = new JButton("Start Server");

    public void hideWindow(){
        this.setVisible(false);
    }

    public MainGUI() {
        super("Welcome to NapsterLITE");

        this.setLocation(250,250);
        this.setDefaultLookAndFeelDecorated(true);
        BorderLayout bl = new BorderLayout();
        this.setLayout(bl);

        client_panel.setLayout(new BoxLayout(client_panel, BoxLayout.PAGE_AXIS));
        client_panel.setBorder(BorderFactory.createTitledBorder("CLIENT"));

        target_ip_panel.setLayout(new BoxLayout(target_ip_panel, BoxLayout.LINE_AXIS));
        target_ip_panel.add(target_ip_label);
        target_ip_panel.add(target_ip_field);

        target_port_panel.setLayout(new BoxLayout(target_port_panel, BoxLayout.LINE_AXIS));
        target_port_panel.add(target_port_label);
        target_port_panel.add(target_port_field);

        client_port_panel.setLayout(new BoxLayout(client_port_panel, BoxLayout.LINE_AXIS));
        client_port_panel.add(client_port_label);
        client_port_panel.add(client_port_field);

        client_panel.add(target_ip_panel);
        client_panel.add(target_port_panel);
        client_panel.add(client_port_panel);
        client_panel.add(client_button);

        server_panel.setLayout(new BoxLayout(server_panel, BoxLayout.PAGE_AXIS));
        server_panel.setBorder(BorderFactory.createTitledBorder("SERVER"));

        server_port_panel.setLayout(new BoxLayout(server_port_panel, BoxLayout.LINE_AXIS));
        server_port_panel.add(server_port_label);
        server_port_panel.add(server_port_field);

        server_panel.add(server_port_panel);
        server_panel.add(server_button);


        this.add(client_panel, BorderLayout.WEST);
        this.add(server_panel, BorderLayout.EAST);

        this.clientButtonAction();
        this.serverButtonAction();
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.pack();
        this.setResizable(false);
        this.setVisible(true);
    }


    public void clientButtonAction(){
        client_button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(target_ip_field.getText().isEmpty() || target_port_field.getText().isEmpty() || client_port_field.getText().isEmpty()){
                    System.out.println("GUI:: ERROR: You forgot to fill in some fields (client side).");
                }
                else{
                    hideWindow();
                    ClientGUI client = new ClientGUI(target_ip_field.getText(), target_port_field.getText(), client_port_field.getText());
                }
            }
        });
    }

    public void serverButtonAction(){
        server_button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(server_port_field.getText().isEmpty()){
                    System.out.println("GUI:: ERROR: You forgot to fill in some fields (server side).");
                }
                else{
                    hideWindow();
                    ServerGUI server = new ServerGUI(server_port_field.getText());
                }
            }
        });
    }

}