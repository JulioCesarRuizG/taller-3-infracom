package Cliente;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;


public class Cliente extends Thread{
	private static final int PUERTO = 3400; //Puerto del servidor
	private static final String SERVIDOR = "localhost";
	private int id;
	private int archivo;
	private FileOutputStream output;
	
	
	public Cliente(int pid, int cantidad)
	{
		id=pid;
		archivo = cantidad;
		this.start();
	}
	
	@Override
	public void run() {
		Socket socket = null;
		InputStream lector = null;
		try {
			
			try {
				while(socket == null)
				{
					socket = new Socket(SERVIDOR, PUERTO);
				}
				lector = socket.getInputStream();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			File file = new File("recibido-"+id);
			output = new FileOutputStream(file);
			byte[] bytes = null;
			if(archivo == 100)
			{
				bytes = new byte[100 * 1024 * 1024];
			}
			else {
				bytes = new byte[250 * 1024 * 1024];
			}
			
	        int count;
	        while(lector.available() == 0) {
	        	
	        }
	        while ((count = lector.read(bytes)) > 0) {
				output.write(bytes, 0, count);
	        }
			
			
			lector.close();
			socket.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}