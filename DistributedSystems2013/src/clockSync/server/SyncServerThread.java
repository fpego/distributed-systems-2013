package clockSync.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import clockSync.common.ClockSyncProtocol;

public class SyncServerThread extends Thread{
	private Socket client = null;
	private ClockSyncProtocol protocol;
	private long responseTime;
	private long currentTime;
	private long elapsedTime;
	private PrintWriter out;
	private BufferedReader in;
	
    public SyncServerThread(Socket socket, long responseTime) {
    	super("SyncServerThread");
    	protocol = new ClockSyncProtocol();
    	this.responseTime = responseTime;
    	this.client = socket;
    }

    public void run() {
		try {
			System.out.println("Ricevuta una richiesta da " + client.getInetAddress() + ":" + client.getPort());
			
			out = new PrintWriter(client.getOutputStream(), true);
		    in = new BufferedReader(new InputStreamReader(client.getInputStream()));
		    
		    String received = in.readLine();
		    System.out.println("Ricevuto: " + received);

		    if (received.equals(ClockSyncProtocol.REQ_SIMPLE) || received.equals(ClockSyncProtocol.REQ_FULL)){
		    	currentTime = System.currentTimeMillis();
		    	elapsedTime = System.nanoTime() - responseTime;
		    	out.println(protocol.simpleResponse(currentTime, elapsedTime));
		    }else{
		    	out.println("ERROR");
		    }
		    
		    out.close();
		    in.close();
			client.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
}
