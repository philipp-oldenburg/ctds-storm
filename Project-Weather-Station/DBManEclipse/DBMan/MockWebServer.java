import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;

public class MockWebServer extends Thread{
	String hostName;
	int portNumber;
	
	public MockWebServer(String host, int port) {
		hostName = host;
		portNumber = port;
	}
	
	public void run() {
		System.out.println("connecting");
		try (
			Socket socket = new Socket(hostName, portNumber);
		    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
		    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		) {
			Thread.sleep(1000);
			out.println("2016-01-04 00:00:00;2016-01-15 23:59:59;TRUE;owmtemperature");
			System.out.println("Sent Request");
			
			String input;
			while(true) {
				if((input = in.readLine()) != null) {
					JSONObject jobj = new JSONObject(input);
					Iterator<?> keys = jobj.keys();

					while( keys.hasNext() ) {
					    System.out.println(jobj.get((String)keys.next()) + System.getProperty("line.separator"));
					}
				}
			}
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
