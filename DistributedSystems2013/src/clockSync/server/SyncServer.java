package clockSync.server;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * Con questa classe, si vuole fornire un servizio di network clock syncronization.
 *
 */
public class SyncServer {
	
	private ServerSocket server;
	private boolean listening;
	public static final int port = 4444;
	
	public static void main(String[] args){
		SyncServer srv = new SyncServer();
		srv.run();
	}
	
	public SyncServer(){
		listening = true;
	}
	
	public void run(){
		try {
			server = new ServerSocket(port);
			System.out.println("Server up and listening on port " + port);
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

/*
import java.net.*;
import java.io.*;

public class KKMultiServer {
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = null;
        boolean listening = true;

        try {
            serverSocket = new ServerSocket(4444);
        } catch (IOException e) {
            System.err.println("Could not listen on port: 4444.");
            System.exit(-1);
        }

        while (listening)
	    new KKMultiServerThread(serverSocket.accept()).start();

        serverSocket.close();
    }
}

import java.net.*;
import java.io.*;

public class KKMultiServerThread extends Thread {
    private Socket socket = null;

    public KKMultiServerThread(Socket socket) {
	super("KKMultiServerThread");
	this.socket = socket;
    }

    public void run() {

	try {
	    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
	    BufferedReader in = new BufferedReader(
				    new InputStreamReader(
				    socket.getInputStream()));

	    String inputLine, outputLine;
	    KnockKnockProtocol kkp = new KnockKnockProtocol();
	    outputLine = kkp.processInput(null);
	    out.println(outputLine);

	    while ((inputLine = in.readLine()) != null) {
		outputLine = kkp.processInput(inputLine);
		out.println(outputLine);
		if (outputLine.equals("Bye"))
		    break;
	    }
	    out.close();
	    in.close();
	    socket.close();

	} catch (IOException e) {
	    e.printStackTrace();
	}
    }
}

*/