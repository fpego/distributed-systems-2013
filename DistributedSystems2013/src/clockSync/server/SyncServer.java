package clockSync.server;

import java.io.IOException;
import java.net.ServerSocket;

import clockSync.common.ClockSyncProtocol;

/**
 * Con questa classe, si vuole fornire un servizio di network clock syncronization.
 * 
 * Per ulteriori informazioni, visitare la pagina di Wikipedia: @link{http://en.wikipedia.org/wiki/Cristian%27s_algorithm}
 *
 */
public class SyncServer {
	
	private ServerSocket server;
	private boolean listening;
	
	public static void main(String[] args){
		SyncServer srv = new SyncServer();
		srv.run();
	}
	
	public SyncServer(){
		listening = true;
	}
	
	public void run(){
		try {
			server = new ServerSocket(ClockSyncProtocol.port);
			System.out.println("Server up and listening on port " + ClockSyncProtocol.port);
			while (listening){
			    new SyncServerThread(server.accept()).start();
			}
			server.close();
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
	}

	public boolean isListening() {
		return listening;
	}

	public void setListening(boolean listening) {
		this.listening = listening;
	}
	
}
