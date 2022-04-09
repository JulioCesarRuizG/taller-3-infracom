import java.io.IOException;
import java.util.Scanner;

import Cliente.*;
import Servidor.*;
public class Main {
	
	public static void main(String[] args) throws IOException, InterruptedException {
		Scanner sc = new Scanner(System.in);
		String tipo = "";
		while(!tipo.equals("servidor") && !tipo.equals("cliente"))
		{
			System.out.println("Ingrese el tipo de servicio que desea ejecutar(cliente o servidor) :");
			tipo = sc.nextLine();
		}
		System.out.println("Ingrese la cantidad de clientes:");
		int clientes = sc.nextInt();
		if(tipo.equals("servidor")){
			int archivo = 0;
			while(archivo != 100 && archivo != 250)
			{
				System.out.println("Ingrese el archivo a usar (100 o 250):");
				archivo = sc.nextInt();
			}
			new Servidor(archivo, clientes).start();
		}else{
			for(int j=0; j<clientes ; j++)
			{
				// Thread.sleep(200);
				new Cliente(j);
			}
		}


		

		sc.close();
	}
}


