import java.io.IOException;
import java.net.UnknownHostException;

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
			System.out.println("HUM");
			System.out.println(client.getHumidity());
			System.out.println("ALTI");
			System.out.println(client.getAltitude());
			System.out.println("PRESS");
			System.out.println(client.getPressure());
			System.out.println("SEAPRESS");
			System.out.println(client.getSealevelPressure());
			System.out.println("LUMI");
			System.out.println(client.getLight());
		} else System.out.println("Rekt");
	}

}