package Servidor;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

import Servidor.Servidor.ClientSocket;

public class Servidor extends Thread{
	//Conection
	private static final int PUERTO = 3400; //Puerto
	private static final String LOGPATH = "Logs/Servidor/";
	//Client
	private static int totalClients = 0;
	private static int clientCounter = 1;
	//file
	private int tipoArchivo;
	private String fileName;
	private File file;
	private long fileSize;

	class ClientSocket{
		InetAddress address;
		int port;
		
		public ClientSocket (DatagramPacket request) {
			this.address = request.getAddress();
			this.port = request.getPort();
		}
		
	}
	public Servidor(int tipoArchivo, int totalClients) {
		Servidor.totalClients = totalClients;
		this.tipoArchivo = tipoArchivo;
	}
	public void run() {
		//LOGS DATE
		File logFile = new File(LOGPATH+getDate()+"log.txt");
		try {
			//File and Hash
			file = new File(tipoArchivo ==100? "assets/Servidor/f1":"assets/Servidor/f2");
			this.fileSize = file.length();
			this.fileName = file.getName();

			//prueba
			//LOGS
			FileOutputStream logOutput = new FileOutputStream(logFile);
			String message = "Name File:"+this.fileName+" Size:"+String.valueOf(tipoArchivo)+"MB";
			logOutput.write(message.getBytes(), 0, message.length());
			logOutput.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
		Multi threads[] = new Multi[totalClients];
		try {
			DatagramSocket ds = new DatagramSocket(PUERTO);
			boolean listening = true;
			while(listening){
				DatagramPacket request = new DatagramPacket (new byte [1], 1);
				ds.receive(request);
				ClientSocket cs = new ClientSocket(request);
				//Threads
				System.out.println("Se recibe una conexion de cliente (numero "+clientCounter+")");
				Multi thread=new Multi(cs, ds, this.file, this.fileSize,this.fileName, clientCounter, totalClients, logFile);
				threads[clientCounter-1] = thread;
				thread.start();
				if(clientCounter == totalClients){
					listening = false;
				}
				clientCounter++;
			}
			for (Multi thread : threads) {
				thread.join();
			}

		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}
	// Appends information to file
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
	public static String getDate(){
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");  
		Date date = new Date();  
		String strdate = String.valueOf(formatter.format(date)); 
		return strdate; 
	}
}

class Multi extends Thread{
	//threads
	//Streams
	private ClientSocket cs;
	private DatagramSocket ds;
	//Files
	private File file = null;
	private long fileSize = 0;
	private String fileName;
	private File logFile;
	//Clients
	private int clientCounter;
	private int totalClients;
	private static final int CHUNKSIZE = 64;


	public Multi(ClientSocket cs, DatagramSocket ds, File file, long fileSize,String fileName, int clientCounter, int totalClients, File logFile) throws IOException{
		this.cs =cs;
		this.ds =ds;
	    this.file = file;
	    this.fileSize = fileSize;
	    this.fileName = fileName;
	    this.clientCounter = clientCounter;
	    this.totalClients = totalClients;
	    this.logFile = logFile;
	}
	
	public void run(){
		try {
			FileInputStream input = null;
			input = new FileInputStream(this.file);

		    System.out.println("Se esperan a los usuarios\t Usuario:" + clientCounter+"\tEsperando: "+this.clientCounter+"/"+totalClients);
			
		    System.out.println("Se procede a enviar los archivos");
		    long time1 = System.currentTimeMillis();

			//Metadata
				ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
				DataOutputStream intagerSend = new DataOutputStream(byteOut);
				// Sends total amount of clients
				intagerSend.writeInt(totalClients);
				// Sends the id of the client
				intagerSend.writeInt(clientCounter);
				// Sends the size of the file
				intagerSend.writeLong(this.fileSize);
				// Sends the file name
				intagerSend.writeUTF(this.fileName);
				byte[] MetaBytes = byteOut.toByteArray();
				byteOut.close();
				DatagramPacket MetaResponse = new DatagramPacket(MetaBytes, MetaBytes.length, this.cs.address, this.cs.port);
				ds.send(MetaResponse);

			writeFile(input, CHUNKSIZE, this.ds, this.cs);
			input.close();
			// ds.close();
		    long time2 = System.currentTimeMillis();
		    long total = time2-time1;
			
			synchronized(this.logFile){
				System.out.println("Se enviaron los archivos a"+ " Usuario " + clientCounter+ " Tiempo: " + total );
				Servidor.writeLog("Cliente:"+clientCounter+"\tTiempo:"+String.valueOf(total), this.logFile);
			}
		} catch (Exception e) {
			//
		}
	}

	synchronized public void writeFile(FileInputStream input, int chunkSize, DatagramSocket ds, ClientSocket cs) throws IOException{
		byte[] bytes = new byte[1024];//NKB to avoid OutOfMemoryError
		
		while (input.read(bytes) > 0) {
			DatagramPacket response = new DatagramPacket(bytes, bytes.length, cs.address, cs.port);
			ds.send(response);
			// String aux = new String(bytes, 0, bytes.length);
			// System.out.println("[SERVER]"+aux);
		}
		byte[] END = "END".getBytes();
		DatagramPacket endPacket = new DatagramPacket(END, END.length, cs.address, cs.port);
		ds.send(endPacket);
		System.out.println("[Server] FILE END");
	}

} 

