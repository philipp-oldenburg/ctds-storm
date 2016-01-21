
import java.io.IOException;

import org.junit.Test;

public class DBManTester {
	private class MockSensorClient implements SensorClientInterface {

		public MockSensorClient() {}

		public double getTemperature() { return 20; }

		@Override
		public double getPressure() { return 100000; }

		@Override
		public double getAltitude() { return 71; }

		@Override
		public double getSealevelPressure() { return 100000; }

		@Override
		public double getHumidity() { return 40; }

		@Override
		public double getWindSpeed() { return 20; }

		@Override
		public double getLight() { return 10000; }

		@Override
		public String getSensorServerAddress() {
			return null;
		}

		@Override
		public boolean ping() throws IOException {
			// TODO Auto-generated method stub
			return false;
		}
	}
	
	@Test
	public void testCreateDBMan() {
		MockSensorClient client = new MockSensorClient();
		System.out.println("creating dbman");
		DataBaseManager dbMan = new DataBaseManager(client, false);
		while(true) {
			//Noob approach, I know, but the test has to keep running so the DBMan actually gets some work done
			//before the test terminates, and I can't be arsed to write anything fancy.
		}
	}
}
