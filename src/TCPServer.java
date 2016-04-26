

import java.io.*;
import java.net.*;

/**
 * Simple TCP server thread that starts up and waits for TCP connections and
 * echos what is sent capitalized. This is an adaption of the code provided by
 * the Computer Networking: A Top Down Approach book by Kurose and Ross
 *
 * @author Chad Williams
 */
public class TCPServer extends Thread {

  private int port;

  public TCPServer(String name, int port) {
    super(name);
    this.port = port;
  }
  
  
  
  
  public static void main(String args[]) throws IOException
  {
    int portNumber = 20030;

    ServerSocket serverSocket = new ServerSocket(portNumber);


    while ( true ) 
    {
      new ServerConnection(serverSocket.accept()).start();
    } 

  }
}


class ServerConnection extends Thread
{
Socket clientSocket;  
int port;
public ServerConnection(String name, int port) {
    super(name);
    this.port = port;
  }

ServerConnection (Socket clientSocket) throws SocketException
{
  this.clientSocket = clientSocket;
  setPriority(NORM_PRIORITY - 1);     
} 

//starting the thread to begin listening
public void run()
{
	//instantiating InputStream/OutputStream
	InputStream inputStream = null;
	OutputStream outputStream = null;
	
	ServerSocket serverSocket = null;
  try{
	  serverSocket = new ServerSocket(this.port);	 	 
      

      OutputStream outToClient = clientSocket.getOutputStream();

      PrintWriter printOutPut = new PrintWriter(outToClient,true);

      
    	  Socket clientConnectionSocket = serverSocket.accept();
          
          while (clientConnectionSocket.isConnected() && !clientConnectionSocket.isClosed())
          {
          BufferedReader inFromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
          String request = inFromClient.readLine();
        
        //A method for converting a file to inputStream  
          try
          {
        	  inputStream = new FileInputStream("C:\\Users\\Benjamin\\workspace\\NetworkingTCP\\src\\text.txt");
        	  
        	  outputStream = new FileOutputStream(new File("C:\\Users\\Benjamin\\workspace\\NetworkingTCP\\src\\Output.txt"));
        	  
        	  int read = 0;
        	  byte[] bytes = new byte[1024];
        	  
        	  while ((read = inputStream.read(bytes)) != -1)
        	  {
        		  outputStream.write(bytes, 0, read);
        	  }
          
          
          System.out.println("Done!");
          }
          
          catch (IOException e)
          {
        	  e.printStackTrace();
          }
          finally
          {
        	  if (inputStream != null)
        	  {
        		  try
        		  {
        			  inputStream.close();        			  
        		  }
        		  catch (IOException e)
        		  {
        			  e.printStackTrace();
        		  }
        	  }
        	  if (outputStream != null)
        	  {
        		  try
        		  {
        			  //outputStream.flush();
        			  outputStream.close();
        		  }
        		  catch (IOException e)
        		  {
        			  e.printStackTrace();
        		  }
        	  }
          }
          
         // while (inFromClient.ready())
          //{
        	//  String request2 = inFromClient.readLine();
        	//  System.out.println(request2);
        	//  System.out.println("Test");
         // }
          System.out.println(request);         
          if (printOutPut !=null)
          {
        	  System.out.println("Sever Received: " + printOutPut);        	  
          }
          else
          {
        	  clientConnectionSocket.close();
        	  System.out.println("Server Client connection closed");
          }
          }
          
      
       
      printOutPut.write("HTTP/1.1 200 OK\nConnection: close\n\n");
      printOutPut.write("Hello sends from Server");
      


      printOutPut.flush();
      printOutPut.close();

      clientSocket.close();
  }
  catch (Exception e) 
  {
	  e.printStackTrace();
	  if (serverSocket != null)
	  {
		  try
		  {
			  serverSocket.close(); 
		  }	
		  catch (IOException ioe)
		  {
			  //ignore
		  }
	  }	  
      System.out.println(e.getMessage());
      e.printStackTrace();
      }  
}
}

