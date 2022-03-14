package Cliente;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;


public class Cliente extends Thread{
	private static final int PUERTO = 3400; //Puerto del servidor
	private static final String SERVIDOR = "localhost";
	private int id;
	
	
	public Cliente(int pid)
	{
		id=pid;
		this.start();
	}
	
	@Override
	public void run() {
		// System.out.print("CIENTE");
		long startTime = System.nanoTime();
		Socket socket = null;
		PrintWriter escritor = null;
		BufferedReader lector = null;
		try {
			
			try {
			
				socket = new Socket(SERVIDOR, PUERTO);
				escritor = new PrintWriter(socket.getOutputStream(), true);
				lector = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			
			//implementar comunicaci�n
			
			
			escritor.close();
			lector.close();
			socket.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}