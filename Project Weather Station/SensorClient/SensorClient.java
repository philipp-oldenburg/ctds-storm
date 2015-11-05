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
			socket = new Socket("localhost", 1337);  
			dout = new PrintWriter(socket.getOutputStream());
			din = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/** Requests temperature from the server and waits for response.
	 * @return temperature in °C
	 */
	public double getTemperature() {
		String res = requestAndWaitForResponse("TEMP");
		return Double.valueOf(res);
	}
	
	/** Requests Pressure from the server and waits for response.
	 * @return pressure in Pa
	 */
	public double getPressure() {
		String res = requestAndWaitForResponse("PRES");
		return Double.valueOf(res);
	}
	
	/** Requests altitude from the server and waits for response.
	 * @return altitude in m
	 */
	public double getAltitude() {
		String res = requestAndWaitForResponse("ALTI");
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
		return "failed";
	}
	
	@Override
	protected void finalize() throws Throwable {
		dout.close();
		socket.close();
		super.finalize();
	}
}