import java.io.*;
import java.net.*;

 class FileReceive 
{
	 public static void main(String args[])
	 {
		 try
		 {
			 Socket socket = new Socket("127.0.0.1", 50000);
			 
			 //read and write on socket
			 PrintWriter out = new PrintWriter(socket.getOutputStream());
			 BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			 
			 //read from console
			 BufferedReader bu = new BufferedReader (new InputStreamReader(System.in));
			 
			 String s;
			 
			 while((br.read())!= '~')
			 {
				 System.out.println(br.readLine());
			 }
			 
			 System.out.println("Enter file index no: ");
			 out.println(bu.read());
			 
			 //force write buffer
			 out.flush();
			 
			 //file receive process, independent
			 try
			 {
				 BufferedWriter pw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("C:\\Users\\Benjamin\\workspace\\TCPFileSend\\src\\Received.txt")));
				 
				 while ((s=br.readLine()) != null)
				 {
					 pw.write(s);;
				 }
				 
				 //force write buffer to server
				 
				 pw.close();
				 
				 if (br.readLine() == null)
				 {
					 System.out.println("File Write Successful.  Closing Socket.");
				 }
			 }
			 catch (IOException ioe)
			 {
				 
			 }
		 }
		 catch (Exception E)
		 {
			 System.out.println("Server is down, please try agian later.");
		 }
	 }
}
