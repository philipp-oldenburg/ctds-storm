

import org.json.JSONObject;

public interface Receiver {

	void receivedTemperature(double temperature);

	void receivedPressure(double pressure);

	void receivedAltitude(double altitude);

	void receivedSealevelPressure(double sealevelpressure);

	void receivedHumidity(double humidity);

	void receivedLight(double light);
	
	void receivedWindSpeed(double windspeed);
	
	void receivedAllData(JSONObject json);
	
	void receivedpong();
	
	void connectionReset();

}
