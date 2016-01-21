import java.io.*;
import java.net.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.json.JSONException;

import net.aksingh.owmjapis.CurrentWeather;

public class SensorClient implements SensorClientInterface {
	
	private Socket socket;
	private PrintWriter dout;
	private BufferedReader din;
	private String sensorServerAddress;

	/** Builds up connection to sensor server.<br>
	 * 	Available methods: getTemperature(), getPressure(), getAltitude().<br>
	 *  Connection gets closed automatically during object finalization.
	 * @param serverIP IP or hostname of the server
	 * @throws IOException 
	 * @throws UnknownHostException 
	 */
	public SensorClient(String serverIP) throws UnknownHostException, IOException {
		socket = new Socket(serverIP, 1337);
		try {
			dout = new PrintWriter(socket.getOutputStream());
			din = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		} catch (Exception e) {
			e.printStackTrace();
		}
		socket.setSoTimeout(2000);
		sensorServerAddress = serverIP;
	}
	
	/* (non-Javadoc)
	 * @see SensorClientInterface#getTemperature()
	 */
	@Override
	public double getTemperature() {
		String res;
		try {
			res = requestAndWaitForResponse("TEMP");
		} catch (IOException e) {
			return -300;
		}
		return Double.valueOf(res);
	}
	
	/* (non-Javadoc)
	 * @see SensorClientInterface#getPressure()
	 */
	@Override
	public double getPressure() {
		String res;
		try {
			res = requestAndWaitForResponse("PRES");
		} catch (IOException e) {
			return -1;
		}
		return Double.valueOf(res);
	}
	
	/* (non-Javadoc)
	 * @see SensorClientInterface#getAltitude()
	 */
	@Override
	public double getAltitude() {
		String res;
		try {
			res = requestAndWaitForResponse("ALTI");
		} catch (IOException e) {
			return -1;
		}
		return Double.valueOf(res);
	}
	
	/* (non-Javadoc)
	 * @see SensorClientInterface#getSealevelPressure()
	 */
	@Override
	public double getSealevelPressure() {
		String res;
		try {
			res = requestAndWaitForResponse("SEAL");
		} catch (IOException e) {
			return -1;
		}
		return Double.valueOf(res);
	}
	
	/* (non-Javadoc)
	 * @see SensorClientInterface#getHumidity()
	 */
	@Override
	public double getHumidity() {
		String res;
		try {
			res = requestAndWaitForResponse("HUMI");
		} catch (IOException e) {
			return -1;
		}
		return Double.valueOf(res);
	}
	
	/* (non-Javadoc)
	 * @see SensorClientInterface#getLuminosity()
	 */
	@Override
	public double getLight() {
		String res;
		try {
			res = requestAndWaitForResponse("LUMI");
		} catch (IOException e) {
			return -1;
		}
		return Double.valueOf(res);
	}
	
	public double getWindSpeed() {
		return -1.0;
	}
	
	public boolean ping() throws IOException {
		String res = "";
		
		res = requestAndWaitForResponse("PING");
		
		return res.equals("PONG");
	}
	
	private String requestAndWaitForResponse(String request) throws IOException{
		dout.println(request);
		dout.flush();
		
		String res = din.readLine();
		return res;
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
}