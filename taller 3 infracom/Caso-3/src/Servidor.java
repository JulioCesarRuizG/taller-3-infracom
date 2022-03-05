import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class Servidor extends Thread{

	private static final int PUERTO = 3400; //Puerto
	
	@Override
	public void run() {
		
		
		try {
			ServerSocket ss = null;
			boolean seguir = true;
			
			try {
				ss = new ServerSocket(PUERTO);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			while(seguir)
			{
				Socket socket = ss.accept();
				PrintWriter escritor = new PrintWriter(socket.getOutputStream(), true);
				BufferedReader lector = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				
				//implementar
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}