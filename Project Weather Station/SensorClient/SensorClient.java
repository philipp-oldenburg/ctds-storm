import java.io.*;
import java.net.*;

public class SensorClient {
	
	private Socket socket;
	private PrintWriter dout;
	private BufferedReader din;

	public SensorClient() {
		try {
			socket = new Socket("localhost", 1337);  
			dout = new PrintWriter(socket.getOutputStream());
			din = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public double getTemperature() {
		String res = requestAndWaitForResponse("TEMP");
		return Double.valueOf(res);
	}
	
	public double getPressure() {
		String res = requestAndWaitForResponse("PRESS");
		return Double.valueOf(res);
	}
	
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