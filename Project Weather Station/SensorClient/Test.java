
public class Test {

	public static void main(String[] args) {
		SensorClient client = new SensorClient("localhost");
		System.out.println(client.getTemperature());
		System.out.println(client.getPressure());
		System.out.println(client.getAltitude());
	}

}
