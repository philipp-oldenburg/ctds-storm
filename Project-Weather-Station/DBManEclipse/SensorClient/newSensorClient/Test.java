package newSensorClient;
import java.io.IOException;
import java.net.UnknownHostException;

import org.json.JSONException;
import org.json.JSONObject;

public class Test implements Receiver {

	public static void main(String[] args) {
		SensorClient client = null;
		try {
			client = new SensorClient("deffi.thecuslink.com", 1338, new Test());
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		client.requestAllData();
		client.requestHumidity();
		client.requestTemperature();
	}

	@Override
	public void receivedTemperature(double temperature) {
		System.out.println(temperature);
	}

	@Override
	public void receivedPressure(double pressure) {
		System.out.println(pressure);
	}

	@Override
	public void receivedAltitude(double altitude) {
		System.out.println(altitude);
	}

	@Override
	public void receivedSealevelPressure(double sealevelpressure) {
		System.out.println(sealevelpressure);
	}

	@Override
	public void receivedHumidity(double humidity) {
		System.out.println(humidity);
	}

	@Override
	public void receivedLight(double light) {
		System.out.println(light);
	}

	@Override
	public void receivedWindSpeed(double windspeed) {
		System.out.println(windspeed);
	}

	@Override
	public void receivedAllData(JSONObject json) {
		System.out.println(json.toString());
	}

	@Override
	public void receivedpong() {
		System.out.println("rcvd pong");
	}

}
