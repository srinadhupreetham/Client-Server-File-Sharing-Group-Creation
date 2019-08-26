
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.*;  
import java.io.*;
import java.util.*; 
import java.net.InetAddress; 
public class FileClient extends Thread {
	public static void TrasferStatus(long a, long b)
	{
	    //Function to Print the Status bar.
	    System.out.println("File Transfer Complete Status: ");
	    int percentage =(int) ((a*100)/b);
	    System.out.printf("[");
	    for(int k=0;k<percentage/10;k++)
		System.out.printf("=");
	    for(int k=percentage/10;k<10;k++)
		System.out.printf(" ");
	    System.out.println("]           " +  String.valueOf(percentage) + "%");
	}
	public static void main(String[] args) {
        int type =0;
       	int client_port=3333,udp_client_port=9000;
		String nameport="thisuser";
        String str1="",str2="";
		String ourip ="";
		try{		
			InetAddress inetAddresslocal = InetAddress.getLocalHost();
		ourip = inetAddresslocal.getHostAddress();
		System.out.println(ourip);
		}
		catch(IOException e)
        {
            System.out.println("Error in reading Buffer: Please Check");
        }
	while(true)
	    {
		//Running the Client part in this loop(main thread.)
		BufferedReader buffer=new BufferedReader(new InputStreamReader(System.in));
		System.out.printf("$>>");
		try
        {
            str1=buffer.readLine();
        }
        catch(IOException e)
        {
            System.out.println("Error in reading Buffer: Please Check");
        }
		
		String[] commands = str1.split(" ");
		String cmp1 = "upload",cmp2 ="uploadudp",cmp3="createfolder",cmp4="movefile",cmp5="createuser";
		if(commands.length == 3){
			if(commands[0].equals(cmp4)){
				try{
					//to other pc	//Connect
  				    // Socket s= new Socket("10.1.34.33",client_port);
					Socket s= new Socket("localhost",client_port);
				    DataInputStream dataInpStream=new DataInputStream(s.getInputStream());  
				    DataOutputStream dataOutStream=new DataOutputStream(s.getOutputStream());

                    byte[] contents;
				    dataOutStream.writeUTF("start"+":" + commands[0] +":"+commands[1]+":"+commands[2]+":"+ourip+":"+nameport+":"+"move");
                    System.out.println("Move File Completed");
					dataOutStream.flush();  
					dataInpStream.close();
					s.close();
				}
				catch(IOException ex)
				    {
					System.out.println("Unable to Move the Files " + commands[1]);
				    }
			    }
		}
        if(commands.length == 2){
			if(commands[0].equals(cmp5)){
				try{
					//to other pc	//Connect
  				    // Socket s= new Socket("10.1.34.33",client_port);
					Socket s= new Socket("localhost",client_port);
				    DataInputStream dataInpStream=new DataInputStream(s.getInputStream());  
				    DataOutputStream dataOutStream=new DataOutputStream(s.getOutputStream());

                    byte[] contents;
					nameport = commands[1];
				    dataOutStream.writeUTF("create"+":" + commands[0] +":"+ourip+":"+nameport+":"+"createuser");
                    System.out.println("Create  User Completed");
					dataOutStream.flush();  
					dataInpStream.close();
					s.close();
				}
				catch(IOException ex)
				    {
					System.out.println("Unable to Move the Files " + commands[1]);
				    }
			}
			if(commands[0].equals(cmp3)){
				try{
					//to other pc	//Connect
  				    // Socket s= new Socket("10.1.34.33",client_port);
					// String IPaddr = "10.1.34.33";
					Socket s= new Socket("localhost",client_port);
					System.out.println(s.getLocalAddress().getHostAddress());
				    DataInputStream dataInpStream=new DataInputStream(s.getInputStream());  
				    DataOutputStream dataOutStream=new DataOutputStream(s.getOutputStream());

                    byte[] contents;
				    dataOutStream.writeUTF(commands[0] +":"+commands[1]+":"+ ourip +":" + nameport +":"+ "create");
                    System.out.println("Directory Creation Completed");
					dataOutStream.flush();  
					dataInpStream.close();
					s.close();
				}
				catch(IOException ex)
				    {
					System.out.println("Unable to create the directory " + commands[1]);
				    }
			    }
            if(commands[0].equals(cmp1) || commands[0].equals(cmp2)){
                //upload request
                try{
                    File fileName = new File(commands[1]);
                    FileInputStream fileInpStream = new FileInputStream(fileName);
                    BufferedInputStream BuffInpStream = new BufferedInputStream(fileInpStream);
                    //Connect
  				    Socket s= new Socket("localhost",client_port);
				    DataInputStream dataInpStream=new DataInputStream(s.getInputStream());  
				    DataOutputStream dataOutStream=new DataOutputStream(s.getOutputStream());

                    byte[] contents;
				    long size = fileName.length(),sentSize=0;
				    dataOutStream.writeUTF(commands[0] +":" + String.valueOf(size)+":"+commands[1]+":"+ourip+":"+nameport+":"+ "uploaded");
                    if(commands[0].equals(cmp1)){
                        //TCP
                        while(sentSize!=size)
						{
						    int window = 1000;
						    if(size - sentSize >= window)
							sentSize += window;
						    else
							{
							    window = (int)(size - sentSize);
							    sentSize = size;
							}
						    contents = new byte[window];
						    BuffInpStream.read(contents,0,window);
						    TrasferStatus(sentSize,size);
						    dataOutStream.write(contents);
						}
					    System.out.println("File Transfer Completed");
					    dataOutStream.flush();  
					    dataInpStream.close();
					    s.close();
                    }
                    else
					{
					    //UDP.
					    //Closing the existing TCP socket.
					    dataOutStream.flush();  
					    dataInpStream.close();
					    s.close();

					    //Creating the UDP socket and sending.
					    DatagramSocket udpSocket = new DatagramSocket();
					    InetAddress IPAddress = InetAddress.getByName("localhost");

					    // InetAddress IPAddress = InetAddress.getByName("localhost");
					     while(sentSize!=size)
						{
						    int window = 1000;
						    if(size - sentSize >= window)
							sentSize += window;
						    else
							{
							    window = (int)(size - sentSize);
							    sentSize = size;
							}
						    contents = new byte[window];
						    BuffInpStream.read(contents,0,window);
						    TrasferStatus(sentSize,size);
						    DatagramPacket udpPacket = new DatagramPacket(contents,window, IPAddress, udp_client_port);
						    udpSocket.send(udpPacket);
						}
					     contents = new String("UDPEND").getBytes();
					     DatagramPacket udpPacket = new DatagramPacket(contents,6, IPAddress, udp_client_port);
					     udpSocket.send(udpPacket);
					     udpSocket.close();
					    
					}
				}
				catch(FileNotFoundException ex)
				    {
					System.out.println( commands[1]  + " File not found.");
				    }
				catch(IOException ex)
				    {
					System.out.println("Unable to open the file " + commands[1]);
				    }
			    }	    
            }
        }
        }	
}
