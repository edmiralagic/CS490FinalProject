package edu.ccsu.networking.tcp;

import java.net.*;
import java.io.*;

public class Sender{
    public static void main(String[] args) throws IOException{

            ServerSocket serverSocket = new ServerSocket(7002);
            Socket clientSocket = serverSocket.accept();
            String fileLocation = "C:\\Users\\deepa\\Downloads\\RemotePlayInstaller.exe";
            File file = new File(fileLocation);

            BufferedInputStream fileInputStream = new BufferedInputStream(new FileInputStream(file));
            OutputStream outputStream = clientSocket.getOutputStream();


            fileInputStream.close();
            outputStream.close();
            outputStream.flush();
            serverSocket.close();
            clientSocket.close();
        
    }
}