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

	// massimo numero di chiamate al server effettuabili nella richiesta di tipo FULL
	public static final int MAX_REQUEST_NUMBER = 100;
	// numero di chiamate di default
	public static final int DEFAULT_REQUEST_NUMBER = 10;
	// server di default
	public static final String DEFAULT_SERVER = "localhost";
	
	private String host;
	private String request_type;
	private int request_number_full;
	
	public static void main(String[] args){
		SyncClient client = new SyncClient();
		long time = client.getCurrentTime(1);
		System.out.println("Il server ha risposto con l'ora corrente: " + time);
		
		Date d = new Date(time);
		System.out.println("Data: " + d.toString());
	}
	
	public SyncClient(){
		host = SyncClient.DEFAULT_SERVER;
		request_type = ClockSyncProtocol.REQ_SIMPLE;
		request_number_full = SyncClient.DEFAULT_REQUEST_NUMBER;
	}
	
	/**
	 * Ritorna il tempo corrente letto dal server
	 * @param request_type: se == 0, si usa la richiesta di tipo semplice. Se == 1, si usa la richiesta completa. Se numero
	 * tra 2 e MAX_REQUEST_NUMBER, esegue richiesta di tipo FULL eseguendo un numero di chiamate al server pari al numero passato.
	 * @return tempo letto dal server
	 */
	public long getCurrentTime(int request_type){
		if (request_type == 0)
			return this.getCurrentTime(ClockSyncProtocol.REQ_SIMPLE);
		else if (request_type == 1)
			return this.getCurrentTime(ClockSyncProtocol.REQ_FULL);
		else if (request_type >= 2 && request_type <= SyncClient.MAX_REQUEST_NUMBER){
			int current_request_number = this.request_number_full;
			this.setRequestNumber(request_type);
			long time = this.getCurrentTime(ClockSyncProtocol.REQ_FULL);
			this.setRequestNumber(current_request_number);
			return time;
		}
			
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
        ClockSyncProtocol protocol = new ClockSyncProtocol();
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
				
		        currentTime = protocol.parseResponse(fromServer, ClockSyncProtocol.REQ_SIMPLE, endTime - startTime);
			} else if (request_type.equals(ClockSyncProtocol.REQ_FULL)){
				// faccio N volte la richiesta semplice, poi ci pensa la classe del protocollo a fare la media tra i valori
				this.checkRequestNumber();
				int current_iteration;
				for (current_iteration = 0; current_iteration < this.request_number_full; current_iteration++){
					if (current_iteration != 0){
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
					}
					
					startTime = System.nanoTime();
					out.println(ClockSyncProtocol.REQ_FULL);
			        fromServer = in.readLine();
			        endTime = System.nanoTime();
			        System.out.println(current_iteration + ") Risposta: " + fromServer);
			        protocol.parseResponse(fromServer, ClockSyncProtocol.REQ_FULL, endTime - startTime);
			        Thread.sleep(100);
			        
			        if (current_iteration != request_number_full - 1){
			        try {
			        	out.close();
			        	in.close();
						socket.close();
					} catch (IOException e) { }
			        }
			    }
				currentTime = protocol.getFullResponse();
			}
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
	
	/**
	 * Controlla che il numero di richieste da eseguire sia accettabile, ovvero tra 1 e MAX_REQUEST_NUMBER
	 * Se out of bounds, lo setta a default
	 */
	private void checkRequestNumber(){
		if (this.request_number_full < 1 || this.request_number_full > SyncClient.MAX_REQUEST_NUMBER){
			this.request_number_full = SyncClient.MAX_REQUEST_NUMBER;
		}
	}
	
	/**
	 * Setta il numero di richieste fatte al server dal tipo di richiesta FULL. Controlla che sia un numero ammissibile
	 * @param request_number_full
	 */
	public void setRequestNumber(int request_number_full){
		this.request_number_full = request_number_full;
		this.checkRequestNumber();
	}
	
	public int getRequestNumber(){
		return this.request_number_full;
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
	
	public void setHost(String host){
		this.host = host;
	}
}
