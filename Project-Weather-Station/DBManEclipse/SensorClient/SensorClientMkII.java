
import java.io.*;
import java.net.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.json.JSONException;
import org.json.JSONObject;

import net.aksingh.owmjapis.CurrentWeather;

public class SensorClientMkII {
	
	private Socket socket;
	private PrintWriter dout;
	private BufferedReader din;
	private String sensorServerAddress;
	private Receiver receiver;

	/** Builds up connection to sensor server.<br>
	 * 	Available methods: getTemperature(), getPressure(), getAltitude().<br>
	 *  Connection gets closed automatically during object finalization.
	 * @param serverIP IP or hostname of the server
	 * @param receiver 
	 * @throws IOException 
	 * @throws UnknownHostException 
	 */
	public SensorClientMkII(String serverIP, int port) throws UnknownHostException, IOException {
		socket = new Socket(serverIP, port);
		try {
			dout = new PrintWriter(socket.getOutputStream());
			din = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		} catch (Exception e) {
			e.printStackTrace();
		}
		sensorServerAddress = serverIP;
	}
	
	public void init(Receiver rec) {
		receiver = rec;
		new Listener(din, receiver).start();
	}
	
	public void requestTemperature() {
		request("TEMP");
	}
	
	public void requestPressure() {
		request("PRESS");
	}

	public void requestAltitude() {
		request("ALTI");
	}
	
	public void requestSealevelPressure() {
		request("SEAL");
	}
	
	public void requestHumidity() {
		request("HUMI");
	}
	
	public void requestLight() {
		request("LUMI");
	}
	
	public void requestWindSpeed() throws IOException {
		Thread thread = new Thread() {
			public void run(){
			
				try {
					URL url = new URL("http://192.168.2.222:8080");
					System.out.println("Opening stream to wind sensor...");
					BufferedReader is = new BufferedReader(new InputStreamReader(url.openStream()));
					System.out.println("Opened connection to wind sensor.");
					try {
						receiver.receivedWindSpeed(Double.parseDouble(is.readLine()));
					} finally {
						is.close();
					}
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
				catch (IOException e) {
					System.out.println("Wind sensor unavailable.");
					e.printStackTrace();
				}
				receiver.receivedWindSpeed(-1);
			}
		};
		thread.start();
	}
	
	public void ping() throws IOException {
		request("PING");
	}
	
	private void request(String request) {
		dout.println(request);
		dout.flush();
		System.out.println("Sent request: " + request);
	}
	
	@Override
	protected void finalize() throws Throwable {
		dout.close();
		socket.close();
		super.finalize();
	}

	public String getSensorServerAddress() {
		return sensorServerAddress;
	}

	public void requestAllData() {
		request("ALLD");
	}
	
	private class Listener extends Thread {
		private Receiver receiver;
		private BufferedReader din;

		public Listener(BufferedReader din, Receiver receiver) {
			this.din = din;
			this.receiver = receiver;
		}

		@Override
		public void run() {
			try {
				while (true) {
					String line = din.readLine();
					String msg = line.split(";")[1];
					String type = line.split(";")[0];
					switch (type) {
					case "TEMP":
						receiver.receivedTemperature(Double.parseDouble(msg));
						break;
					case "HUMI":
						receiver.receivedHumidity(Double.parseDouble(msg));
						break;
					case "LUMI":
						receiver.receivedLight(Double.parseDouble(msg));
						break;
					case "SEAL":
						receiver.receivedSealevelPressure((Double.parseDouble(msg)));
						break;
					case "PRES":
						receiver.receivedPressure(Double.parseDouble(msg));
						break;
					case "ALTI":
						receiver.receivedAltitude(Double.parseDouble(msg));
						break;
					case "ALLD":
						receiver.receivedAllData((new JSONObject(msg)));
						break;

					default:
						System.out.println("Received unknown prot:" + type);
						break;
					}
				}
			} catch (NumberFormatException e) {
				e.printStackTrace();
			} catch (IOException e) {
				receiver.connectionReset();
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
}