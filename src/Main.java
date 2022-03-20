import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.CyclicBarrier;

import Cliente.*;
import Servidor.*;
public class Main {
	
	public static void main(String[] args) throws IOException, InterruptedException {
		Scanner sc = new Scanner(System.in);
		System.out.println("Ingrese la cantidad de clientes:");
		int clientes = sc.nextInt();
		int archivo = 0;
		while(archivo != 100 && archivo != 250)
		{
			System.out.println("Ingrese el archivo a usar (100 o 250):");
			archivo = sc.nextInt();
		}
        CyclicBarrier barrera = new CyclicBarrier(clientes);
		new Servidor(barrera, archivo, clientes).start();
		
		for(int j=0; j<clientes ; j++)
		{
			// Thread.sleep(200);
			new Cliente(j);
		}
		sc.close();
	}
}


