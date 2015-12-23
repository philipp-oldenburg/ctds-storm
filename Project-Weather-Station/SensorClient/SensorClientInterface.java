
public interface SensorClientInterface {


	/** Requests temperature the server and waits for response.
	 * @return temperature in °C
	 */
	double getTemperature();

	/** Requests Pressure from server and waits for response.
	 * @return pressure in Pa
	 */
	double getPressure();

	/** Requests altitude from server and waits for response.
	 * @return altitude in m
	 */
	double getAltitude();

	/** Requests sealevel pressure from server and waits for response.
	 * @return sealevel pressure in Pa
	 */
	double getSealevelPressure();

	/** Requests humidity from server and waits for response.
	 * @return relative humidity
	 */
	double getHumidity();

	/** Requests luminosity from server and waits for response.
	 * @return luminosity in lux
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

}