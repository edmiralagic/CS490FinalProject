

import java.io.*;
import java.net.*;
import java.util.Arrays;

class FileSend 
{
	public static void main (String args[])
	{
		//keep trying until Client connects
		
		try
		{
			ServerSocket serversocket = new ServerSocket(50000);
			//created serversocket on port 50000.  Ack client.
			
			System.out.println("Running...");
			//accept incoming client request
			
			Socket socket = serversocket.accept();
			System.out.println("Client connected.");
			
			//Read and write on socket
			PrintWriter pw = new PrintWriter (new OutputStreamWriter(socket.getOutputStream()));
			BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			
			//send file list
			String dirname = "C:\\Users\\Benjamin\\workspace\\TCPFileSend\\src\\text.txt";
			File f1 = new File(dirname);
			File fl[] = f1.listFiles();
			
			//Sort Alphabetically
			Arrays.sort(fl);
			
			//counter for required files
			
			int c=0;
			
			for (int i = 0; i<fl.length; i++)
			{
				if(fl[i].canRead() && (fl[i].toString()).endsWith(".txt"))
				{
					c++;
				}
			}
			
			pw.println(" " + c + " .txt files found, listed A-Z.");
			
			for (int i=0; i<fl.length; i++)
			{
				if((fl[i].toString()).endsWith(".txt"))
				{
					pw.println(" " + fl[i].getName() + " " + fl[i].length() + " Bytes");					
				}
			}
				//output string stream delimiter
				pw.println("~");
				pw.flush();
				
				//convert ASCII to decimal value
				String tem = br.readLine();
				int temp = Integer.parseInt(tem);
				temp -=48;
				System.out.println("Index: " + temp);
				
				//Check if the file exists
				boolean flis = false;
				int index = 0;
				
				if (temp >=0 && temp <= fl.length)
				{
					flis = true;
					index = temp;
				}
				
				else
				{
					flis = false;
				}
				
				if (flis)
				{
					try
					{
						//file send process, independent
						File ff = new File(fl[index].getAbsolutePath());
						FileReader fr = new FileReader(ff);
						BufferedReader brf = new BufferedReader(fr);
						String s;
						
						while ((s = brf.readLine())!= null)
						{
							pw.println(s);
						}
						
						//force write buffer to client
						pw.flush();
						
						if (brf.readLine() == null)
						{
							System.out.println("File Read successful.  Closing socket.");
						}
					}
					catch (IOException ioe)
					{
						System.out.println("\n Error in FTP.  Please try again.");
					}					
				}
				
				//close streams and socket.
				br.close();
				socket.close();
				
			}
		
			catch (Exception E)
			{
				System.out.println("\n Connection error, please try again");
			}
		}
	}

	

