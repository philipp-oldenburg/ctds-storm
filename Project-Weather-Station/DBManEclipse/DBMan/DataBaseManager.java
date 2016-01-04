import java.io.IOException;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.json.JSONException;

import net.aksingh.owmjapis.CurrentWeather;
import net.aksingh.owmjapis.OpenWeatherMap;

public class DataBaseManager {
	
	private DataCollector collector;
	
	private class DataCollector extends Thread {
		
		private static final long ACQUIREDATAINTERVAL = 30000;
		
		private static final double TEMPDEFAULTVALUE = -300;
		private static final double PRESDEFAULTVALUE = -1;
		private static final double HUMDEFAULTVALUE = -1;
		private static final double WSPEEDDEFAULTVALUE = -1;
		private static final float WDEGREEDEFAULTVALUE = -1;
		private static final double LIGHTDEFAULTVALUE = -1;

		private static final long WEATHER_REQUEST_TIMEOUT = 2;

		private static final int ITERATION_LIMIT = 5;
		
		private SensorClientInterface client;
		private Connection dbConnection;
		private boolean sensorServerAvailable;
		private boolean weathermapAccessible;
		
		private String sensorServerAddress;
		
		private String url = "jdbc:mysql://localhost:3306/ctds_db_test";
		private String user = "CTDS_DB_User";
		private String password = "password";

		private OpenWeatherMap owmap;
		
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
		
		private class SCReconnector extends Thread {
			private SensorClient client;
			
			public SCReconnector(SensorClient client) {
				this.client = client;
			}
			
			public void run() {
				while (true) {
					try {
						client = new SensorClient(sensorServerAddress);
						sensorServerAvailable = true;
						break;
					} catch(UnknownHostException e) {
						System.out.println("Unknown Host. Please verify if SensorServer is actually running.");
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e1) {
							e1.printStackTrace();
						}
						continue;
					} catch(IOException e) {
						System.out.println("IOException. Do something.");
						e.printStackTrace();
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e1) {
							e1.printStackTrace();
						}
						continue;
					}
				}
			}
		}
		
//		private void reestablishSensorServerConnection() {
//			while (true) {
//				try {
//					client = new SensorClient(sensorServerAddress);
//					break;
//				} catch(UnknownHostException e) {
//					System.out.println("Unknown Host. Please verify if SensorServer is actually running.");
//					try {
//						Thread.sleep(1000);
//					} catch (InterruptedException e1) {
//						e1.printStackTrace();
//					}
//					continue;
//				} catch(IOException e) {
//					System.out.println("IOException. Do something.");
//					e.printStackTrace();
//					try {
//						Thread.sleep(1000);
//					} catch (InterruptedException e1) {
//						e1.printStackTrace();
//					}
//					continue;
//				}
//			}
//		}
		
		private CurrentWeather getCurrentWeatherWithTimeout(CurrentWeather currentWeather) throws ExecutionException, InterruptedException, TimeoutException {
			
			ExecutorService executor = Executors.newCachedThreadPool();
			
			Callable<CurrentWeather> task = new Callable<CurrentWeather>() {
				public CurrentWeather call() throws IOException, JSONException {
					return owmap.currentWeatherByCityName("Basel", "CH");
				}
			};
			
			Future<CurrentWeather> future = executor.submit(task);
			currentWeather = future.get(WEATHER_REQUEST_TIMEOUT, TimeUnit.SECONDS);
			
//			int tries = 0;
//			while(tries < ITERATION_LIMIT) {
//				Future<CurrentWeather> future = executor.submit(task);
//				try {
//					currentWeather = future.get(WEATHER_REQUEST_TIMEOUT, TimeUnit.SECONDS);
//					break;
//				} catch (InterruptedException e) {
//					System.out.println("Request interrupted while waiting for response.");
//					e.printStackTrace();
//					continue;
//				} catch (TimeoutException e) {
//					System.out.println("Did not receive response within "+WEATHER_REQUEST_TIMEOUT+" seconds.");
//					e.printStackTrace();
//					continue;
//				} 
//				finally {		What would happen here? finally is going to be called after the continue/break, does that mean that future is 
//					future.cancel(true);		already gone at this point, causing a runtime error?
//				}
//			}
			
			return currentWeather;
		}
		
		public void run() {
			registerDriver();
			dbConnection = establishConnection();
			
			// #1 security EU. Plz no stealerino.
			String apiKey = "26ef2b98aa2077410020408feb29cde8";
			
			owmap = new OpenWeatherMap(apiKey);
			System.out.println("created OpenWeatherMap instance");
			
			
			while(true) {
				
				CurrentWeather currentWeather = null;
				try {
					weathermapAccessible = true;
					currentWeather = getCurrentWeatherWithTimeout(currentWeather);
					System.out.println("Received current Weather.");
				} catch (ExecutionException e1) {
					System.out.println("OpenWeatherMap appears to be inaccessible.");
					weathermapAccessible = false;
					e1.printStackTrace();
				} catch (InterruptedException e) {
					weathermapAccessible = false;
					System.out.println("Request interrupted while waiting for response.");
					e.printStackTrace();
				} catch (TimeoutException e) {
					weathermapAccessible = false;
					System.out.println("Did not receive response within "+WEATHER_REQUEST_TIMEOUT+" seconds.");
					e.printStackTrace();
				}
				
				WeatherData data = initializeWeatherDataObject(currentWeather, sensorServerAvailable, weathermapAccessible);

				String query = "INSERT INTO weatherdatalog (temperature, pressure, humidity, sensorwindspeed, owmwindspeed, owmwinddegree, light, owmweathername, owmweatherdesc)"
								+ " VALUES (" + data.getTemp() + ", " + data.getPressure() + ", " + data.getHumidity() + ", "
								+ data.getSensorWindSpeed() + ", " + data.getOwmWindSpeed() + ", " + data.getOwmWindDegree() + ", " + data.getLight()
								+ ", '"+ data.getOwmName() + "', '" + data.getOwmDesc() + "');";
				try {
					Statement statement = dbConnection.createStatement();
					System.out.println("Executing query...");
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
		 * @param sensorServerAvailable	If false, no data will be taken from the SensorClient.
		 * @param weathermapAccessible	If false, no data will be taken from OpenWeatherMap service.
		 * @return
		 */
		private WeatherData initializeWeatherDataObject(CurrentWeather currentWeather, boolean sensorServerAvailable, boolean weathermapAccessible) {
			WeatherData data = new WeatherData();
			if (weathermapAccessible) {
				if (sensorServerAvailable) {
					initWDwithSensorData(data);
					data.setOwmWindSpeed(currentWeather.getWindInstance().getWindSpeed());
					float temp = currentWeather.getWindInstance().getWindDegree();
					if (Float.compare(Float.NaN, temp) == 0) {
						data.setOwmWindDegree(0);
					} else {
						data.setOwmWindDegree(currentWeather.getWindInstance().getWindDegree());
					}
					data.setOwmDesc(currentWeather.getWeatherInstance(0).getWeatherDescription());
					data.setOwmName(currentWeather.getWeatherInstance(0).getWeatherName());
				} else {
					initWDwithOWMData(currentWeather, data);
					data.setLight(LIGHTDEFAULTVALUE);
					data.setSensorWindSpeed(WSPEEDDEFAULTVALUE);
				}
			} else {
				if (sensorServerAvailable) {
					initWDwithSensorData(data);
					data.setOwmDesc("N/A");
					data.setOwmName("N/A");
					data.setOwmWindSpeed(WSPEEDDEFAULTVALUE);
					data.setOwmWindDegree(WDEGREEDEFAULTVALUE);
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
			data.setSensorWindSpeed(WSPEEDDEFAULTVALUE);
			data.setOwmWindSpeed(WSPEEDDEFAULTVALUE);
			data.setOwmWindDegree(WDEGREEDEFAULTVALUE);
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
			data.setOwmWindSpeed(currentWeather.getWindInstance().getWindSpeed());
			data.setOwmWindDegree(currentWeather.getWindInstance().getWindDegree());
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
			data.setPressure(client.getPressure()/100);
			data.setHumidity(client.getHumidity());
			data.setSensorWindSpeed(client.getWindSpeed());
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
		 * @param sensorServerAvailable  If true, data is collected from given SensorClient (where possible). Otherwise, all data
		 * is taken from openweathermap.org.
		 */
		public DataCollector(SensorClientInterface client, boolean sensorServerAvailable) {
			this.client = client;
			this.sensorServerAvailable = sensorServerAvailable;
			if (client == null) {
				sensorServerAddress = "192.168.2.123";
			} else {
				sensorServerAddress = client.getSensorServerAddress();
			}
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
	 * @param sensorServerAvailable  If true, the collector gets its data from the given SensorClient (where possible). Otherwise, all data
	 * is taken from openweathermap.org.
	 */
	public DataBaseManager(SensorClientInterface client, boolean sensorServerAvailable) {
		collector = new DataCollector(client, sensorServerAvailable);
		collector.start();
	}
}
