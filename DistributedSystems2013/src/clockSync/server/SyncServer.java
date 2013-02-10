package clockSync.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Con questa classe, si vuole fornire un servizio di network clock syncronization.
 *
 */
public class SyncServer {
	
	private ServerSocket server;
	public static final int port = 4444;
	
	public SyncServer(){
		
	}
	
	public void run(){
		Socket client = null;
		
		try {
			server = new ServerSocket(port);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			client = server.accept();
			System.out.println(client.getInetAddress());
			client.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void close(){
		if (this.server != null){
			try{
				server.close();
			}catch (Exception e){
				e.printStackTrace();
			}
		}
			
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