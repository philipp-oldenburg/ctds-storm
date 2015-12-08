
public class Test {

	public static void main(String[] args) {
		SensorClient client = new SensorClient("deffi.thecuslink.com");
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
		System.out.println(client.getLuminosity());
	}

}
