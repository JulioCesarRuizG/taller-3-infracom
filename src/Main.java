import Cliente.*;
import Servidor.*;
public class Main {
	
	public static void main(String[] args) {
		new Servidor().start();
		
		for(int j=0; j<25 ; j++)
		{
			new Cliente(j);
		}
		
		System.exit(0);
	}
}
