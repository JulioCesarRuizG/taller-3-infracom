package Servidor;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

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
		AtomicInteger atom = new AtomicInteger(0);
		synchronized(atom){
			try {
				FileOutputStream output = new FileOutputStream(logFile);
				String name = archivo == 100? "f1":"f2";
				String message = "Name File:"+name+"Size:"+String.valueOf(archivo);
				output.write(message.getBytes(), 0, message.length());
				output.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
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
	public static void writeFile(String message,File file) throws IOException{
		if (file.exists()){
			Scanner myReader = new Scanner(file);
			String data = "";
			while (myReader.hasNextLine()) {
			  data += myReader.nextLine();
			}
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
	private int clientCounter;
	private int totalClientes;
	private File logFile;
	private Socket s;


	public Multi(Socket s, CyclicBarrier br, int archivo, int clientCounter, int totalClientes, File logFile) throws IOException{
		this.s =s;
	    Multi.barrera = br;
	    Multi.archivo = archivo;
	    output = s.getOutputStream();
	    this.clientCounter = clientCounter;
	    this.totalClientes = totalClientes;
	    this.logFile = logFile;
	}
	
	public void run(){
		try {

			FileInputStream input = null;
			int nbytes = 0;
			byte[] bytes = new byte[10*1024*1024];//10mMB to avoid OutOfMemoryError
			File file = null;
			String hash = null;
			MessageDigest ms =MessageDigest.getInstance("SHA-256");
				try {
					if(archivo == 100)
					{
						file = new File("assets\\Servidor\\f1");
						hash = Servidor.checksum(ms,file);
						input = new FileInputStream(file);
						nbytes = 100 * 1024 * 1024;
					}
					else {
						file = new File("assets\\Servidor\\f2");
						hash = Servidor.checksum(ms,file);
						input = new FileInputStream(file);
						nbytes = 250 * 1024 * 1024;
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
			int count;
			int totalCount = 0;
			while ((count = input.read(bytes)) > 0 && (totalCount += count) <= nbytes) {
				output.write(bytes, 0, count);
			}
		    long time2 = System.currentTimeMillis();

			input.close();
			barrera.await();
		    long total = time2-time1;
			Servidor.writeFile("\nCliente:"+clientCounter+" Verificacion:"+hash+"Tiempo:"+String.valueOf(total), this.logFile);
			AtomicInteger atom = new AtomicInteger(1);
			synchronized(atom){
				System.out.println("Se enviaron los archivos en tiempo: " + total + " Usuario " + clientCounter);
			}

		} catch (Exception e) {
		}
	}
} 

