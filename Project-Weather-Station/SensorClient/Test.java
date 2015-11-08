
public class Test {

	public static void main(String[] args) {
		SensorClient client = new SensorClient("192.168.2.66");
		System.out.println(client.getTemperature());
		System.out.println(client.getPressure());
		System.out.println(client.getAltitude());
		System.out.println(client.getSealevelPressure());
	}

}
