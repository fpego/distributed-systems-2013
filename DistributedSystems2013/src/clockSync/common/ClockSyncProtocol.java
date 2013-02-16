package clockSync.common;

/**
 * Classe comune, implementa il protocollo di comunicazione tra client e server
 */
public class ClockSyncProtocol {
	
	public final static int port = 4444;
	
	public final static String REQ_SIMPLE = "REQ SIMPLE SYNC";
	public final static String REQ_FULL = "REQ FULL SYNC";
	public final static String SEPARATOR = ":";
	
	
	public ClockSyncProtocol(){	}
	
	public String simpleResponse(long currentTime, long elapsedTime){
		return Long.toString(currentTime) + SEPARATOR + Long.toString(elapsedTime);
	}
	
	public String fullResponse(){
		return "TO IMPLEMENT";
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
	public static long parseResponse(String fromServer, String type, long clientInterruptTime) {
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
			
		}
		return 0;
	}
}
