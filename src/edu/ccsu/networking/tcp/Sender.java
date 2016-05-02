/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ccsu.networking.tcp;
import java.net.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 *
 * @author deepa
 */
//public class Sender {
//    
//    public static void main(String[] args) throws IOException, InterruptedException{
//        
//        System.out.println("SENDER:: Initialized!");
//    ServerSocket serverSocket = new ServerSocket(7777);
//    Socket clientSocket = serverSocket.accept();
//    
//    System.out.println("SENDER:: Accepted a connection!");
//    DataOutputStream outputStream = new DataOutputStream(clientSocket.getOutputStream());
//    
//    System.out.println("SERVER:: A data output stream to the client has been created!");
//    //Stores the file in a byte array -- bad but works
//    byte[] fileToSend = Files.readAllBytes(Paths.get("C:\\Users\\deepa\\Documents\\Deepankar\\CCSU\\Semester 4\\CS 490-01\\CS490S16ComputerNetworksSyllabus.docx"));
//    File file = new File("C:\\Users\\deepa\\Documents\\Deepankar\\CCSU\\Semester 4\\CS 490-01\\CS490S16ComputerNetworksSyllabus.docx");
//    //Output the length of the file to the screen and outputStream
//    outputStream.writeLong(file.length());
//    System.out.println("SERVER:: File length is: " + file.length() + "!");
//    long bytesSent = 0;
//    
//    //Calculate how many blocks of file to be sent with max limit 4000 bytes at a time.
//    double x = (double)file.length()/4000;
//    double y = Math.ceil(x);
//    long timesToBeSent = (long)y;
//    
////    do{
////        if(file.length()-outputStream.size() >= 4000) {
////            outputStream.write(fileToSend, outputStream.size(), 4000);
////            System.out.println("Sent 4000 bytes!");
////        }
////        else{
////            outputStream.write(fileToSend, outputStream.size(), (int)(file.length()-outputStream.size()));
////            System.out.println("Sent " + (int)(file.length()-outputStream.size()) + " bytes!");
////        }
////        
////    bytesSent += 4000;
////    timesWritten++;()
////    System.out.println("Times sent: " + timesWritten);
////    }
////    while(bytesSent <= file.length());
////    Thread.sleep(10000);
//
//    
//    System.out.println("SERVER:: The whole file has been sent to the client!");
//    }
//}

public class Sender{
    public static void main(String[] args) throws IOException{

            ServerSocket serverSocket = new ServerSocket(7002);
            Socket clientSocket = serverSocket.accept();
            String fileLocation = "/Users/edmiralagic/Downloads/testMusic.mp4";
            File file = new File(fileLocation);

            BufferedInputStream fileInputStream = new BufferedInputStream(new FileInputStream(file));
            OutputStream outputStream = clientSocket.getOutputStream();

//            long count = 0;
//
//            long fileLength = file.length();
//            int packetSize = 2048;
//
//            byte[] buffer;
//
//            while(count < fileLength) {
//                if(fileLength - count > packetSize){
//                    count += packetSize;
//                }
//                else{
//                    packetSize = (int)(fileLength - count);
//                    count = fileLength;
//                }
//
//                buffer = new byte[packetSize];
//                fileInputStream.read(buffer, 0, packetSize);
//                outputStream.write(buffer);
//            }

            byte[] data;
            data = Files.readAllBytes(Paths.get(fileLocation));
            outputStream.write(data);

            //wait for confirmation from receiver
            //InputStream inputStream = clientSocket.getInputStream();
            //int confirmation = inputStream.read();
            //System.out.println("SERVER:: Read the confirmation: " + confirmation);
//            if(confirmation == 1) {
//                fileInputStream.close();
//                outputStream.close();
//                inputStream.close();
//                clientSocket.close();
//                System.out.println("SENDER:: Closed everything!!");
//            }

            fileInputStream.close();
            outputStream.close();
            outputStream.flush();
            serverSocket.close();
            clientSocket.close();
        
    }
}