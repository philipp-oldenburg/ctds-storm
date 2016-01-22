import java.io.IOException;

import org.json.JSONObject;

public interface SensorClientInterface {


	/** Requests temperature the server and waits for response.
	 * @return temperature in °C
	 * @throws IOException 
	 */
	double getTemperature();

	/** Requests Pressure from server and waits for response.
	 * @return pressure in Pa
	 * @throws IOException 
	 */
	double getPressure();

	/** Requests altitude from server and waits for response.
	 * @return altitude in m
	 * @throws IOException 
	 */
	double getAltitude();

	/** Requests sealevel pressure from server and waits for response.
	 * @return sealevel pressure in Pa
	 * @throws IOException 
	 */
	double getSealevelPressure();

	/** Requests humidity from server and waits for response.
	 * @return relative humidity
	 * @throws IOException 
	 */
	double getHumidity();

	/** Requests luminosity from server and waits for response.
	 * @return luminosity in lux
	 * @throws IOException 
	 */
	double getLight();
	
	/**Returns wind speed. Placeholder until better solution is found.
	 * 
	 * @return Some arbitrary value.
	 */
	double getWindSpeed();
	
	/**Returns IP address of connected SensorServer.
	 * 
	 * @return ^
	 */
	String getSensorServerAddress();
	
	/**Returns all data in JSON. Placeholder until better solution is found.
	 * 
	 * @return JSON Object or null if data could not be retrieved or received data is no valid JSON String
	 */
	JSONObject getAllData();
	
	public boolean ping() throws IOException;

}