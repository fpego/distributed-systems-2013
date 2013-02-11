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
	
    public SyncServerThread(Socket socket) {
    	super("SyncServerThread");
    	responseTime = System.currentTimeMillis();
    	protocol = new ClockSyncProtocol();
    	this.client = socket;
    }

    public void run() {
		try {
			System.out.println("Ricevuta una richiesta da " + client.getInetAddress() + ":" + client.getPort());
			
			out = new PrintWriter(client.getOutputStream(), true);
		    in = new BufferedReader(new InputStreamReader(client.getInputStream()));
		    
		    String received = in.readLine();
		    System.out.println("Ricevuto: " + received);

		    if (received.equals(ClockSyncProtocol.REQ_SIMPLE)){
		    	currentTime = System.currentTimeMillis();
		    	elapsedTime = currentTime - responseTime;
		    	out.println(protocol.simpleResponse(currentTime, elapsedTime));
		    }else if (received.equals(ClockSyncProtocol.REQ_FULL)){
		    	out.println(protocol.fullResponse());
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
