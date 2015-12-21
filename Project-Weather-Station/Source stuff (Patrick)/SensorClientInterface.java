
public interface SensorClientInterface {

	/** Requests temperature from the server and waits for response.
	 * @return temperature in °C
	 */
	double getTemperature();

	/** Requests Pressure from the server and waits for response.
	 * @return pressure in Pa
	 */
	double getPressure();

	/** Requests altitude from the server and waits for response.
	 * @return altitude in m
	 */
	double getAltitude();

	/** Requests sealevel pressure from the server and waits for response.
	 * @return sealevel pressure in Pa
	 */
	double getSealevelPressure();

	double getHumidity();

	double getWindSpeed();

	double getLight();

}