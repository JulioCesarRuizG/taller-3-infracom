package Cliente;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;


public class Cliente extends Thread{
	private int id;
	//Server
	private static final int PUERTO = 3400; //Puerto del servidor
	private static final String SERVIDOR = "157.253.217.216";
	private static final int CHUNKSIZE = 50;
	//file and log paths
	private static final String PATH = "assets/Cliente/" ;
	private static final String LOGPATH = "Logs/Cliente/";
	//file
	private int total;
	private long fileSize;
	private String fileName;
	//Streams and hash
	private FileOutputStream output;
	private String hash = "";

	
	
	public Cliente(int pid)
	{
		id=pid;
		this.start();
	}
	
	@Override
	public void run() {
		Socket socket = null;
		InputStream lector = null;
		OutputStream escritor = null;
		String serverHash = "";
		boolean FileCorrect = false;

		File logFile = new File(LOGPATH+"Cliente"+this.id+"-"+getDate()+"log.txt");

		try {	
			socket = new Socket(SERVIDOR, PUERTO);
			lector = socket.getInputStream();
			escritor = socket.getOutputStream();
			
	        while(lector.available() == 0){}
			
		    long time1 = System.currentTimeMillis();
			//Metadata
			DataInputStream intagerRecive = new DataInputStream(socket.getInputStream());
			// Recive the total of the client
			this.total = intagerRecive.readInt();
			// Recive the id of the client
			this.id = intagerRecive.readInt();
			// Recive the total size of the file
			this.fileSize = intagerRecive.readLong();
			// Recive the hash of the file
			serverHash = intagerRecive.readUTF();
			// Recive the name of the file
			this.fileName = intagerRecive.readUTF();
			
			int sizeMB = (int)fileSize/(1024*1024);
			writeLog("Name File:"+this.fileName+" Size:"+String.valueOf(sizeMB)+"MB", logFile);
			//Reads file 
			File file = new File(PATH+"Cliente"+id+"-Prueba"+total+".bin");
			output = new FileOutputStream(file);
			readFile(CHUNKSIZE, lector, output, fileSize);
				//Thread.sleep(200);
			output.close();

			//Hashing to server
			MessageDigest ms =MessageDigest.getInstance("SHA-256");
			this.hash = checksum(ms,file);
			byte bhash[] = this.hash.getBytes();
			escritor.write(bhash, 0, this.hash.length());
			
		    long time2 = System.currentTimeMillis();
		    long totalTime = time2-time1;

			FileCorrect = this.hash.equals(serverHash) ? true : false;
			writeLog("Cliente:"+this.id+"\tVerificacion:"+FileCorrect+"\tServer:"+serverHash+"\tCient:"+this.hash+"\tTiempo:"+String.valueOf(totalTime), logFile);
			//close Streams
			escritor.close();
			lector.close();
			socket.close();
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

	public void readFile(int chunkSize, InputStream lector, OutputStream output,long fileSize )throws Exception{
		byte[] chunks = new byte[chunkSize*1024*1024];//50MB
		int count = 0;
		long total = 0;
		while (total < fileSize) {
			synchronized(lector){
				count = lector.read(chunks);
			}
			output.write(chunks, 0, count);
			total += count;
		}
		// System.out.println("[Client] FILE END");
	}

	synchronized private static String checksum(MessageDigest digest,File file)throws IOException 
    {
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