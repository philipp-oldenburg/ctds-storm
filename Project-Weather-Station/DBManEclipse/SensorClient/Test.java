import java.io.IOException;
import java.net.UnknownHostException;

import org.json.JSONException;
import org.json.JSONObject;

public class Test {

	public static void main(String[] args) {
		SensorClientInterface client = null;
		try {
			client = new SensorClient("deffi.thecuslink.com");
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (client != null) {
			System.out.println("TEMP");
			System.out.println(client.getTemperature());
			System.out.println("HUMI");
			System.out.println(client.getHumidity());
			System.out.println("ALTI");
			System.out.println(client.getAltitude());
			System.out.println("PRES");
			System.out.println(client.getPressure());
			System.out.println("SEAL");
			System.out.println(client.getSealevelPressure());
			System.out.println("LUMI");
			System.out.println(client.getLight());
			System.out.println(client.getWindSpeed());
			JSONObject obj = client.getAllData();
			
			try {
				System.out.println(obj.get("temperature"));
				System.out.println(obj.get("pressure"));
				System.out.println(obj.get("luminosity"));
				System.out.println(obj.get("humidity"));
				
			} catch (JSONException e) {
				e.printStackTrace();
			}
			 while(true) {
				long time = System.currentTimeMillis();
				try {
					client.ping();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				System.out.println("RTT: " + (System.currentTimeMillis() - time));
			 }
		} else System.out.println("Rekt");
	}

}
