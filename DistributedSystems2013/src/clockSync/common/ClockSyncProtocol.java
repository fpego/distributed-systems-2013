package clockSync.common;

import java.util.ArrayList;

/**
 * Common class between SyncClient and SyncServer. Contains constants, messages
 * and methods to handle requests and responses.
 */
public class ClockSyncProtocol {

	/** Server default listening port */
	public final static int DEFAULT_PORT = 4444;

	public final static int MIN_PORT = 1024;
	public final static int MAX_PORT = 65535;

	public final static String REQ_SIMPLE = "REQ SIMPLE SYNC";
	public final static String REQ_FULL = "REQ FULL SYNC";
	public final static String SEPARATOR = ":";
	public static final String SERVER_ERROR_MESSAGE = "ERROR! Request not valid.";

	private ArrayList<Long> full_response_values = null;
	private long last_server_time;

	public ClockSyncProtocol() {
	}

	/**
	 * Builds the server response to a client
	 * 
	 * @param currentTime
	 *            current time on the server, in milliseconds since Unix time
	 * @param elapsedTime
	 *            time elapsed on the server serving the response
	 * @return
	 */
	public String simpleResponse(long currentTime, long elapsedTime) {
		return Long.toString(currentTime) + SEPARATOR
				+ Long.toString(elapsedTime);
	}

	/**
	 * Depending on the client's type of request, returns the current time
	 * obtained from the server.
	 * 
	 * @param fromServer
	 *            server response
	 * @param type
	 *            type of request (simple or full)
	 * @param clientInterruptTime
	 *            server interrupt time, in nanoseconds
	 * @return long current server time, in milliseconds
	 */
	public long parseResponse(String fromServer, String type,
			long clientInterruptTime) {
		long serverTime;
		long serverInterrutpTime;
		long currentTime;
		String[] parts = fromServer.split(SEPARATOR);
		if (parts.length != 2) {
			return 0L;
		}

		try {
			serverTime = Long.parseLong(parts[0]);
			serverInterrutpTime = Long.parseLong(parts[1]);
		} catch (Exception e) {
			return 0L;
		}

		if (type.equals(REQ_SIMPLE)) {

			currentTime = serverTime;
			if ((clientInterruptTime - serverInterrutpTime) > 0)
				currentTime += (clientInterruptTime - serverInterrutpTime) / 2000;

			return currentTime;
		} else if (type.equals(REQ_FULL)) {
			if (full_response_values == null) {
				full_response_values = new ArrayList<Long>();
			}

			this.last_server_time = serverTime;
			long diff = clientInterruptTime - serverInterrutpTime > 0 ? clientInterruptTime
					- serverInterrutpTime
					: 0L;
			full_response_values.add(diff);
		}
		return 0L;
	}

	/**
	 * Eseguo la media tra i valori di valori_full_response, quindi lo aggiungo
	 * a last_server_time e lo ritorno Method called at the end of a full
	 * request. Makes the average of full_response_values, adds it to
	 * last_server_time and returns it
	 * 
	 * @return
	 */
	public long getFullResponse() {
		if (full_response_values != null && !full_response_values.isEmpty()) {
			long media = 0;
			for (int i = 0; i < full_response_values.size(); i++) {
				media += full_response_values.get(i);
			}
			media = media / (full_response_values.size() * 2000);
			full_response_values.clear();
			return this.last_server_time + media;
		}
		return 0L;
	}

}
