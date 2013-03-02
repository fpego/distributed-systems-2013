package clockSync.server;

import java.io.IOException;
import java.net.ServerSocket;

import clockSync.common.ClockSyncProtocol;

/**
 * Classe del server di un servizio di network clock syncronization 
 */
public class SyncServer{
	
	// numero massimo di tentativi di registrazione del server su porte diverse
	private final int MAX_RETRIES = 10;
	// il SocketServer che ascolta sulla porta 4444
	private ServerSocket server;
	// la porta del socket corrente in ascolto
	private int current_port;
	// il thread che contiene il server in ascolto
	private Thread serverMainThread;
	
	public static void main(String[] args) throws InterruptedException{
		SyncServer s = new SyncServer();
		s.startServer();
		
		Thread.sleep(1000);
		s.stopServer();
	}
	
	public SyncServer(){ 
		current_port = ClockSyncProtocol.port;
	}
	
	/**
	 * Lancia il clock server. Il server rimane operativo fino a quando viene chiamato il metodo stopServer()
	 */
	public void startServer(){
		
		if (server != null){
			System.err.println("The server is already running on port " + current_port + "!");
			return;
		}
		
		for (int i = 0; i < MAX_RETRIES && server == null; i++){
			current_port += i;
			
			try {
				server = new ServerSocket(current_port);
				System.out.println("Server up and listening on port " + current_port);
			} catch (IOException e) {
				System.err.println("Impossible to register the server on port " + current_port + ", trying the next one...");
				server = null;
			}
			
			if (current_port < 4444 || current_port > 4454){
				break;
			}
		}
		if (server == null){
			System.out.println("Impossible to register the server on any port, the program will now exit.");
			return;
		}
		
		// questo thread contiene il SocketServer in ascolto dei client
		// ritorna da solo quando il server viene chiuso da stopServer()
		serverMainThread = new Thread(){
			public void run(){
				while (true){
					try {
					    new SyncServerThread(server.accept(), System.nanoTime()).start();
					} catch (IOException e) { 
						if (server == null){
							return;
						}
					}
				}
			}
		};
		
		serverMainThread.start();
		
	}

	/**
	 * Se il server sta funzionando, viene fermata la sua esecuzione.
	 */
	public void stopServer(){
		if (serverMainThread != null && serverMainThread.isAlive()){
			
			try {
				server.close();
			} catch (IOException e) {
			}

			server = null;
			
			try {
				serverMainThread.join();
			} catch (InterruptedException e) {	}
			
			serverMainThread = null;
			
			System.out.println("Server stopped.");
		}else{
			System.out.println("The server was not running!");
		}
	}
	
	/**
	 * Ritorna la porta che � stata utilizzata ora e sulla quale il server sta ascoltando i client.
	 * Se il server non � in esecuzione, ritorna la porta usata di default. 
	 */
	public int getPort(){
		return current_port;
	}
	
	/**
	 * Setta la porta sulla quale si mette in ascolto il server.
	 * Deve essere compresa tra 1024 e 65535 
	 */
	public void setPort(int port){
		if (port > 1024 && port < 65535)
			this.current_port = port;
	}
}
