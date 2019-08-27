
import java.io.DataInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.*;  
import java.io.*;
import java.util.*; 

public class FileServer {
	public static Vector<String> Group;
    public static Map< String, Vector <String> > GroupUserList;
	public static Map<String, Socket> clients = new HashMap<String, Socket>();
	public static List<Socket> Sending = new ArrayList<Socket>();
	public static void main(String[] args) {
		ServerSocket ss = null;
		DatagramSocket udp_ser = null;
       	int port  = 3333,udp_ser_port=9000;
		Group = new Vector<String>();
		GroupUserList = new HashMap< String, Vector <String> >();
		try{
			ss = new ServerSocket(port);
        	udp_ser = new DatagramSocket(udp_ser_port);
		}catch(IOException e){
			e.printStackTrace();
		}
		while (true) {
			Socket s =null;
            try
			{
				s = ss.accept();
				DataInputStream dataInpStream=new DataInputStream(s.getInputStream());
				DataOutputStream dataOutStream = new DataOutputStream(s.getOutputStream());
				System.out.println("New thread for client");
				Thread t = 	new ClientHandler(s,dataInpStream,dataOutStream,udp_ser);
				t.start();	
			}
		    catch(Exception r)
			{
				// s.close();
			    System.out.println("Socket timed out!");
			    r.printStackTrace();
			    break;
			}
		}
	}

}

class ClientHandler extends Thread{

	final DataInputStream dataInpStream; 
    final DataOutputStream dataOutStream; 
    final Socket s;
	final DatagramSocket udp_ser;
	public String username;
	public ClientHandler(Socket s, DataInputStream dataInpStream, DataOutputStream dataOutStream, DatagramSocket udp_ser)  
    { 
        this.s = s; 
        this.dataInpStream = dataInpStream; 
        this.dataOutStream = dataOutStream;
		this.udp_ser = udp_ser; 
    } 
	public static void RecieveStatus(long a, long b)
	{
	    //Function to Print the Progress bar.
	    System.out.println("File Received : ");
	    int per =(int) ((a*100)/b);
	    System.out.printf("[");
	    for(int k=0;k<per/10;k++)
		System.out.printf("=");
	    for(int k=per/10;k<10;k++)
		System.out.printf(" ");
	    System.out.println("]           " +  String.valueOf(per) + "%");
	}
	public void run(){
		while(true)
			{
				try{
				//Reading the message from client.
					String strRecv="";
					// System.out.println("ikada dhaka ok ankunta");
					try{
						strRecv = dataInpStream.readUTF();
					}
					catch(Exception e){
						// System.out.println("panicheyatle");
					}
					//Checking if it is a file transfer request.
					String cmp1 = "upload",cmp2 = "uploadudp",cmp3="create",cmp4="move",cmp5 ="createuser",cmp6="creategroup",cmp7="listgroups";
					String cmp8 = "joingroup",cmp9 = "leavegroup",cmp10 = "listdetail",cmp11 = "getfile";
					String[]  Recv = strRecv.split(":");
					for(int i= 0; i <=Recv.length-1; i++)
						{System.out.println(Recv[i]);}
					byte[] contents = new byte[1000];
					BufferedOutputStream bufferOutStream=null;
					// System.out.println(Recv.length);
					
					if(Recv.length == 4){
						if(Recv[3].equals(cmp7)){
							String sendresponse = "";
							Iterator grp = FileServer.Group.iterator();
					        while (grp.hasNext()) { 
            					sendresponse += grp.next();
								sendresponse += "    ";
							} 
							dataOutStream.writeUTF(sendresponse);
						}
					}
					if(Recv.length == 5){
						if(Recv[4].equals(cmp3)){
							// System.out.println("sdgksadknldsfnb");
							String temp="";
							temp = "./" +Recv[3]+"/" + Recv[1];
							File folder = new File(temp);
							folder.mkdirs();
						}
						if(Recv[4].equals(cmp5)){
							String temp="";
							temp = "./" +Recv[3];
							File folder = new File(temp);
							folder.mkdirs();
							username = Recv[3];
							FileServer.clients.put(Recv[3],this.s);
							int tempolen = FileServer.clients.size();
							dataOutStream.writeUTF("As per your request User is created"+tempolen);
						}
						if(Recv[4].equals(cmp6)){
							FileServer.Group.add(Recv[3]);
							dataOutStream.writeUTF("As per your request new group is created with name "+ Recv[3] + "Now the groups length is: " +FileServer.Group.size());
						}
						if(Recv[4].equals(cmp8)){
							if(FileServer.GroupUserList.containsKey(Recv[3])){
								Vector <String> Listgrp = FileServer.GroupUserList.get(Recv[3]);
								Listgrp.add(Recv[2]);
								FileServer.GroupUserList.put(Recv[3],Listgrp);
								dataOutStream.writeUTF("Now the length of "+ Recv[3] +" after you joined is:" +Listgrp.size());
							}
							else{
								Vector <String> single = new Vector<String> ();
								single.add(Recv[2]);
								FileServer.GroupUserList.put(Recv[3],single);
								dataOutStream.writeUTF("Now the length of "+ Recv[3] +" after you joined is:" +single.size());
							}

						}
						if(Recv[4].equals(cmp9)){
							if(FileServer.GroupUserList.containsKey(Recv[3])){
								Vector <String> Listgrp = FileServer.GroupUserList.get(Recv[3]);
								if(Listgrp.size() >= 1){
									Listgrp.remove(Recv[2]);
									FileServer.GroupUserList.put(Recv[3],Listgrp);
									dataOutStream.writeUTF("Now the length of "+ Recv[3] +" after you left is:" +Listgrp.size());								
								}
								else{
									dataOutStream.writeUTF("You are not there in the group Please check the group name");
								}
							}
							else{
								dataOutStream.writeUTF("You are not there in the group Please check the group name");
							}
														
						}
						if(Recv[4].equals(cmp10)){
						if(FileServer.GroupUserList.containsKey(Recv[3])){
								Vector <String> Listgrp = FileServer.GroupUserList.get(Recv[3]);
								if(Listgrp.size() >= 1){
									if(Listgrp.contains(Recv[2])){
										String response ="";
										Iterator usernameingrp = Listgrp.iterator();
										while (usernameingrp.hasNext()) {
											String username = "./";
											response += "Files under the user"; 
											username += usernameingrp.next();
											response += username;
											response += " ";
											File folder = new File(username);
											File[] files = folder.listFiles();
											for (File file : files)
											{
												if (file.isFile())
												{
													response += file.getPath();
													response += "\n";
												}
											}	
        								}
										dataOutStream.writeUTF(response);
									}
									else{
										dataOutStream.writeUTF("You are not there in the group Please check the group name");
									}
								}
								else{
									dataOutStream.writeUTF("You are not there in the group Please check the group name");
								}
							}
							else{
								dataOutStream.writeUTF("You are not there in the group Please check the group name");
							}
						}
						if(Recv[4].equals(cmp11)){
							System.out.println("Server ready for connection");           // binding with port: 4000
							System.out.println("Connection is successful and wating for chatting");
																														
													// reading the file name from client
							String fname ="./";
							String Path[] = Recv[3].split("/"); 
							for(int i=1;i<Path.length-1;i++){
								fname += Path[i];
								fname += "/";
							}
							fname += Path[Path.length -1];
													// reading file contents
							BufferedReader contentRead = new BufferedReader(new FileReader(fname) );
								
												// keeping output stream ready to send the contents
							OutputStream ostream = this.s.getOutputStream( );
							PrintWriter pwrite = new PrintWriter(ostream, true);
							
							String str;
							while((str = contentRead.readLine()) !=  null) // reading line-by-line from file
							{
								pwrite.println(str);         // sending each line to client
							}
						
							this.s.close();       // closing network sockets
							pwrite.close();  contentRead.close();
						}
					}
					if(Recv.length==7){
						if(Recv[6].equals(cmp4)){
							String src="",dest="";
							
							src = "./"+Recv[4]+Recv[5]+Recv[2].substring(1);
							dest = "./"+Recv[4]+Recv[5]+Recv[3].substring(1);
							// System.out.println(src);
							// System.out.println(dest);
							File a = new File(src);
							a.renameTo(new File(dest + a.getName()));
							a.delete();
						}
					}
					if(Recv.length==6){
						//File Transfer.
						int fileSize = Integer.parseInt(Recv[1]),rec=0,bread;
						try{
							//Creating the file and opening bufferreader.
							String fileLocationdir ="./"+Recv[4];
							System.out.println(fileLocationdir);
							File tt = new File(fileLocationdir,Recv[2]);
							tt.createNewFile();
							FileOutputStream fos = new FileOutputStream(tt);
							bufferOutStream = new BufferedOutputStream(fos);
								
						} catch (IOException e) {
							System.out.println("Error Creating and writing to file.");
							e.printStackTrace();
						}
						if(Recv[0].equals(cmp1))
							{
							//TCP

							while( (bread=this.dataInpStream.read(contents) ) !=-1)
								{
								bufferOutStream.write(contents, 0, bread);
								rec += bread;
								RecieveStatus(rec,fileSize);
								}
							this.s.close();
							}
						else
							{
							//UDP.
							//Closing the already open TCP socket and recieveing packets.
							this.s.close();
							// System.out.println("sdkvksdjfkvbhefdk");

							while(rec!=fileSize)
								{
								DatagramPacket dp=new DatagramPacket(contents,contents.length);
								this.udp_ser.receive(dp);
								bread = dp.getLength();
								rec += bread;
								bufferOutStream.write(contents, 0, bread);
								RecieveStatus(rec,fileSize);
								String ram = new String("UDPEND"),ll = new String(contents);
								if( ram.equals(ll) )
									break;
								}
							}
						bufferOutStream.flush();
						System.out.println("File Receiving Completed");
						System.out.printf("$>>");
					}
				}
				catch(IOException e){
					e.printStackTrace();
					System.out.println("Socket timed out!");
					break;
				}
			}
		try{
			this.dataInpStream.close();
			this.dataOutStream.close();
		}
		catch(IOException e){
			e.printStackTrace();
		}
	}
}
