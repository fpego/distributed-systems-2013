package clockSync.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import clockSync.common.ClockSyncProtocol;

/**
 * Istanza del server che risponde effettivamente alla chiamata di un client 
 */
public class SyncServerThread extends Thread{
	private Socket client = null;
	private ClockSyncProtocol protocol;
	private long responseTime;
	private long currentTime;
	private long elapsedTime;
	private PrintWriter out;
	private BufferedReader in;
	private String received;
	
    public SyncServerThread(Socket socket, long responseTime) {
    	super("SyncServerThread");
    	protocol = new ClockSyncProtocol();
    	this.responseTime = responseTime;
    	this.client = socket;
    }

    public void run() {
		try {
			System.out.println("Request received from client " + client.getInetAddress() + ":" + client.getPort());
			
			out = new PrintWriter(client.getOutputStream(), true);
		    in = new BufferedReader(new InputStreamReader(client.getInputStream()));
		    
		    received = in.readLine();

		    if (received.equals(ClockSyncProtocol.REQ_SIMPLE) || received.equals(ClockSyncProtocol.REQ_FULL)){
		    	currentTime = System.currentTimeMillis();
		    	elapsedTime = System.nanoTime() - responseTime;
		    	out.println(protocol.simpleResponse(currentTime, elapsedTime));
		    }else{
		    	out.println("ERROR: REQUEST NOT VALID");
		    }
		    
		} catch (IOException e) {
			System.err.println("Comunication error: " + e.getLocalizedMessage());
		}
		
		try{
			out.close();
		}catch (Exception e){}
		
		try{
			in.close();
		}catch (Exception e){}
		
		try{
			client.close();
		}catch (Exception e){}
    }
}
