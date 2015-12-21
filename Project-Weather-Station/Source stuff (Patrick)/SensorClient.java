import java.io.*;
import java.net.*;

public class SensorClient implements SensorClientInterface {
	
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

	@Override
	public double getHumidity() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getWindSpeed() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getLight() {
		// TODO Auto-generated method stub
		return 0;
	}
}