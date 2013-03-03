package clockSync.common;

import java.util.ArrayList;

/**
 * Classe comune, implementa il protocollo di comunicazione tra client e server
 */
public class ClockSyncProtocol {
	
	public final static int DEFAULT_PORT = 4444;
	
	public final static int MIN_PORT = 1024;
	public final static int MAX_PORT = 65535;
	
	public final static String REQ_SIMPLE = "REQ SIMPLE SYNC";
	public final static String REQ_FULL = "REQ FULL SYNC";
	public final static String SEPARATOR = ":";
	
	private ArrayList<Long> valori_full_response = null; 
	private long last_server_time;
	
	public ClockSyncProtocol(){	}
	
	/**
	 * Costruisce la risposta del server ad una richiesta di un client
	 * 
	 * @param currentTime: tempo corrente sul server, in millisecondi da Unix time
	 * @param elapsedTime: tempo intercorso sul server dalla ricezione della richiesta alla risposta
	 * @return
	 */
	public String simpleResponse(long currentTime, long elapsedTime){
		return Long.toString(currentTime) + SEPARATOR + Long.toString(elapsedTime);
	}
	
	/**
	 * A seconda del tipo di risposta richiesta al server, fornisce il tempo attuale restituito
	 * dal server
	 * 
	 * @param fromServer: risposta del server
	 * @param type: tipo di richiesta effettuata (simple o full)
	 * @param clientInterruptTime: tempo intercorso sul client tra la chiamata iniziale e la risposta dal server, in nanosecondi
	 * @return long contenente la data attuale in millisecondi da linux time
	 */
	public long parseResponse(String fromServer, String type, long clientInterruptTime) {
		if (type.equals(REQ_SIMPLE)){
			// la richiesta è formata dal tempo sul server, il separatore e l'intervallo di tempo impiegato dal server per
			// rispondere alla richiesta
			String[] parts = fromServer.split(SEPARATOR);
			if (parts.length != 2){
				return 0;
			}
			long serverTime = Long.parseLong(parts[0]);
			long serverInterrutpTime = Long.parseLong(parts[1]);
			long currentTime = serverTime;
			
			if ((clientInterruptTime - serverInterrutpTime) > 0)
				currentTime += (clientInterruptTime - serverInterrutpTime) / 2000;
			
			return currentTime;
		}else if (type.equals(REQ_FULL)){
			if (valori_full_response == null){
				valori_full_response = new ArrayList<Long>();
			}
			
			String[] parts = fromServer.split(SEPARATOR);
			if (parts.length != 2){
				return 0;
			}
			long serverTime = Long.parseLong(parts[0]);
			long serverInterrutpTime = Long.parseLong(parts[1]);
			
			this.last_server_time = serverTime;
			long diff = clientInterruptTime - serverInterrutpTime > 0 ? clientInterruptTime - serverInterrutpTime : 0L;
			valori_full_response.add(diff);
		}
		return 0L;
	}
	
	/**
	 * Eseguo la media tra i valori di valori_full_response, quindi lo aggiungo a last_server_time e lo ritorno
	 * @return
	 */
	public long getFullResponse(){
		if (valori_full_response != null && !valori_full_response.isEmpty()){
			long media = 0;
			for (int i = 0; i < valori_full_response.size(); i++){
				media += valori_full_response.get(i);
			}
			media = media / (valori_full_response.size() * 2000);
			// ora posso cancellare l'array
			valori_full_response.clear();
			return this.last_server_time + media;
		}
		return 0L;
	}

}
