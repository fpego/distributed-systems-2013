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
 * Time syncronization client. It makes the time request to a server and
 * displays the response. The request can either be "simple" or "full".
 */
public class SyncClient {

	/** max number of request made in a full request */
	public static final int MAX_REQUEST_NUMBER = 50;
	/** default number of requests for a full request */
	public static final int DEFAULT_REQUEST_NUMBER = 10;
	/** default server location */
	public static final String DEFAULT_SERVER = "localhost";
	/** milliseconds between two requests in the full request */
	private final long SLEEP_TIME = 100;
	/** time display format */
	public static final String DATA_FORMAT = "dd/MM/yyyy hh:mm ss SSS";

	private String server;
	private int port;
	private String request_type;
	private int request_number_full;
	private ClockSyncProtocol protocol;
	private long currentTime;

	private Calendar calendar;
	private SimpleDateFormat date_format;

	/**
	 * Main entry point. Asks to the user the server's IP and port and gets the
	 * current time on the server.
	 */
	public static void main(String[] args) {
		SyncClient client = new SyncClient();
		Scanner scanner = new Scanner(System.in);

		System.out.print("Insert server's IP (default: localhost): ");
		String server = scanner.nextLine();

		if (!server.equals("")) {
			client.setServer(server);
			System.out.println("Server is \"" + client.getServer() + "\".");
		}

		System.out.println("Insert server's port (default: 4444): ");
		String port = scanner.nextLine();
		scanner.close();

		if (!port.equals("")) {
			client.setPort(port);
			System.out.println("Server port is \"" + client.getPort() + "\".");
		}

		System.out.println("Date: " + client.getCurrentTimeAsString(0));
	}

	/**
	 * Initialize the SyncClient class
	 */
	public SyncClient() {
		server = SyncClient.DEFAULT_SERVER;
		port = ClockSyncProtocol.DEFAULT_PORT;
		request_type = ClockSyncProtocol.REQ_SIMPLE;
		request_number_full = SyncClient.DEFAULT_REQUEST_NUMBER;
		protocol = new ClockSyncProtocol();
		calendar = new GregorianCalendar();
		date_format = new SimpleDateFormat(SyncClient.DATA_FORMAT);
		currentTime = 0L;
	}

	/**
	 * Returns the current time, fetched from the server.
	 * 
	 * @param request_type
	 *            If '0', the request is "simple". If '1', the request is "full"
	 *            using the default number of requests. If the number is between
	 *            2 and MAX_REQUEST_NUMBER will be executed a full request with
	 *            this number of requests to the server.
	 * @return current time fetched from the server, or '0' on error.
	 */
	public long getCurrentTime(int request_type) {
		if (request_type == 0) {
			this.request_type = ClockSyncProtocol.REQ_SIMPLE;
		} else if (request_type == 1) {
			this.request_type = ClockSyncProtocol.REQ_FULL;
		} else if (request_type >= 2
				&& request_type <= SyncClient.MAX_REQUEST_NUMBER) {
			this.request_number_full = request_type;
			this.request_type = ClockSyncProtocol.REQ_FULL;
		}

		getCurrentTime();

		this.request_number_full = SyncClient.DEFAULT_REQUEST_NUMBER;

		return this.currentTime;
	}

	/**
	 * The same as {@link getCurrentTime()}, but returns the time as a string in
	 * the following format: "dd/MM/yyyy hh:mm ss SSS" If there are errors
	 * returns "01/01/1970 00:00 00 000"
	 */
	public String getCurrentTimeAsString(int request_type) {
		getCurrentTime(request_type);

		calendar.setTimeInMillis(currentTime);
		return date_format.format(calendar.getTime());
	}

	/**
	 * Returns the current time fetched from the server as milliseconds since 1
	 * Jan 1970 (Unix Time). On errors, return '0'
	 */
	private void getCurrentTime() {
		if (request_type.equals(ClockSyncProtocol.REQ_SIMPLE)) {
			executeRequest();
		} else if (request_type.equals(ClockSyncProtocol.REQ_FULL)) {
			// faccio N volte la richiesta semplice, poi ci pensa la classe del
			// protocollo a fare la media tra i valori
			checkRequestNumber();
			int current_iteration;
			for (current_iteration = 0; current_iteration < this.request_number_full; current_iteration++) {
				executeRequest();
				if (this.server == null) {
					currentTime = 0L;
					return;
				}
				try {
					Thread.sleep(SLEEP_TIME);
				} catch (InterruptedException e) {
				}
			}
			currentTime = protocol.getFullResponse();
		}
	}

	/**
	 * Executes a single request to the server and saves the reply in
	 * 'currentTime'.
	 */
	private void executeRequest() {
		Socket socket = null;
		String fromServer = null;
		PrintWriter out = null;
		BufferedReader in = null;
		long startTime = 0, endTime = 0;

		this.currentTime = 0L;

		if (server == null) {
			System.err.println("Server is NULL, cannot create the socket.");
			return;
		}

		try {
			socket = new Socket(server, port);
			out = new PrintWriter(socket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
		} catch (Exception e) {
			System.out.println("Server unknown: " + server);
			server = null;
			return;
		}

		try {
			startTime = System.nanoTime();
			out.println(this.request_type);
			fromServer = in.readLine();
			endTime = System.nanoTime();

			this.currentTime = this.protocol.parseResponse(fromServer,
					this.request_type, endTime - startTime);
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			out.close();
			in.close();
			socket.close();
		} catch (IOException e) {
		}
	}

	/**
	 * Checks if request_number_full it's in the correct range between 1 and
	 * MAX_REQUEST_NUMBER. If out of bounds, sets to default.
	 */
	private void checkRequestNumber() {
		if (this.request_number_full < 1
				|| this.request_number_full > SyncClient.MAX_REQUEST_NUMBER) {
			this.request_number_full = SyncClient.MAX_REQUEST_NUMBER;
		}
	}

	/**
	 * Sets the server to which connect
	 * 
	 * @param server
	 *            name or IP of the server
	 */
	public void setServer(String server) {
		this.server = server;
	}

	public String getServer() {
		return server;
	}

	/**
	 * Sets the server port
	 * 
	 * @param port
	 *            server port. Must be between 1024 and 65535
	 */
	public void setPort(int port) {
		if (port > ClockSyncProtocol.MIN_PORT
				&& port < ClockSyncProtocol.MAX_PORT)
			this.port = port;
		else
			this.port = ClockSyncProtocol.DEFAULT_PORT;
	}

	/**
	 * Sets the server port
	 * 
	 * @param port
	 *            server port. Must be between 1024 and 65535
	 */
	public void setPort(String port) {
		int iPort;
		try {
			iPort = Integer.parseInt(port);
		} catch (Exception e) {
			iPort = ClockSyncProtocol.DEFAULT_PORT;
		}
		this.setPort(iPort);
	}

	/**
	 * Returns the server's port to witch the client will connect
	 * 
	 * @return the server's port
	 */
	public int getPort() {
		return this.port;
	}
}
