package Servidor;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import java.util.concurrent.CyclicBarrier;

public class Servidor extends Thread{
	//Conection
	private static final int PUERTO = 3400; //Puerto
	private static CyclicBarrier barrera;
	private static final String LOGPATH = "Logs\\";
	//Client
	private static int totalClients = 0;
	private static int clientCounter = 1;
	//file
	private int tipoArchivo;
	private String filename;
	private String hash;
	private File file;
	private long fileSize;

	
	public Servidor(CyclicBarrier br, int tipoArchivo, int totalClients) {
		barrera = br;
		Servidor.totalClients = totalClients;
		this.tipoArchivo = tipoArchivo;
	}
	public void run() {
        ServerSocket ss;
		//LOGS DATE
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");  
		Date date = new Date();  
		String strdate = String.valueOf(formatter.format(date));  
		File logFile = new File(LOGPATH+strdate+"log.txt");
		try {
			//File
			file = new File(tipoArchivo ==100? "assets\\Servidor\\f1":"assets\\Servidor\\f2");
			this.fileSize = file.length();
			this.filename = file.getName();
			MessageDigest ms =MessageDigest.getInstance("SHA-256");
			hash = Servidor.checksum(ms,file);

			//LOGS
			FileOutputStream output = new FileOutputStream(logFile);
			String message = "Name File:"+this.filename+" Size:"+String.valueOf(tipoArchivo)+"MB";
			output.write(message.getBytes(), 0, message.length());
			output.close();

		} catch (IOException | NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		
		try {
			ss = new ServerSocket(PUERTO);
			while(clientCounter <= 50){
				Socket s = ss.accept();
				//Threads
				System.out.println("Se recibe una conexion de cliente (numero "+clientCounter+")");
				Multi t=new Multi(s, barrera, this.file, this.fileSize, this.hash, clientCounter, totalClients, logFile);
				t.start();
				clientCounter++;
			}
			ss.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
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
	public static String checksum(MessageDigest digest,File file) throws IOException{
		// Get file input stream for reading the file
		// content
		FileInputStream fis = new FileInputStream(file);

		// Create byte array to read data in chunks
		byte[] byteArray = new byte[1024];
		int bytesCount = 0;

		// read the data from file and update that data in
		// the message digest
		while ((bytesCount = fis.read(byteArray)) != -1)
		{
		digest.update(byteArray, 0, bytesCount);
		};

		// close the input stream
		fis.close();

		// store the bytes returned by the digest() method
		byte[] bytes = digest.digest();

		// this array of bytes has bytes in decimal format
		// so we need to convert it into hexadecimal format

		// for this we create an object of StringBuilder
		// since it allows us to update the string i.e. its
		// mutable
		StringBuilder sb = new StringBuilder();

		// loop through the bytes array
		for (int i = 0; i < bytes.length; i++) {

		// the following line converts the decimal into
		// hexadecimal format and appends that to the
		// StringBuilder object
		sb.append(Integer
		.toString((bytes[i] & 0xff) + 0x100, 16)
		.substring(1));
		}

		// finally we return the complete hash
		return sb.toString();
	}
}

class Multi extends Thread{
	//threads
	private static CyclicBarrier barrera;
	//Streams
	private Socket s;
	OutputStream output;
	InputStream inputHash;
	//Files
	private File file = null;
	private long fileSize = 0;
	private String fileHash;
	private File logFile;
	//Clients
	private int clientCounter;
	private int totalClients;


	public Multi(Socket s, CyclicBarrier br, File file, long fileSize,String fileHash, int clientCounter, int totalClients, File logFile) throws IOException{
		this.s =s;
	    Multi.barrera = br;
	    output = s.getOutputStream();
	    inputHash = s.getInputStream();
	    this.file = file;
	    this.fileSize = fileSize;
	    this.fileHash = fileHash;
	    this.clientCounter = clientCounter;
	    this.totalClients = totalClients;
	    this.logFile = logFile;
	}
	
	public void run(){
		try {
			FileInputStream input = null;
			input = new FileInputStream(this.file);

		    System.out.println("Se esperan a los usuarios\t Usuario:" + clientCounter+"\tEsperando: "+barrera.getNumberWaiting()+"/"+totalClients);
			
			barrera.await();//se cuelga si llegan en desorden
		    System.out.println("Se procede a enviar los archivos");
		    long time1 = System.currentTimeMillis();

			//Metadata
				// Sends total amount of clients
				DataOutputStream intagerSend = new DataOutputStream(s.getOutputStream());
				intagerSend.writeInt(totalClients);
				// Sends the id of the client
				intagerSend.writeInt(clientCounter);
				// Sends the size of the file
				intagerSend.writeLong(this.fileSize);

			writeFile(input, 50, output);
			input.close();

		    long time2 = System.currentTimeMillis();
		    long total = time2-time1;

			//getHash from CLient
			byte bhash[] = new byte[this.fileHash.length()];
			inputHash.read(bhash, 0, this.fileHash.length());
			String clientHash = new String(bhash);
			// System.out.println("[Client"+clientCounter+"Hash]"clientHash);
			boolean GoodRead = clientHash.equals(this.fileHash) ? true : false;

			barrera.await();
			synchronized(this.logFile){
				System.out.println("Se enviaron los archivos a"+ " Usuario " + clientCounter+ " Tiempo: " + total );
				Servidor.writeLog("Cliente:"+clientCounter+"\tVerificacion:"+GoodRead+"\tServer:"+this.fileHash+"\tCient:"+clientHash+"\tTiempo:"+String.valueOf(total), this.logFile);
			}

		} catch (Exception e) {
			//
		}
	}

	synchronized public void writeFile(FileInputStream input, int chunkSize, OutputStream output) throws IOException{
		byte[] bytes = new byte[chunkSize*1024*1024];//NMB to avoid OutOfMemoryError
		int count;
		while ((count = input.read(bytes)) > 0) {
			output.write(bytes, 0, count);
		}
		// System.out.println("[Server] FILE END");
	}

} 

