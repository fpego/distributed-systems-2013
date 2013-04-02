package clockSync.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import clockSync.common.ClockSyncProtocol;

/**
 * Instance of the server, will handle the reply to the client
 */
public class SyncServerThread extends Thread {

	private Socket client = null;
	private ClockSyncProtocol protocol;
	private long responseTime;
	private long currentTime;
	private long elapsedTime;
	private PrintWriter out;
	private BufferedReader in;
	private String received;

	/**
	 * Initiate the SyncServerThread class
	 * 
	 * @param socket
	 *            the client socket
	 * @param responseTime
	 *            the server's current system nanotime, used to measure the
	 *            response time
	 */
	public SyncServerThread(Socket socket, long responseTime) {
		super("SyncServerThread");
		protocol = new ClockSyncProtocol();
		this.responseTime = responseTime;
		this.client = socket;
	}

	/**
	 * Implements the response to the client
	 */
	public void run() {
		try {
			System.out.println("\nRequest received from client "
					+ client.getInetAddress() + ":" + client.getPort());

			out = new PrintWriter(client.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(
					client.getInputStream()));

			received = in.readLine();

			if (received.equals(ClockSyncProtocol.REQ_SIMPLE)
					|| received.equals(ClockSyncProtocol.REQ_FULL)) {
				currentTime = System.currentTimeMillis();
				elapsedTime = System.nanoTime() - responseTime;
				out.println(protocol.simpleResponse(currentTime, elapsedTime));
			} else {
				out.println(ClockSyncProtocol.SERVER_ERROR_MESSAGE);
			}

		} catch (IOException e) {
			System.err
					.println("Comunication error: " + e.getLocalizedMessage());
		}

		try {
			out.close();
		} catch (Exception e) {
		}

		try {
			in.close();
		} catch (Exception e) {
		}

		try {
			client.close();
		} catch (Exception e) {
		}
	}
}
