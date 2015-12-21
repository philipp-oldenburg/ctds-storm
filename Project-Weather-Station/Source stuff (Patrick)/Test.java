
public class Test {

	public static void main(String[] args) {
		SensorClientInterface client = new SensorClient("deffi.thecuslink.com");
		long timestamp = System.currentTimeMillis();
		System.out.println(client.getTemperature());
		System.out.println(System.currentTimeMillis() - timestamp);
		System.out.println(client.getPressure());
		System.out.println(client.getAltitude());
		System.out.println(client.getSealevelPressure());
	}

}
