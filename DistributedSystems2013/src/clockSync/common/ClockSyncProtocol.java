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
	 * @return string contenente la data attuale
	 */
	public String parseResponse(String fromServer, String type) {
		if (type.equals(REQ_SIMPLE)){
			// la richiesta è formata dal tempo sul server, il separatore e l'intervallo di tempo impiegato dal server per
			// rispondere alla richiesta
			String[] parts = fromServer.split(SEPARATOR);
			if (parts.length != 2){
				return null;
			}
			
		}else if (type.equals(REQ_FULL)){
			
		}
		return null;
	}
}
