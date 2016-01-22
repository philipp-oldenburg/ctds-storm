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
	public double getTemperature() throws IOException {
		String res;
		double result;
		for(int i=0; i < 5; i++) {
//			try {
				res = requestAndWaitForResponse("TEMP");
				result = (res == null) ? -300 : Double.valueOf(res);
//			} catch (IOException e) {
//				e.printStackTrace();
//				continue;
//			}
			return result;
		}
		return -300;
	}
	
	/* (non-Javadoc)
	 * @see SensorClientInterface#getPressure()
	 */
	@Override
	public double getPressure() throws IOException {
		String res;
		double result;
		for(int i=0; i < 5; i++) {
//			try {
				res = requestAndWaitForResponse("PRES");
				result = (res == null) ? -1 : Double.valueOf(res);
//			} catch (IOException e) {
//				e.printStackTrace();
//				continue;
//			}
			return result;
		}
		return -1;
		
//		String res;
//		double result;
//		try {
//			res = requestAndWaitForResponse("PRES");
//			result = (res == null) ? -1 : Double.valueOf(res);
//		} catch (IOException e) {
//			return -1;
//		}
//		return result;
	}
	
	/* (non-Javadoc)
	 * @see SensorClientInterface#getAltitude()
	 */
	@Override
	public double getAltitude() {
		String res;
		double result;
		for(int i=0; i < 5; i++) {
			try {
				res = requestAndWaitForResponse("ALTI");
				result = (res == null) ? -1 : Double.valueOf(res);
			} catch (IOException e) {
				continue;
			}
			return result;
		}
		return -1;
		
//		String res;
//		double result;
//		try {
//			res = requestAndWaitForResponse("ALTI");
//			result = (res == null) ? -1 : Double.valueOf(res);
//		} catch (IOException e) {
//			return -1;
//		}
//		return result;
	}
	
	/* (non-Javadoc)
	 * @see SensorClientInterface#getSealevelPressure()
	 */
	@Override
	public double getSealevelPressure() {
		String res;
		double result;
		for(int i=0; i < 5; i++) {
			try {
				res = requestAndWaitForResponse("SEAL");
				result = (res == null) ? -1 : Double.valueOf(res);
			} catch (IOException e) {
				continue;
			}
			return result;
		}
		return -1;
		
//		String res;
//		double result;
//		try {
//			res = requestAndWaitForResponse("SEAL");
//			result = (res == null) ? -1 : Double.valueOf(res);
//		} catch (IOException e) {
//			return -1;
//		}
//		return result;
	}
	
	/* (non-Javadoc)
	 * @see SensorClientInterface#getHumidity()
	 */
	@Override
	public double getHumidity() throws IOException {
		String res;
		double result;
		for(int i=0; i < 5; i++) {
//			try {
				res = requestAndWaitForResponse("HUMI");
				result = (res == null) ? -1 : Double.valueOf(res);
//			} catch (IOException e) {
//				e.printStackTrace();
//				continue;
//			}
			return result;
		}
		return -1;
		
//		String res;
//		double result;
//		try {
//			res = requestAndWaitForResponse("HUMI");
//			result = (res == null) ? -1 : Double.valueOf(res);
//		} catch (IOException e) {
//			return -1;
//		}
//		return result;
	}
	
	/* (non-Javadoc)
	 * @see SensorClientInterface#getLuminosity()
	 */
	@Override
	public double getLight() {
//		String res;
//		double result;
//		for(int i=0; i < 5; i++) {
//			try {
//				res = requestAndWaitForResponse("LUMI");
//				result = (res == null) ? -1 : Double.valueOf(res);
//			} catch (IOException e) {
//				continue;
//			}
//			return result;
//		}
		return -1;
		
//		String res;
//		double result;
//		try {
//			res = requestAndWaitForResponse("LUMI");
//			result = (res == null) ? -1 : Double.valueOf(res);
//		} catch (IOException e) {
//			return -1;
//		}
//		return result;
	}
	
	public double getWindSpeed() throws IOException {
		try {
			URL url = new URL("http://192.168.2.222:8080");
			BufferedReader is = new BufferedReader(new InputStreamReader(url.openStream()));
			
			try {
				return Double.parseDouble(is.readLine());
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
//		catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		return -1;
		
	}
	
	public boolean ping() throws IOException {
		String res = "";
		res = requestAndWaitForResponse("PING");
//		System.out.println(res.equals("PONG"));
		return res.equals("PONG");
	}
	
	private String requestAndWaitForResponse(String request) throws IOException{
//		if (din.ready()) {
//			System.out.println("--------------------------------");
//			while(din.ready()) {
//				System.out.print(din.read());
//			}
//			System.out.print(System.getProperty("line.separator"));
//			System.out.println("--------------------------------");
//		}
		
		dout.println(request);
		dout.flush();
		System.out.println("Sent request: " + request);
		String res = din.readLine();
		if (res == null) throw new IOException();
		System.out.println(" and received response: " + res);
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

	@Override
	public JSONObject getAllData() {
		String json = null;
		try {
			json = requestAndWaitForResponse("ALLD");
		} catch (IOException e) {
			return null;
		}
		try {
			return new JSONObject(json);
		} catch (JSONException e) {
			return null;
		}
	}
}