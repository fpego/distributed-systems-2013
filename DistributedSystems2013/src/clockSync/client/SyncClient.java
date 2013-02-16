package clockSync.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Date;

import clockSync.common.ClockSyncProtocol;

public class SyncClient {

	private String host;
	private String request_type;
	
	public static void main(String[] args){
		SyncClient client = new SyncClient();
		long time = client.getCurrentTime();
		System.out.println("Il server ha risposto con l'ora corrente: " + time);
		
		Date d = new Date(time);
		System.out.println("Data: " + d.toString());
	}
	
	public SyncClient(){
		host = "localhost";
		request_type = ClockSyncProtocol.REQ_SIMPLE;
	}
	
	/**
	 * Ritorna il tempo corrente letto dal server
	 * @param request_type: se == 0, si usa la richiesta di tipo semplice. Se == 1, si usa la richiesta completa. Se numero
	 * non valido, ritorna 0.
	 * @return tempo letto dal server
	 */
	public long getCurrentTime(int request_type){
		if (request_type == 0)
			return this.getCurrentTime(ClockSyncProtocol.REQ_SIMPLE);
		else if (request_type == 1)
			return this.getCurrentTime(ClockSyncProtocol.REQ_FULL);
		
		return 0;
	}
	
	/**
	 * Ritorna il tempo corrente letto dal server utilizzando il tipo di richiesta settato nella classe.
	 * @return
	 */
	public long getCurrentTime(){
		return this.getCurrentTime(this.request_type);
	}
	
	/**
	 * Ritorna il tempo corrente letto dal server, come long, in millisecondi passati dal 1 gennaio 1970 (unix time).
	 * Se ci sono stati errori, ritorna 0.
	 * @param request_type: viene usato questo request type per la chiamata al server
	 * settato nella classe.
	 */
	private long getCurrentTime(String request_type){
		Socket socket = null;
        String fromServer = null;
		PrintWriter out = null;
        BufferedReader in = null;
        long startTime = 0, endTime = 0;
        //long serverTime = 0, serverInterrutpTime = 0;
        long currentTime = 0;
        
		try {
			socket = new Socket(host, ClockSyncProtocol.port);
			out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		} catch (UnknownHostException e) {
			System.out.println("Server unkwnoun: " + host);
			return 0;
		} catch (IOException e) {
			e.printStackTrace();
			return 0;
		}
		
		try{
			if (request_type.equals(ClockSyncProtocol.REQ_SIMPLE)){
				startTime = System.nanoTime();
				out.println(ClockSyncProtocol.REQ_SIMPLE);
		        fromServer = in.readLine();
		        endTime = System.nanoTime();
				
		        currentTime = ClockSyncProtocol.parseResponse(fromServer, ClockSyncProtocol.REQ_SIMPLE, endTime - startTime);
			} else if (request_type.equals(ClockSyncProtocol.REQ_FULL)){
				//TODO implement
			}
	        /*
			String[] parts = fromServer.split(ClockSyncProtocol.SEPARATOR);
			if (parts.length != 2){
				System.err.println("Parts non ha dimensione 2 ma " + parts.length);
				return 0;
			}
			
			serverTime = Long.parseLong(parts[0]);
			serverInterrutpTime = Long.parseLong(parts[1]);
			
			System.out.println("Start Time: "+ startTime);
			System.out.println("End Time: "+ endTime);
			System.out.println("Server Time: "+ serverTime);
			System.out.println("Server Interrupt Time: "+ serverInterrutpTime);
			System.out.println("endTime - startTime - serverInterrutpTime: "+ (endTime - startTime - serverInterrutpTime));
			
			currentTime = serverTime;
			
			if ((endTime - startTime - serverInterrutpTime) > 0)
				currentTime += (endTime - startTime - serverInterrutpTime) / 2000;
			*/
		}catch (Exception e){
			e.printStackTrace();
		}
        
        try {
        	out.close();
        	in.close();
			socket.close();
		} catch (IOException e) { }
		
		return currentTime;
	}
	
	public void setRequestSimple(){
		this.request_type = ClockSyncProtocol.REQ_SIMPLE;
	}
	
	public void setRequestFull(){
		this.request_type = ClockSyncProtocol.REQ_FULL;
	}
	
	public String getRequest(){
		if (this.request_type.equals(ClockSyncProtocol.REQ_SIMPLE))
			return "SIMPLE";
		else
			return "FULL";
	}
}
