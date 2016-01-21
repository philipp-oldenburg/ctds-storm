import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketImpl;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import net.aksingh.owmjapis.CurrentWeather;
import net.aksingh.owmjapis.OpenWeatherMap;

public class DataBaseManager {
	
	private DataCollector collector;
	
	private String url = "jdbc:mysql://localhost:3306/ctds_db_test";
	private String user = "CTDS_DB_User";
	private String password = "password";
	
	
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
	
	/** Does exactly what is says. Shouldn't fail.
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
		boolean reconnectorRunning = false;
		private Connection dbConnection;
		private boolean sensorServerAvailable;
		private boolean weathermapAccessible;
		
		private String sensorServerAddress;

		private OpenWeatherMap owmap;
		
		private class SCReconnector extends Thread {
			
			public void run() {
				reconnectorRunning = true;
				System.out.println("Started reconnector");
				while (true) {
					try {
						client = new SensorClient(sensorServerAddress);
						System.out.println("created new client");
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
						//e.printStackTrace();
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e1) {
							e1.printStackTrace();
						}
						continue;
					}
				}
				reconnectorRunning = false;
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
					CurrentWeather currentWeather = owmap.currentWeatherByCityName("Basel", "CH");
					if (currentWeather.getMainInstance() == null || currentWeather.getWindInstance() == null) {
						throw new IOException();
					}
					return currentWeather;
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
					System.out.println("Received current Weather from OpenWeatherMap.");
				} catch (ExecutionException e) {
					System.out.println("OpenWeatherMap appears to be inaccessible or main/wind instance could not be retrieved.");
					weathermapAccessible = false;
					e.printStackTrace();
				} catch (InterruptedException e) {
					weathermapAccessible = false;
					System.out.println("Request interrupted while waiting for response.");
					e.printStackTrace();
				} catch (TimeoutException e) {
					weathermapAccessible = false;
					System.out.println("Did not receive response within "+WEATHER_REQUEST_TIMEOUT+" seconds.");
					e.printStackTrace();
				}
				
				try {
					client.ping();
					sensorServerAvailable = true;
					System.out.println("SensorServer is available");
				} catch (Exception e) {
					sensorServerAvailable = false;
					e.printStackTrace();
					System.out.println("Client unreachable. Trying to reconnect...");
					if (!reconnectorRunning) {
						SCReconnector reconnector = new SCReconnector();
						reconnector.start();
					}
				}
				
//				try {
//					sensorServerAvailable = isPortReachable(sensorServerAddress, 1337);
////					if (client == null && sensorServerAvailable) {
////						client = new SensorClient(sensorServerAddress);
////					}
//					System.out.println("SensorServer is available");
//				} catch (UnknownHostException e1) {
//					if (!reconnectorRunning) {
//						SCReconnector reconnector = new SCReconnector();
//						reconnector.start();
//					}
//					e1.printStackTrace();
//				} catch (IOException e1) {
//					if (!reconnectorRunning) {
//						SCReconnector reconnector = new SCReconnector();
//						reconnector.start();
//					}
//					e1.printStackTrace();
//				}
				System.out.println("Creating WeatherData object...");
				
				WeatherData data = initializeWeatherDataObject(currentWeather, sensorServerAvailable, weathermapAccessible);
				System.out.println("Created WeatherData object");
				String query = "INSERT INTO weatherdatalog (temperature, pressure, humidity, owmtemperature, owmpressure, owmhumidity, sensorwindspeed, owmwindspeed, owmwinddegree, light, owmweathername, owmweatherdesc)"
								+ " VALUES (" + data.getTemp() + ", " + data.getPressure() + ", " + data.getHumidity() + ", "
								+ data.getOwmTemp() + ", " + data.getOwmPressure() + ", " + data.getOwmHumidity() + ", "
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
			System.out.println("Created WD instance");
			if (weathermapAccessible) {
				if (sensorServerAvailable && client != null) {
					System.out.println("WM accessible, ss available and client != null");
					initWDwithSensorData(data);
					System.out.println("completed init with sensor data");
					initWDwithOWMData(currentWeather, data);
					System.out.println("completed init with owm data");
				} else {
					System.out.println("WM accessible, ss unavailable");
					initWDwithOWMData(currentWeather, data);
					initWDwithSensorDefaults(data);
				}
			} else {
				if (sensorServerAvailable && client != null) {
					System.out.println("WM not accessible, ss available and client != null");
					initWDwithSensorData(data);
					initWDwithOwmDefaults(data);
				} else {
					System.out.println("WM not accessible, ss unavailable");
					initWDwithDefaultData(data);
				}
			}
			System.out.println("Completed input of data");
			return data;
		}

		private void initWDwithSensorDefaults(WeatherData data) {
			data.setTemp(TEMPDEFAULTVALUE);
			data.setPressure(PRESDEFAULTVALUE);
			data.setHumidity(HUMDEFAULTVALUE);
			data.setLight(LIGHTDEFAULTVALUE);
			data.setSensorWindSpeed(WSPEEDDEFAULTVALUE);
		}

		private void initWDwithOwmDefaults(WeatherData data) {
			data.setOwmTemp(TEMPDEFAULTVALUE);
			data.setOwmPressure(PRESDEFAULTVALUE);
			data.setOwmHumidity(HUMDEFAULTVALUE);
			data.setOwmDesc("N/A");
			data.setOwmName("N/A");
			data.setOwmWindSpeed(WSPEEDDEFAULTVALUE);
			data.setOwmWindDegree(WDEGREEDEFAULTVALUE); 
		}
		
		/**Sets all fields to default values, indicating that no sensor/OWM-data is present.
		 * 
		 * @param data	WeatherData object to be initialized.
		 */
		private void initWDwithDefaultData(WeatherData data) {
			data.setOwmTemp(TEMPDEFAULTVALUE);
			data.setOwmPressure(PRESDEFAULTVALUE);
			data.setOwmHumidity(HUMDEFAULTVALUE);
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
			try {
				data.setOwmTemp(fahrenheitToCelsius(currentWeather.getMainInstance().getTemperature()));
				data.setOwmPressure(currentWeather.getMainInstance().getPressure());
				data.setOwmHumidity(currentWeather.getMainInstance().getHumidity());
				data.setOwmWindSpeed(currentWeather.getWindInstance().getWindSpeed());
				data.setOwmWindDegree(currentWeather.getWindInstance().getWindDegree());
				data.setOwmDesc(currentWeather.getWeatherInstance(0).getWeatherDescription());
				data.setOwmName(currentWeather.getWeatherInstance(0).getWeatherName());
			} catch (NullPointerException e) {
				System.out.println("Could not get main/wind/weather instance of currentWeather.\n Should have thrown exception earlier. Something's seriously wrong here.");
				e.printStackTrace();
				initWDwithOwmDefaults(data);
			}
		}

		/**Initializes data with all the values which the connected sensor array provides. WindDegree, WeatherDescription and WeatherName are
		 * only provided by OpenWeatherMap and therefore missing.
		 * 
		 * @param data	WeatherData object to be initialized.
		 */
		private void initWDwithSensorData(WeatherData data) {
			data.setTemp(client.getTemperature());
			System.out.println("set temp");
			data.setPressure(client.getPressure()/100);
			System.out.println("set pres");
			data.setHumidity(client.getHumidity());
			System.out.println("set hum");
			data.setSensorWindSpeed(client.getWindSpeed());
			System.out.println("set ws");
			data.setLight(client.getLight());
			System.out.println("set light");
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
		
		WebServerServer wss = new WebServerServer();
		wss.start();
	}

	private boolean isValidTimestamp(String timestamp) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			format.parse(timestamp);
			return true;
		} catch (ParseException e) {
			return false;
		}
	}
	
	private class SpoutServer extends Thread {
		
		private static final int PORT = 9002;
		
		public void run() {
			
			
			
			
//			try (
//				ServerSocket server = new ServerSocket(PORT);
//				Socket client = server.accept();
//				OutputStreamWriter out = new OutputStreamWriter(client.getOutputStream(), StandardCharsets.UTF_8);
//				BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
//			) {
//				String input, output;
//				while((input = in.readLine()) != null) {
//					System.out.println("Processing WebServer request...");
//					String[] timestamps = input.split(";");
//					for (String timestamp : timestamps) {
//						if (!isValidTimestamp(timestamp)) System.out.println("Invalid timestamp detected."); break;
//					}
//					
//					registerDriver();
//					Connection conn = establishConnection();
//					System.out.println("Established DBConnection for WebServer.");
//					String query = "SELECT * FROM weatherdatalog WHERE timestamp BETWEEN '" + timestamps[0] + "' AND '" + timestamps[1] + "';";
//					
//					try {
//						Statement statement = conn.createStatement();
//						ResultSet rs = statement.executeQuery(query);
//						System.out.println("Processed SQL query.");
//						JSONObject jretobj = new JSONObject();
//						while(rs.next()) {
//							JSONObject currentObject = new JSONObject();
//							currentObject.put("timestamp", rs.getTimestamp("timestamp").toString());
//							currentObject.put("owmtemperature", rs.getDouble("owmtemperature"));
//							currentObject.put("owmpressure", rs.getDouble("owmpressure"));
//							currentObject.put("owmhumidity", rs.getDouble("owmhumidity"));
//							currentObject.put("temperature", rs.getDouble("temperature"));
//							currentObject.put("pressure", rs.getDouble("pressure"));
//							currentObject.put("humidity", rs.getDouble("humidity"));
//							currentObject.put("sensorwindspeed", rs.getDouble("sensorwindspeed"));
//							currentObject.put("owmwindspeed", rs.getDouble("owmwindspeed"));
//							currentObject.put("owmwinddegree", rs.getDouble("owmwinddegree"));
//							currentObject.put("light", rs.getDouble("light"));
//							currentObject.put("owmweathername", rs.getString("owmweathername"));
//							currentObject.put("owmweatherdesc", rs.getString("owmweatherdesc"));
//							
//							System.out.println("Created current object");
//							
//							jretobj.put(rs.getTimestamp("timestamp").toString(), currentObject);
//						}
//						out.write(jretobj.toString() + System.getProperty("line.separator"));
//						out.flush();
//						System.out.println("Sent JSON object");
//						
//					} catch (SQLException | JSONException e) {
//						System.out.println("Unable to retrieve data from specified interval.");
//						e.printStackTrace();
//						out.write("1337");
//						out.flush();
//					}
//				}
//				
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
		}
	}
	
	private class WebServerServer extends Thread {
		
		private static final int PORT = 9001;
		
		public void run() {
			ServerSocket server = null;
			try {
				server = new ServerSocket(PORT);
			} catch (IOException e) {
				System.out.println("Port already in use");
				e.printStackTrace();
				return;
			}
			while(true) {
				Socket client;
				System.out.println("waiting for client");
				try {
					client = server.accept();
				} catch (IOException e1) {
					e1.printStackTrace();
					continue;
				}
				
				Thread connection = new Thread() {
					
					private Socket client;
					private ServerSocket server;
					
					public Thread init(ServerSocket server, Socket client) {
						this.server = server;
						this.client = client;
						return this;
					}
					
					public void run() {
						try {
							OutputStreamWriter out = new OutputStreamWriter(client.getOutputStream(), StandardCharsets.UTF_8);
							BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));

							System.out.println("started WebServerServer");
							String input, output;
							while((input = in.readLine()) != null) {
								System.out.println("Processing WebServer request...");
								if (input.equals("NEWDATA")) {
									
								}
								String[] timestamps = input.split(";");
								for (String timestamp : timestamps) {
									if (!isValidTimestamp(timestamp)) {
										out.write("IMD" + System.getProperty("line.separator"));
										out.flush();
										System.out.println("Invalid message detected.");
										continue;
									}
								}
								
								registerDriver();
								Connection conn = establishConnection();
								System.out.println("Established DBConnection for WebServer.");
								String query = "SELECT * FROM weatherdatalog WHERE timestamp BETWEEN '" + timestamps[0] + "' AND '" + timestamps[1] + "';";
								
								try {
									Statement statement = conn.createStatement();
									ResultSet rs = statement.executeQuery(query);
									System.out.println("Processed SQL query.");
									JSONObject jretobj = new JSONObject();
									int counter = 0;
									while(rs.next()) {
										JSONObject currentObject = new JSONObject();
										currentObject.put("timestamp", rs.getTimestamp("timestamp").toString());
										currentObject.put("owmtemperature", rs.getDouble("owmtemperature"));
										currentObject.put("owmpressure", rs.getDouble("owmpressure"));
										currentObject.put("owmhumidity", rs.getDouble("owmhumidity"));
										currentObject.put("temperature", rs.getDouble("temperature"));
										currentObject.put("pressure", rs.getDouble("pressure"));
										currentObject.put("humidity", rs.getDouble("humidity"));
										currentObject.put("sensorwindspeed", rs.getDouble("sensorwindspeed"));
										currentObject.put("owmwindspeed", rs.getDouble("owmwindspeed"));
										currentObject.put("owmwinddegree", rs.getDouble("owmwinddegree"));
										currentObject.put("light", rs.getDouble("light"));
										currentObject.put("owmweathername", rs.getString("owmweathername"));
										currentObject.put("owmweatherdesc", rs.getString("owmweatherdesc"));
										
										System.out.println("Created current object");
										
										jretobj.put(Integer.toString(counter), currentObject);
										counter++;
									}
									JSONObject tempObj = new JSONObject();
									jretobj.put("length", counter+1);
									out.write(jretobj.toString() + System.getProperty("line.separator"));
									out.flush();
									System.out.println("Sent JSON object");
									
								} catch (SQLException | JSONException e) {
									System.out.println("Unable to retrieve data from specified interval.");
									e.printStackTrace();
									out.write("CNRD" + System.getProperty("line.separator"));
									out.flush();
								}
							}
						} catch (IOException e) {
							System.out.println("Connection on Port 9001 to "+ client.getInetAddress() +" was closed.");
							e.printStackTrace();
						}
					}
				}.init(server, client);
				connection.start();
			}
		}
	}
	
}
