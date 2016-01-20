import java.io.*;
import java.net.*;

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
		sensorServerAddress = serverIP;
	}
	
	/* (non-Javadoc)
	 * @see SensorClientInterface#getTemperature()
	 */
	@Override
	public double getTemperature() {
		String res = requestAndWaitForResponse("TEMP");
		return Double.valueOf(res);
	}
	
	/* (non-Javadoc)
	 * @see SensorClientInterface#getPressure()
	 */
	@Override
	public double getPressure() {
		String res = requestAndWaitForResponse("PRES");
		return Double.valueOf(res);
	}
	
	/* (non-Javadoc)
	 * @see SensorClientInterface#getAltitude()
	 */
	@Override
	public double getAltitude() {
		String res = requestAndWaitForResponse("ALTI");
		return Double.valueOf(res);
	}
	
	/* (non-Javadoc)
	 * @see SensorClientInterface#getSealevelPressure()
	 */
	@Override
	public double getSealevelPressure() {
		String res = requestAndWaitForResponse("SEAL");
		return Double.valueOf(res);
	}
	
	/* (non-Javadoc)
	 * @see SensorClientInterface#getHumidity()
	 */
	@Override
	public double getHumidity() {
		String res = requestAndWaitForResponse("HUMI");
		return Double.valueOf(res);
	}
	
	/* (non-Javadoc)
	 * @see SensorClientInterface#getLuminosity()
	 */
	@Override
	public double getLight() {
		String res = requestAndWaitForResponse("LUMI");
		return Double.valueOf(res);
	}
	
	public double getWindSpeed() {
		return -1.0;
	}
	
	public boolean ping() {
		String res = "";
		try {
			res = requestAndWaitForResponse("PING");
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return res.equals("PONG");
	}
	
	private String requestAndWaitForResponse(String request) {
		try {
			dout.println(request);
			dout.flush();
			
			String res = din.readLine();
			return res;
		} catch (IOException e) {
			e.printStackTrace();
		}
		// should never happen
		return "-1";
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