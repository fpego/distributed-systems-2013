package clockSync.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import clockSync.common.ClockSyncProtocol;

/**
 * Classe del server di un servizio di network clock syncronization 
 */
public class SyncServer{
	
	// numero massimo di tentativi di registrazione del server su porte diverse
	private final int MAX_RETRIES = 10;
	private ServerSocket server;
	private int current_port;
	private Thread serverMainThread;
	
	public static void main(String[] args) throws InterruptedException{
		SyncServer s = new SyncServer();
		s.run();
		
		Thread.sleep(1000);
		s.stopServer();
	}
	public SyncServer(){ 
		current_port = ClockSyncProtocol.port;
	}
	
	/**
	 * Lancia il clock server. Il server rimane operativo fino a quando viene chiamato il metodo stopServer()
	 */
	public void run(){
		
		current_port = ClockSyncProtocol.port;
		
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
		}
		if (server == null){
			System.out.println("Impossible to register the server on any port, the program will now exit.");
			return;
		}
		
		serverMainThread = new Thread(){
			public void run(){
				Socket client;
				while (true){
					try {
						client = server.accept();
					    new SyncServerThread(client, System.nanoTime()).start();
					} catch (IOException e) { }
				}
			}
		};
		
		serverMainThread.start();
		
	}

	/**
	 * Se il server sta funzionando, viene fermata la sua esecuzione.
	 */
	@SuppressWarnings("deprecation")
	public void stopServer(){
		if (serverMainThread != null && serverMainThread.isAlive()){
			serverMainThread.stop();
			
			try {
				server.close();
				server = null;
			} catch (IOException e) {}
			
			System.out.println("Server stopped.");
		}
	}
	
	/**
	 * Ritorna la porta che � stata utilizzata ora e sulla quale il server sta ascoltando i client.
	 * Se il server non � in esecuzione, ritorna la porta usata di default. 
	 */
	public int getCurrentPort(){
		return current_port;
	}
}
