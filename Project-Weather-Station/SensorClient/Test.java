
public class Test {

	public static void main(String[] args) {
		SensorClient client = new SensorClient("2A02:8071:A3C5:6500:157E:E550:F64E:7534");
		System.out.println(client.getTemperature());
		System.out.println(client.getPressure());
		System.out.println(client.getAltitude());
		System.out.println(client.getSealevelPressure());
	}

}
