package clockSync.server;

import java.io.IOException;
import java.net.ServerSocket;

import clockSync.common.ClockSyncProtocol;

/**
 * Time syncronization server. Provides a public network time service,
 * implementing Cristian's algorithm
 */
public class SyncServer {

	/**
	 * number of max retries for the server to try to register the service to
	 * the OS on different ports
	 */
	private final int MAX_RETRIES = 10;
	/** the server socket */
	private ServerSocket server;
	/** current listening port */
	private int current_port;
	/** thread listening on current_port */
	private Thread serverMainThread;

	public static void main(String[] args) {
		SyncServer s = new SyncServer();
		s.startServer();
	}

	public SyncServer() {
		current_port = ClockSyncProtocol.DEFAULT_PORT;
	}

	/**
	 * Launch the server. It remains listening until stopServer() is called or a
	 * fatal error occours.
	 */
	public void startServer() {

		if (server != null) {
			System.err.println("The server is already running on port "
					+ current_port + "!");
			return;
		}

		for (int i = 0; i < MAX_RETRIES && server == null; i++) {
			current_port += i;

			try {
				server = new ServerSocket(current_port);
				System.out.println("Server up and listening on port "
						+ current_port);
			} catch (IOException e) {
				System.err.println("Impossible to register the server on port "
						+ current_port + ", trying the next one...");
				server = null;
			}

			if (current_port < 4444 || current_port > 4454) {
				break;
			}
		}
		if (server == null) {
			System.out
					.println("Impossible to register the server on any port, the program will now exit.");
			return;
		}

		serverMainThread = new Thread() {
			public void run() {
				while (true) {
					try {
						new SyncServerThread(server.accept(), System.nanoTime())
								.start();
					} catch (IOException e) {
						if (server == null) {
							return;
						}
					}
				}
			}
		};

		serverMainThread.start();

	}

	/**
	 * If the server is running, it's stopped and all the connections are
	 * closed.
	 */
	public void stopServer() {
		if (serverMainThread != null && serverMainThread.isAlive()) {

			try {
				server.close();
			} catch (IOException e) {
			}

			server = null;

			try {
				serverMainThread.join();
			} catch (InterruptedException e) {
			}

			serverMainThread = null;

			System.out.println("Server stopped.");
		} else {
			System.out.println("The server was not running!");
		}
	}

	/**
	 * Returns the listening port. If the server is not running, the default
	 * port is returned.
	 */
	public int getPort() {
		return current_port;
	}

	/**
	 * Sets the server listening port
	 * 
	 * @param port
	 *            the port the server will listen to. It must be between 1024
	 *            and 65535, otherwise the default listening port is set (4444)
	 */
	public void setPort(int port) {
		if (port > ClockSyncProtocol.MIN_PORT
				&& port < ClockSyncProtocol.MAX_PORT)
			this.current_port = port;
		else
			this.current_port = ClockSyncProtocol.DEFAULT_PORT;
	}
}
