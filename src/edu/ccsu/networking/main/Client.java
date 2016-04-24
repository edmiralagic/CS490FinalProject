package edu.ccsu.networking.main;

import edu.ccsu.networking.udp.*;
/**
 *
 * @author Deepankar Malhan, Edmir Alagic, Ben Downs
 */
public class Client extends Thread{    
    Thread client;
    SenderUDP sender;
    
    byte[] serverIPAddress;
    int serverPortNum;
    
    public Client(byte[] serverIPAddress, int serverPortNum) {
        this.serverIPAddress = serverIPAddress;
        this.serverPortNum = serverPortNum;
    }
    @Override
    public void run(){
        //Needs to ask for a code from the user, according to that execute different packet
        //headers. Finally call rdtsend() in all methods.
    }
    
    @Override
    public void start(){
        System.out.println("CLIENT:: INFO: Starting the client side of the application!");
        if(client==null) {
            client = new Thread(this, "client");
            client.start();
        }
    }
}
