package Servidor;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import java.util.concurrent.CyclicBarrier;

public class Servidor extends Thread{
	private static final String LOGPATH = "Logs\\" ;
	private static final int PUERTO = 3400; //Puerto
	private static CyclicBarrier barrera;
	private static int archivo;
	private static int totalClientes = 0;
	private static int clientCounter = 1;
	
	public Servidor(CyclicBarrier br, int archivo, int totalClientes) {
		barrera = br;
		Servidor.archivo = archivo;
		Servidor.totalClientes = totalClientes;
	}
	public void run() {
        ServerSocket ss;
		//LOGS
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");  
		Date date = new Date();  
		String strdate = String.valueOf(formatter.format(date));  
		File logFile = new File(LOGPATH+strdate+"log.txt");
		try {
			FileOutputStream output = new FileOutputStream(logFile);
			String name = archivo == 100? "f1":"f2";
			String message = "Name File:"+name+" Size:"+String.valueOf(archivo)+"MB";
			output.write(message.getBytes(), 0, message.length());
			output.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		while(true){
			try {
				ss = new ServerSocket(PUERTO);
		        Socket s=ss.accept();
				//Threads
				System.out.println("Se recibe una conexion de cliente (numero "+clientCounter+")");
		        Multi t=new Multi(s, barrera, archivo, clientCounter, totalClientes, logFile);
		        t.start();
		        ss.close();
		        clientCounter++;
			} catch (IOException e) {
				e.printStackTrace();
			}
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
	private static CyclicBarrier barrera;
	private static int archivo;
	OutputStream output;
	InputStream inputHash;
	private int clientCounter;
	private int totalClientes;
	private File logFile;
	private Socket s;


	public Multi(Socket s, CyclicBarrier br, int archivo, int clientCounter, int totalClientes, File logFile) throws IOException{
		this.s =s;
	    Multi.barrera = br;
	    Multi.archivo = archivo;
	    output = s.getOutputStream();
	    inputHash = s.getInputStream();
	    this.clientCounter = clientCounter;
	    this.totalClientes = totalClientes;
	    this.logFile = logFile;
	}
	
	public void run(){
		try {
			FileInputStream input = null;
			File file = null;
			long FileSize = 0;
			String hash = null;
			MessageDigest ms =MessageDigest.getInstance("SHA-256");
				try {
					if(archivo == 100)
					{
						file = new File("assets\\Servidor\\Prueba.txt");
						FileSize = file.length();
						hash = Servidor.checksum(ms,file);
						input = new FileInputStream(file);
					}
					else {
						file = new File("assets\\Servidor\\f2");
						FileSize = file.length();
						hash = Servidor.checksum(ms,file);
						input = new FileInputStream(file);
					}
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
		    System.out.println("Se esperan a los usuarios\t Usuario:" + clientCounter+"\tEsperando: "+barrera.getNumberWaiting()+"/"+totalClientes);
			barrera.await();//se cuelga si llegan en desorden
		    System.out.println("Se procede a enviar los archivos");
		    long time1 = System.currentTimeMillis();
			// Sends total amount of clients
			DataOutputStream intagerSend = new DataOutputStream(s.getOutputStream());
			intagerSend.writeInt(totalClientes);
			// Sends the id of the client
			intagerSend.writeInt(clientCounter);
			intagerSend.writeLong(FileSize);
			writeFile(input, 50, output);
			input.close();
		    long time2 = System.currentTimeMillis();
			byte bhash[] = new byte[hash.length()];
			inputHash.read(bhash, 0, hash.length());
			String clientHash = new String(bhash);
			System.out.println(clientHash+ " [HAPPY]");
			boolean GoodRead = clientHash.equals(hash) ? true : false;
			barrera.await();
		    long total = time2-time1;
			synchronized(this.logFile){
				System.out.println("Se enviaron los archivos a"+ " Usuario " + clientCounter+ " Tiempo: " + total );
				Servidor.writeLog("Cliente:"+clientCounter+"\tVerificacion:"+GoodRead+"\tServer:"+hash+"\tCient:"+clientHash+"\tTiempo:"+String.valueOf(total), this.logFile);
			}

		} catch (Exception e) {
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

