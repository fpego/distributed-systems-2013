package clockSync.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class SyncServerThread extends Thread{
	public static final char SEPARATOR = ':';
	
	private Socket client = null;
	private long responseTime;
	private long currentTime;
	private long elapsedTime;
	
	private PrintWriter out;
	private BufferedReader in;
	
    public SyncServerThread(Socket socket) {
    	super("SyncServerThread");
    	responseTime = System.currentTimeMillis();
    	this.client = socket;
    }

    public void run() {
		try {
			System.out.println("Ricevuta una richiesta da " + client.getInetAddress());
			
			out = new PrintWriter(client.getOutputStream(), true);
		    in = new BufferedReader(new InputStreamReader(client.getInputStream()));
		    
		    String received = in.readLine();
		    System.out.println("Ricevuto: " + received);

		    if (received.equals("REQUEST CURRENT TIME")){
		    	currentTime = System.currentTimeMillis();
		    	elapsedTime = currentTime - responseTime;
		    	out.println(Long.toString(currentTime) + SEPARATOR + Long.toString(elapsedTime));
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
