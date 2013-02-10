package clockSync.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class SyncClient {

	private Socket socket;
	private static final int port = 4444;
	
	public static void main(String[] args){
		SyncClient client = new SyncClient();
		String time = "";
		try {
			time = client.getCurrentTime();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Il server ha risposto con l'ora corrente: " + time);
	}
	
	public SyncClient(){
		
	}
	
	public String getCurrentTime() throws IOException{
        String fromServer = null;
		PrintWriter out = null;
        BufferedReader in = null;
        
		try {
			socket = new Socket("localhost", port);
			out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
        // send req
        
        out.println("REQUEST CURRENT TIME");
        fromServer = in.readLine();
        
        try {
        	out.close();
        	in.close();
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return fromServer;
	}
}

/*
import java.io.*;
import java.net.*;

public class KnockKnockClient {
    public static void main(String[] args) throws IOException {

        Socket kkSocket = null;
        PrintWriter out = null;
        BufferedReader in = null;

        try {
            kkSocket = new Socket("taranis", 4444);
            out = new PrintWriter(kkSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(kkSocket.getInputStream()));
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host: taranis.");
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to: taranis.");
            System.exit(1);
        }

        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
        String fromServer;
        String fromUser;

        while ((fromServer = in.readLine()) != null) {
            System.out.println("Server: " + fromServer);
            if (fromServer.equals("Bye."))
                break;
		    
            fromUser = stdIn.readLine();
	    if (fromUser != null) {
                System.out.println("Client: " + fromUser);
                out.println(fromUser);
	    }
        }

        out.close();
        in.close();
        stdIn.close();
        kkSocket.close();
    }
}
*/