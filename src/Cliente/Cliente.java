package Cliente;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;


public class Cliente extends Thread{
	private int id;
	//Server
	private static final int PUERTO = 3400; //Puerto del servidor
	private static final String SERVIDOR = "192.168.0.5";
	private static final int CHUNKSIZE = 64;
	//file and log paths
	private static final String PATH = "assets/Cliente/" ;
	private static final String LOGPATH = "Logs/Cliente/";
	//file
	private int total;
	private long fileSize;
	private String fileName;
	//Streams and hash
	private FileOutputStream output;

	
	
	public Cliente(int pid)
	{
		id=pid;
		this.start();
	}
	
	@Override
	public void run() {
		
		try {
			DatagramSocket socket = new DatagramSocket();
			socket.connect(InetAddress.getByName(SERVIDOR), PUERTO);
			DatagramPacket request = new DatagramPacket(new byte[1], 1);
			socket.send(request);
			System.out.println("El estado del cliente es: " + socket.isConnected());

			// Thread.sleep(300);
			
	        // while(lector.available() == 0){}
			
		    long time1 = System.currentTimeMillis();
			// Metadata
			byte[] MetaBytes = new byte[Integer.SIZE*2+Long.SIZE*2];
        	ByteArrayInputStream byteIn = new ByteArrayInputStream(MetaBytes);
			DataInputStream MetaInput = new DataInputStream(byteIn);
			DatagramPacket MetaResponse = new DatagramPacket(MetaBytes, MetaBytes.length);
			socket.receive(MetaResponse);
			// Recive the total of the client
			this.total = MetaInput.readInt();
			// Recive the id of the client
			this.id = MetaInput.readInt();
			// Recive the total size of the file
			this.fileSize = MetaInput.readLong();
			// Recive the name of the file
			this.fileName = MetaInput.readUTF();
			
			int sizeMB = (int)fileSize/1;
			
			File logFile = new File(LOGPATH+"Cliente"+this.id+"-"+getDate()+"log.txt");
			writeLog("Name File:"+this.fileName+" Size:"+String.valueOf(sizeMB)+"B", logFile);
			//Reads file 
			File file = new File(PATH+"Cliente"+id+"-Prueba"+total+".bin");
			output = new FileOutputStream(file);
			
			readFile(CHUNKSIZE, socket, output, fileSize);
			//Thread.sleep(200);
			
			output.close();
			//close Streams
			socket.close();
		    long time2 = System.currentTimeMillis();
		    long totalTime = time2-time1;

			writeLog("Cliente:"+this.id+"\tTiempo:"+String.valueOf(totalTime), logFile);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static String getDate(){
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");  
		Date date = new Date();  
		String strdate = String.valueOf(formatter.format(date)); 
		return strdate; 
	}

	public static void writeLog(String message,File file) throws IOException{
		if (file.exists()){
			Scanner myReader = new Scanner(file);
			String data = "";
			while (myReader.hasNextLine()) {
			  data = data+myReader.nextLine()+"\n";
			}
			myReader.close();
			FileOutputStream output = new FileOutputStream(file);
			output.write((data+message).getBytes(), 0,message.length()+data.length());
			output.close();
		}else{
			FileOutputStream output = new FileOutputStream(file);
			output.write(message.getBytes(), 0, message.length());
			output.close();
		}
	}

	public void readFile(int chunkSize, DatagramSocket socket, OutputStream output,long fileSize )throws Exception{
		// byte[] chunks = new byte[chunkSize*1024*1024];//50MB
		byte[] chunks = new byte[chunkSize*1024];
		int count = 0;
		// t1
		//socket.setSoTimeout(1000);
		boolean ending = false;
		byte[] END = "END".getBytes();
		try{
			while (!ending) {
			DatagramPacket response = new DatagramPacket(chunks, chunks.length);
			socket.receive(response);
			if( response.getLength() == 3 || chunks.equals(END)){ending = true;}
			count = response.getLength();
			output.write(chunks, 0, count);
			}
			System.out.println("[Cliente"+this.id+"] FILE END");
		}
		catch(Exception e){
		}

	}
}