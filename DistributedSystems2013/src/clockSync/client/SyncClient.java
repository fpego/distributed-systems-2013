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
	
	public static void main(String[] args){
		SyncClient client = new SyncClient();
		long time = client.getCurrentTime();
		System.out.println("Il server ha risposto con l'ora corrente: " + time);
		
		Date d = new Date(time);
		System.out.println("Data: " + d.toString());
	}
	
	public SyncClient(){
		host = "localhost";
	}
	
	public long getCurrentTime(){
		Socket socket = null;
        String fromServer = null;
		PrintWriter out = null;
        BufferedReader in = null;
        long startTime = 0, endTime = 0;
        long serverTime = 0, serverInterrutpTime = 0;
        long currentTime = 0;
        
		try {
			socket = new Socket(host, ClockSyncProtocol.port);
			out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try{
			startTime = System.nanoTime();
			out.println(ClockSyncProtocol.REQ_SIMPLE);
	        fromServer = in.readLine();
	        
	        endTime = System.nanoTime();
			
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
		}catch (Exception e){
			e.printStackTrace();
		}
        
        try {
        	out.close();
        	in.close();
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return currentTime;
	}
}
