

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
			DataOutputStream pw = new DataOutputStream (socket.getOutputStream());
			BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			
			//send file list
			String dirname = "C:\\Users\\deepa\\Documents\\Deepankar\\CCSU\\Semester 4\\CS 490-01";
			File f1 = new File(dirname);
			File fl[] = f1.listFiles();
                       
			
			//Sort Alphabetically
			Arrays.sort(fl);
			
			//counter for required files
			
			int c=0;
			
			for (int i = 0; i<fl.length; i++)
			{
				if(fl[i].canRead())
				{
					c++;
                                        System.out.println(fl[i].toString() + " at index: " + i);
				}
                                else {
                                    System.out.println("Can't read one file!");
                                }
			}
			
			pw.writeBytes(" " + c + " files found, listed A-Z.\n");
                        
                     
			for (int i=0; i<fl.length; i++)
			{
                            pw.writeBytes(" " + fl[i].getName() + " " + fl[i].length() + " Bytes\n");
			}
				//output string stream delimiter
				pw.writeBytes("~");
				pw.flush();
				
				//convert ASCII to decimal value
				String tem = br.readLine();
				int temp = Integer.parseInt(tem);
				temp -=48;
				System.out.println("Index: " + temp);
//				
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
						File fileToSend = new File(fl[index].getAbsolutePath());
//						FileReader fr = new FileReader(fileToSend);
//						BufferedReader brf = new BufferedReader(fr);
                                                FileInputStream fileToSendIStream = new FileInputStream(fileToSend);
                                                byte[] byteArray = new byte[(int)fileToSend.length()];
                                                System.out.println(fileToSendIStream.available()  + " length of the array: " + byteArray.length);

						fileToSendIStream.read(byteArray);
                                                System.out.println(fileToSendIStream.available());
						
						pw.flush();
                                                //pw.write(byteArray, 0, byteArray.length);
						for(int i = 0;i < byteArray.length; i ++) {
                                                    pw.write((int)byteArray[i]);
                                                }
						
						//force write buffer to client
						pw.flush();
						
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

	

