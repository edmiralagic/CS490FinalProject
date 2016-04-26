

import java.io.*;
import java.net.*;

/**
 * Simple client connects sends a sentence periodically and outputs the
 * response. This is an adaption of the code provided by the Computer
 * Networking: A Top Down Approach book by Kurose and Ross
 *
 * @author Chad Williams
 */
public class TCPClient extends Thread {

  private int serverPort;

  public TCPClient(String name, int serverPort) {
    super(name);
    this.serverPort = serverPort;
  }
  
  static String file = "C:\\Users\\Benjamin\\workspace\\NetworkingTCP\\src\\text.txt";
  static PrintWriter outToServer;
  static Socket socket;
  public static void main(String[] args) throws IOException
  {
	  final int PORT = 20020;
	  String serverHostname = new String("127.0.0.1");	  
	  socket = new Socket(serverHostname, PORT);
	  outToServer = new PrintWriter(socket.getOutputStream(), true);
	  
	  sendFile();
	  //receieveFile();
	  
	  outToServer.flush();
	  outToServer.close();
	  socket.close();
  }
  
  
  
  public static void sendFile() throws IOException
  {
	  BufferedReader br = new BufferedReader(new FileReader(file));
	  try
	  {		  
		  String line = br.readLine();
		  while (line!=null)
		  {			  
			  outToServer.write(line);
			  line = br.readLine();
		  }
	  }
	  
	  catch (Exception e)
	  {
		  System.out.println("!!!!");		  
	  }
	  br.close();
  
  }
/**
 * Mistakingly added the code below for receiving a file since this method will only be sending
 * 
 * 
  public static void receieveFile() throws IOException
  {
	  BufferedReader brComingFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	    try
	    {	
	        String inline = brComingFromServer.readLine();
	        
	        while(inline!=null)
	        {
	            System.out.println(inline);
	            inline = brComingFromServer.readLine();
	        }
	    }
	    catch (Exception e)
	    {
	    	 e.printStackTrace();
	    }
	}
	**/
	  
}
  

