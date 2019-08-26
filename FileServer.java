
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
   
	public static void main(String[] args) {
		ServerSocket ss = null;
		DatagramSocket udp_ser = null;
       	int port  = 3333,udp_ser_port=9000;
		Group = new Vector<String>();
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
					String cmp1 = "upload",cmp2 = "uploadudp",cmp3="create",cmp4="move",cmp5 ="createuser",cmp6="creategroup";
					String[]  Recv = strRecv.split(":");
					for(int i= 0; i <=Recv.length-1; i++)
						{System.out.println(Recv[i]);}
					byte[] contents = new byte[1000];
					BufferedOutputStream bufferOutStream=null;
					// System.out.println(Recv.length);
					if(Recv.length == 5){
						if(Recv[4].equals(cmp3)){
							// System.out.println("sdgksadknldsfnb");
							String temp="";
							temp = "./" + Recv[2] +Recv[3]+"/" + Recv[1];
							File folder = new File(temp);
							folder.mkdirs();
						}
						if(Recv[4].equals(cmp5)){
							String temp="";
							temp = "./" + Recv[2] +Recv[3];
							File folder = new File(temp);
							folder.mkdirs();
							dataOutStream.writeUTF("As per your request User is created");
						}
						if(Recv[4].equals(cmp6)){
							FileServer.Group.add(Recv[3]);
							dataOutStream.writeUTF("As per your request new group is created with name "+ Recv[3] + "Now the groups length is: " +FileServer.Group.size());
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
							String fileLocationdir ="./"+Recv[3]+Recv[4];
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
				catch(Exception r){
						// s.close();
					System.out.println("Socket timed out!");
					r.printStackTrace();
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
