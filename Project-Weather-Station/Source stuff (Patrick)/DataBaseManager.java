import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import org.json.JSONException;

import net.aksingh.owmjapis.CurrentWeather;
import net.aksingh.owmjapis.OpenWeatherMap;

public class DataBaseManager {
	
	private DataCollector collector;
	
	private class DataCollector extends Thread {
		
		private static final long ACQUIREDATAINTERVAL = 10000;
		
		private static final double TEMPDEFAULTVALUE = -300;
		private static final double PRESDEFAULTVALUE = -1;
		private static final double HUMDEFAULTVALUE = -1;
		private static final double WSPEEDDEFAULTVALUE = -1;
		private static final float WDEGREEDEFAULTVALUE = -1;
		private static final double LIGHTDEFAULTVALUE = -1;
		
		private SensorClientInterface client;
		private Connection dbConnection;
		private boolean sensorsAvailable;
		private boolean weathermapAccessible;
		
		
		private String url = "jdbc:mysql://localhost:3306/ctds_db_test";
		private String user = "CTDS_DB_User";
		private String password = "password";
		
		/** Needed for some reason. Remember to add Connector/J to classpath, otherwise everything's fucked
		 * 
		 */
		private void registerDriver() {
			
			try {
				   Class.forName("com.mysql.jdbc.Driver");
				   System.out.println("Successfully registered driver.");
			} catch(ClassNotFoundException ex) {
				ex.printStackTrace();
				System.exit(1);
			}
		}
		
		/** Establishes connection to database. Host, User and Password are currently hardcoded.
		 * 
		 * @return Connection object to interact with database.
		 */
		private Connection establishConnection() {
			
			Connection conn = null;
			try {
				conn = DriverManager.getConnection(url, user, password);
				System.out.println("Established connection");
			} catch (SQLException e) {
				System.out.println("Unable to establish connection.");
				e.printStackTrace();
			}
			return conn;
		}
		
		public void run() {
			registerDriver();
			dbConnection = establishConnection();
			
			// #1 security EU. Plz no stealerino.
			String apiKey = "26ef2b98aa2077410020408feb29cde8";
			
			OpenWeatherMap owmap = new OpenWeatherMap(apiKey);
			
			
			
			while(true) {
				
				CurrentWeather currentWeather = null;
				try {
					weathermapAccessible = true;
					currentWeather = owmap.currentWeatherByCityName("Basel", "CH");
				} catch (IOException e1) {
					weathermapAccessible = false;
					e1.printStackTrace();
				} catch (JSONException e1) {
					weathermapAccessible = false;
					e1.printStackTrace();
				}
				
				WeatherData data = initializeWeatherDataObject(currentWeather, sensorsAvailable, weathermapAccessible);
				
				String query = "INSERT INTO weatherdatalog (temperature, pressure, humidity, windspeed, winddegree, light, cloudiness, owmweatherdesc)"
								+ " VALUES (" + data.getTemp() + ", " + data.getPressure() + ", " + data.getHumidity() + ", "
								+ data.getWindSpeed() + ", " + data.getWindDegree() + ", " + data.getLight()
								+ ", '"+ data.getOwmName() + "', '" + data.getOwmDesc() + "');";
				
				try {
					Statement statement = dbConnection.createStatement();
					statement.executeUpdate(query);
					System.out.println("Inserted data");
				} catch (SQLException e) {
					e.printStackTrace();
				}
				
				try {
					Thread.sleep(ACQUIREDATAINTERVAL);
				} catch (InterruptedException e) {
					System.out.println("Sleep was interrupted");
					e.printStackTrace();
				}
			}
		}
		
		/**Creates and returns WeatherData object with all fields fully initialized. If sensors or OWM are unavailable, data is taken from other
		 * service if possible or set to default values (which cannot occur naturally, marking them as missing).
		 * 
		 * @param currentWeather	CurrentWeather object from which OWM data will be taken.
		 * @param sensorsAvailable	If false, no data will be taken from the SensorClient.
		 * @param weathermapAccessible	If false, no data will be taken from OpenWeatherMap service.
		 * @return
		 */
		private WeatherData initializeWeatherDataObject(CurrentWeather currentWeather, boolean sensorsAvailable, boolean weathermapAccessible) {
			WeatherData data = new WeatherData();
			if (weathermapAccessible) {
				if (sensorsAvailable) {
					initWDwithSensorData(data);
					data.setWindDegree(currentWeather.getWindInstance().getWindDegree());
					data.setOwmDesc(currentWeather.getWeatherInstance(0).getWeatherDescription());
					data.setOwmName(currentWeather.getWeatherInstance(0).getWeatherName());
				} else {
					initWDwithOWMData(currentWeather, data);
					data.setLight(LIGHTDEFAULTVALUE);
				}
			} else {
				if (sensorsAvailable) {
					initWDwithSensorData(data);
					data.setOwmDesc("N/A");
					data.setOwmName("N/A");
					data.setWindDegree(WDEGREEDEFAULTVALUE);
				} else {
					initWDwithDefaultData(data);
				}
			}
			return data;
		}
		
		/**Sets all fields to default values, indicating that no sensor/OWM-data is present.
		 * 
		 * @param data	WeatherData object to be initialized.
		 */
		private void initWDwithDefaultData(WeatherData data) {
			data.setTemp(TEMPDEFAULTVALUE);
			data.setPressure(PRESDEFAULTVALUE);
			data.setHumidity(HUMDEFAULTVALUE);
			data.setWindSpeed(WSPEEDDEFAULTVALUE);
			data.setWindDegree(WDEGREEDEFAULTVALUE);
			data.setLight(LIGHTDEFAULTVALUE);
			data.setOwmDesc("N/A");
			data.setOwmName("N/A");
		}

		/**Initializes data with all the values which OpenWeatherMap provides. Only value missing is therefore the current light intensity.
		 * 
		 * @param currentWeather	CurrentWeather object from which data is taken.
		 * @param data	WeatherData object to be initialized.
		 */
		private void initWDwithOWMData(CurrentWeather currentWeather, WeatherData data) {
			data.setTemp(fahrenheitToCelsius(currentWeather.getMainInstance().getTemperature()));
			data.setPressure(currentWeather.getMainInstance().getPressure());
			data.setHumidity(currentWeather.getMainInstance().getHumidity());
			data.setWindSpeed(currentWeather.getWindInstance().getWindSpeed());
			data.setWindDegree(currentWeather.getWindInstance().getWindDegree());
			data.setOwmDesc(currentWeather.getWeatherInstance(0).getWeatherDescription());
			data.setOwmName(currentWeather.getWeatherInstance(0).getWeatherName());
		}

		/**Initializes data with all the values which the connected sensor array provides. WindDegree, WeatherDescription and WeatherName are
		 * only provided by OpenWeatherMap and therefore missing.
		 * 
		 * @param data	WeatherData object to be initialized.
		 */
		private void initWDwithSensorData(WeatherData data) {
			data.setTemp(client.getTemperature());
			data.setPressure(client.getPressure());
			data.setHumidity(client.getHumidity());
			data.setWindSpeed(client.getWindSpeed());
			data.setLight(client.getLight());
		}
		
		
		/**Damn 'Muricans.
		 * 
		 * @param temperature temperature in degrees fahrenheit.
		 * @return	temperature in degrees celsius.
		 */
		private double fahrenheitToCelsius(float temperature) {
			return ((5.0/9.0) * (temperature - 32.0));
		}
		
		/**Constructor
		 * 
		 * @param client SensorClient instance which the collector shall use to acquire data.
		 * @param sensorsAvailable  If true, data is collected from given SensorClient (where possible). Otherwise, all data
		 * is taken from openweathermap.org.
		 */
		public DataCollector(SensorClientInterface client, boolean sensorsAvailable) {
			this.client = client;
			this.sensorsAvailable = sensorsAvailable;
		}
		
		@Override
		protected void finalize() throws Throwable {
			dbConnection.close();
			super.finalize();
		}
	}
	
	/**Constructor
	 * 
	 * @param client SensorClient instance to be passed to the collector.
	 * @param sensorsAvailable  If true, the collector gets its data from the given SensorClient (where possible). Otherwise, all data
	 * is taken from openweathermap.org.
	 */
	public DataBaseManager(SensorClientInterface client, boolean sensorsAvailable) {
		collector = new DataCollector(client, sensorsAvailable);
		collector.start();
		System.out.println("started collector");
	}
}
