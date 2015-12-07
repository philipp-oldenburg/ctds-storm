import java.io.*;
import java.net.*;

public class SensorClient {
	
	private Socket socket;
	private PrintWriter dout;
	private BufferedReader din;

	/** Builds up connection to sensor server.<br>
	 * 	Available methods: getTemperature(), getPressure(), getAltitude().<br>
	 *  Connection gets closed automatically during object finalization.
	 * @param serverIP IP or hostname of the server
	 */
	public SensorClient(String serverIP) {
		try {
			socket = new Socket(serverIP, 1337);  
			dout = new PrintWriter(socket.getOutputStream());
			din = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/** Requests temperature the server and waits for response.
	 * @return temperature in °C
	 */
	public double getTemperature() {
		String res = requestAndWaitForResponse("TEMP");
		return Double.valueOf(res);
	}
	
	/** Requests Pressure from server and waits for response.
	 * @return pressure in Pa
	 */
	public double getPressure() {
		String res = requestAndWaitForResponse("PRES");
		return Double.valueOf(res);
	}
	
	/** Requests altitude from server and waits for response.
	 * @return altitude in m
	 */
	public double getAltitude() {
		String res = requestAndWaitForResponse("ALTI");
		return Double.valueOf(res);
	}
	
	/** Requests sealevel pressure from server and waits for response.
	 * @return sealevel pressure in Pa
	 */
	public double getSealevelPressure() {
		String res = requestAndWaitForResponse("SEAL");
		return Double.valueOf(res);
	}
	
	/** Requests humidity from server and waits for response.
	 * @return relative humidity
	 */
	public double getHumidity() {
		String res = requestAndWaitForResponse("HUMI");
		return Double.valueOf(res);
	}
	
	/** Requests luminosity from server and waits for response.
	 * @return luminosity in lux
	 */
	public double getLuminosity() {
		String res = requestAndWaitForResponse("LUMI");
		return Double.valueOf(res);
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
}