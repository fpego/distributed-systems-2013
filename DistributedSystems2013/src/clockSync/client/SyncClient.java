package clockSync.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Scanner;

import clockSync.common.ClockSyncProtocol;

/**
 * Classe client, si occupa di chiedere al server l'ora corrente.
 * E' possibile specificare l'host e il tipo di richiesta da effettuare (se "semplice" o "completa")
 * La richiesta di tipo "completo" effettua svariate richieste semplici, facendo la media del tempo di scarto 
 */
public class SyncClient {

	// massimo numero di chiamate al server effettuabili nella richiesta di tipo FULL
	public static final int MAX_REQUEST_NUMBER = 100;
	// numero di chiamate di default
	public static final int DEFAULT_REQUEST_NUMBER = 10;
	// server di default
	public static final String DEFAULT_SERVER = "localhost";
	// tempo (in millisecondi) tra le richieste nel caso FULL
	private final long SLEEP_TIME = 100;
	
	private String server;
	private String request_type;
	private int request_number_full;
	private ClockSyncProtocol protocol;
	private long currentTime;
	
	private Calendar calendar;
	private SimpleDateFormat date_format;
	
	public static void main(String[] args){
		SyncClient client = new SyncClient();
		Scanner scanner = new Scanner(System.in);
		
		System.out.print("Inserire l'ip del server (default: localhost): ");
		String server = scanner.nextLine();
		scanner.close();
		
		if (!server.equals("")){
			client.setServer(server);
			System.out.println("Settato come server \""+client.getServer()+"\".");
		}
		
		System.out.println("Data: " + client.getCurrentTimeAsString(0));
		
		System.out.println("Data: " + client.getCurrentTimeAsString(1));
		
		System.out.println("Data: " + client.getCurrentTimeAsString(20));
	}
	
	public SyncClient(){
		server = SyncClient.DEFAULT_SERVER;
		request_type = ClockSyncProtocol.REQ_SIMPLE;
		request_number_full = SyncClient.DEFAULT_REQUEST_NUMBER;
		protocol = new ClockSyncProtocol();
		calendar = new GregorianCalendar();
		date_format = new SimpleDateFormat("dd/MM/yyyy hh:mm ss SSS");
		currentTime = 0L;
	}
	
	/**
	 * Ritorna il tempo corrente letto dal server
	 * @param request_type: se == 0, si usa la richiesta di tipo semplice. Se == 1, si usa la richiesta completa. Se numero
	 * tra 2 e MAX_REQUEST_NUMBER, esegue richiesta di tipo FULL eseguendo un numero di chiamate al server pari al numero passato.
	 * @return tempo letto dal server o 0 in caso di fallimento
	 */
	public long getCurrentTime(int request_type){
		if (request_type == 0){
			this.request_type = ClockSyncProtocol.REQ_SIMPLE;
		}else if (request_type == 1){
			this.request_type = ClockSyncProtocol.REQ_FULL;
		}else if (request_type >= 2 && request_type <= SyncClient.MAX_REQUEST_NUMBER){
			this.setRequestNumber(request_type);
			this.request_type = ClockSyncProtocol.REQ_FULL;
		}
		
		getCurrentTime();
		
		return this.currentTime;
	}
	
	/**
	 * Come getCurrentTime(), ma ritorna il tempo come una stringa con il seguente formato: "dd/MM/yyyy hh:mm ss SSS"
	 * Se la richiesta non ha successo, ritorna la data del tempo 0, ovvero 01/01/1970 00:00 00 000
	 */
	public String getCurrentTimeAsString(int request_type){
		getCurrentTime(request_type);
		
		calendar.setTimeInMillis(currentTime);
	    return date_format.format(calendar.getTime());
	}
	
	/**
	 * Ritorna il tempo corrente letto dal server, come long, in millisecondi passati dal 1 gennaio 1970 (unix time).
	 * Se ci sono stati errori, ritorna 0.
	 * @param request_type: viene usato questo request type per la chiamata al server
	 * settato nella classe.
	 */
	private void getCurrentTime(){
		if (request_type.equals(ClockSyncProtocol.REQ_SIMPLE)){
			executeRequest();
		} else if (request_type.equals(ClockSyncProtocol.REQ_FULL)){
			// faccio N volte la richiesta semplice, poi ci pensa la classe del protocollo a fare la media tra i valori
			checkRequestNumber();
			int current_iteration;
			for (current_iteration = 0; current_iteration < this.request_number_full; current_iteration++){
				executeRequest();
				if (this.server == null){
					currentTime = 0L;
					return;
				}
				try {
					Thread.sleep(SLEEP_TIME);
				} catch (InterruptedException e) {	}
		    }
			currentTime = protocol.getFullResponse();
		}
	}
	
	/**
	 * Esegue una singola chiamata al server per ottenere l'ora corrente e la salva nella variabile di classe currentTime
	 */
	private void executeRequest(){
		Socket socket = null;
        String fromServer = null;
		PrintWriter out = null;
        BufferedReader in = null;
        long startTime = 0, endTime = 0;
        
        // azzero il tempo ritornato, cos� sono sicuro di non ritornare un altra data
        this.currentTime = 0L;
        
        if (server == null){
			System.err.println("Server is NULL, cannot create the socket.");
			return;
		}
        
		try {
			socket = new Socket(server, ClockSyncProtocol.port);
			out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		} catch (Exception e) {
			System.out.println("Server unknown: " + server);
			server = null;
			return;
		}
		
		try{
			startTime = System.nanoTime();
			out.println(this.request_type);
	        fromServer = in.readLine();
	        endTime = System.nanoTime();
				
	        this.currentTime = this.protocol.parseResponse(fromServer, this.request_type, endTime - startTime);
		}catch (Exception e){
			e.printStackTrace();
		}
        
        try {
        	out.close();
        	in.close();
			socket.close();
		} catch (IOException e) { }
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
	
	public void setServer(String server){
		this.server = server;
	}
	
	public String getServer(){
		return server;
	}
}
