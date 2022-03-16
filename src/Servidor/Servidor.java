package Servidor;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class Servidor extends Thread{

	private static final int PUERTO = 3400; //Puerto
	private static CyclicBarrier barrera;
	private static int archivo;
	private static int cantidad = 1;
	
	public Servidor(CyclicBarrier br, int archivo) {
		this.barrera = br;
		this.archivo = archivo;
	}
	
	public void run() {
        ServerSocket ss;
		while(true){
			try {
				ss = new ServerSocket(PUERTO);
		        Socket s=ss.accept();
		        System.out.println("Se recibe una conexión de cliente (número "+cantidad+ ")");
		        Multi t=new Multi(s, barrera, archivo, cantidad);
		        cantidad ++;
		        t.start();
		        
		        ss.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
	 }
	}
}

class Multi extends Thread{
	private static CyclicBarrier barrera;
	private static int archivo;
	OutputStream output;
	private int cantidad;


	public Multi() throws IOException{
	}
	
	public Multi(Socket s, CyclicBarrier br, int archivo, int cantidad) throws IOException{
	    this.barrera = br;
	    this.archivo = archivo;
	    output = s.getOutputStream();
	    this.cantidad = cantidad;
	}
	
	public void run(){
		try {

			FileInputStream input = null;
			byte[] bytes = null;
				try {
					if(archivo == 100)
					{
						File file = new File("f1");
						input = new FileInputStream(file);
						bytes = new byte[100 * 1024 * 1024];
					}
					else {
						File file = new File("f2");
						input = new FileInputStream(file);
						bytes = new byte[250 * 1024 * 1024];
					}
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
		    System.out.println("Se esperan a los usuarios (llegó " + cantidad+")");
			barrera.await();
		    System.out.println("Se procede a enviar los archivos");
		    long time1 = System.currentTimeMillis();
			int count;
			while ((count = input.read(bytes)) > 0) {
				output.write(bytes, 0, count);
			}
		    long time2 = System.currentTimeMillis();
			barrera.await();
		    long total = time2-time1;
		    System.out.println("Se enviaron los archivos en tiempo: " + total + "para el usuario " + cantidad);
		} catch (Exception e) {
		}
	}
} 