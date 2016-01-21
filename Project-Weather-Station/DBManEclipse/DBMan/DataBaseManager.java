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
import java.util.Arrays;
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
					} catch(Exception e) {
						System.out.println("Could not reconnect to SensorServer. Still trying...");
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e1) {
							//e1.printStackTrace();
						}
						continue;
					}
				}
				reconnectorRunning = false;
			}
		}
		
		private CurrentWeather getCurrentWeatherWithTimeout(CurrentWeather currentWeather) throws ExecutionException, InterruptedException, TimeoutException {
			
			ExecutorService executor = Executors.newCachedThreadPool();
			
			Callable<CurrentWeather> task = new Callable<CurrentWeather>() {
				public CurrentWeather call() throws IOException, JSONException {
					CurrentWeather currentWeather = owmap.currentWeatherByCityName("Basel", "CH");
					return currentWeather;
				}
			};
			
			Future<CurrentWeather> future = executor.submit(task);
			currentWeather = future.get(WEATHER_REQUEST_TIMEOUT, TimeUnit.SECONDS);
			
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
					System.out.println("OpenWeatherMap appears to be inaccessible.");
					weathermapAccessible = false;
					//e.printStackTrace();
				} catch (InterruptedException e) {
					weathermapAccessible = false;
					System.out.println("Request interrupted while waiting for response.");
					//e.printStackTrace();
				} catch (TimeoutException e) {
					weathermapAccessible = false;
					System.out.println("Did not receive response within "+WEATHER_REQUEST_TIMEOUT+" seconds.");
					//e.printStackTrace();
				}
				
				try {
					client.ping();
					sensorServerAvailable = true;
					System.out.println("SensorServer is available");
				} catch (Exception e) {
					sensorServerAvailable = false;
					//e.printStackTrace();
					System.out.println("Client unreachable. Trying to reconnect...");
					if (!reconnectorRunning) {
						SCReconnector reconnector = new SCReconnector();
						reconnector.start();
					}
				}
				
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
				if (currentWeather.getMainInstance() != null) {
					data.setOwmTemp(fahrenheitToCelsius(currentWeather.getMainInstance().getTemperature()));
					data.setOwmPressure(currentWeather.getMainInstance().getPressure());
					data.setOwmHumidity(currentWeather.getMainInstance().getHumidity());
				} else {
					data.setOwmTemp(TEMPDEFAULTVALUE);
					data.setOwmPressure(PRESDEFAULTVALUE);
					data.setOwmHumidity(HUMDEFAULTVALUE);
				}
				
				if (currentWeather.getWindInstance() != null) {
					data.setOwmWindSpeed(currentWeather.getWindInstance().getWindSpeed());
					data.setOwmWindDegree(currentWeather.getWindInstance().getWindDegree());
				} else {
					data.setOwmWindSpeed(WSPEEDDEFAULTVALUE);
					data.setOwmWindDegree(WDEGREEDEFAULTVALUE);
				}
				
				if ((currentWeather.getWeatherCount() > 0) && (currentWeather.getWeatherInstance(0) != null)) {
					data.setOwmDesc(currentWeather.getWeatherInstance(0).getWeatherDescription());
					data.setOwmName(currentWeather.getWeatherInstance(0).getWeatherName());
				} else {
					data.setOwmDesc("N/A");
					data.setOwmName("N/A");
				}
				
				if (currentWeather.getRainInstance() != null) {
					
					if (currentWeather.getRainInstance().hasRain1h()) System.out.println(currentWeather.getRainInstance().getRain1h());
					if (currentWeather.getRainInstance().hasRain3h()) System.out.println(currentWeather.getRainInstance().getRain3h());
					
				}
				
				if (currentWeather.getCloudsInstance() != null) {
					if (currentWeather.getCloudsInstance().hasPercentageOfClouds()) System.out.println(currentWeather.getCloudsInstance().getPercentageOfClouds());
				}
				
			} catch (NullPointerException e) {
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
		
		DataProvider provider = new DataProvider();
		provider.start();
	}
	
	/**Checks if given String is of the following SimpleDateFormat: "yyyy-MM-dd HH:mm:ss"
	 * 
	 * @param timestamp
	 * @return
	 */
	private boolean isValidTimestamp(String timestamp) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			format.parse(timestamp);
			return true;
		} catch (ParseException e) {
			return false;
		}
	}
	
	private class DataProvider extends Thread {
		
		private static final int PORT = 9001;
		
		/**Waits for connection on port 9001 and accepts connection in new thread, providing the following methods for acquisition of data:<br>
		 * 	-Replies to "NEWDATA" with latest data tuple.<br>
		 * 	-Replies to "yyyy-MM-dd HH:mm:ss" with the data tuple which is closest, yet still older than the given timestamp.<br>
		 * 	-Replies to "yyyy-MM-dd HH:mm:ss;yyyy-MM-dd HH:mm:ss;(distinct)" with all data tuples in specified interval. Replace (distinct) with either
		 * 	"TRUE" or "FALSE" to get distinct or all results.<br>
		 * 	-Replies to "yyyy-MM-dd HH:mm:ss;yyyy-MM-dd HH:mm:ss;(distinct);(arg1);(arg2);...;(argX)" with all  data tuples in specified interval. Replace (distinct)
		 *  with either "TRUE" or "FALSE" to get distinct or all results. Use args to specify the columns which shall be returned.<br>
		 * Error codes: "CNRD" if data could not be read (Due to SQL or JSONException), "IMD" if invalid message is detected.
		 */
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
									sendLatestDataVector(out);
								} else {
									String[] inputParts = input.split(";");
									if (inputParts.length == 1) {
										if(!isValidTimestamp(inputParts[0])) {
											out.write("IMD" + System.getProperty("line.separator"));
											out.flush();
											System.out.println("Invalid message detected.");
										} else {
											System.out.println("Requested closest data Vector");
											sendDataVectorClosest(out, inputParts[0]);
										}
									} else if (inputParts.length == 3) {
										
										if (isValidTimestamp(inputParts[0]) && isValidTimestamp(inputParts[1])) {
											if (inputParts[2].equals("TRUE")) {
												System.out.println("Requested closest data Vector");
												sendDataVectorsInRange(out, inputParts, null, true);
											} else if (inputParts[2].equals("FALSE")){
												System.out.println("Requested closest data Vector");
												sendDataVectorsInRange(out, inputParts, null, false);
											} else {
												out.write("IMD" + System.getProperty("line.separator"));
												out.flush();
												System.out.println("Invalid message detected.");
											}
										} else {
											out.write("IMD" + System.getProperty("line.separator"));
											out.flush();
											System.out.println("Invalid message detected.");
										}
									} else if (inputParts.length > 3) {
										
										if (isValidTimestamp(inputParts[0]) && isValidTimestamp(inputParts[1])) {
											
											String[] columns = new String[inputParts.length-3];
											for (int i=3; i < inputParts.length; i++) {
												columns[i-3] = inputParts[i];
											}
											
											if (inputParts[2].equals("TRUE")) {
												System.out.println("Requested closest data Vector");
												sendDataVectorsInRange(out, inputParts, columns, true);
											} else if (inputParts[2].equals("FALSE")){
												System.out.println("Requested closest data Vector");
												sendDataVectorsInRange(out, inputParts, columns, false);
											} else {
												out.write("IMD" + System.getProperty("line.separator"));
												out.flush();
												System.out.println("Invalid message detected.");
											}
										} else {
											out.write("IMD" + System.getProperty("line.separator"));
											out.flush();
											System.out.println("Invalid message detected.");
										}
									}
								}
							}
						} catch (IOException e) {
							System.out.println("Connection on Port 9001 to "+ client.getInetAddress() +" was closed.");
							e.printStackTrace();
						}
					}
					
					private void sendDataVectorClosest(OutputStreamWriter out, String timestamp) throws IOException {
						
						registerDriver();
						Connection conn = establishConnection();
						System.out.println("Established DBConnection for WebServer.");
						String query = "SELECT * FROM weatherdatalog WHERE timestamp < '"+ timestamp +"' ORDER BY id DESC LIMIT 1;";
						
						try {
							Statement statement = conn.createStatement();
							ResultSet rs = statement.executeQuery(query);
							System.out.println("Processed SQL query.");
							JSONObject jretobj = new JSONObject();
							while(rs.next()) {
								JSONObject currentObject = initCurrentJSONObject(rs);
								
								jretobj.put("0", currentObject);
							}
							jretobj.put("length", 1);
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

					private void sendDataVectorsInRange(OutputStreamWriter out, String[] timestamps, String[] columns, boolean distinct)
							throws IOException {
						registerDriver();
						Connection conn = establishConnection();
						System.out.println("Established DBConnection for WebServer.");
						String query = "SELECT ";
						if(columns != null) {
							for (int i=0; i < columns.length-1; i++) {
								query += columns[i] + ", ";
							}
							query += columns[columns.length-1];
						}
						query += " FROM weatherdatalog WHERE timestamp BETWEEN '" + timestamps[0] + "' AND '" + timestamps[1] + "';";
						
						try {
							Statement statement = conn.createStatement();
							ResultSet rs = statement.executeQuery(query);
							System.out.println("Processed SQL query.");
							JSONObject jretobj = new JSONObject();
							int counter = 0;
							String[] prevVals = new String[columns.length];
							Arrays.fill(prevVals, "");
							while(rs.next()) {
								
								if (distinct) {
									boolean changed = false;
									for (int i = 0; i < prevVals.length; i++) {
										String tmp = Double.toString(rs.getDouble(columns[i]));
										if (!tmp.equals(prevVals[i])) {
											changed = true;
											prevVals[i] = tmp;
										}
									}
									if (!changed)
										continue;
								}
								
								JSONObject currentObject = initCurrentJSONObjectPicky(columns, prevVals);
								
								jretobj.put(Integer.toString(counter), currentObject);
								counter++;
							}
							jretobj.put("length", counter);
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
					
					private void sendLatestDataVector(OutputStreamWriter out) throws IOException {
						registerDriver();
						Connection conn = establishConnection();
						System.out.println("Established DBConnection for WebServer.");
						String query = "SELECT * FROM weatherdatalog ORDER BY id DESC LIMIT 1;";
						
						try {
							Statement statement = conn.createStatement();
							ResultSet rs = statement.executeQuery(query);
							System.out.println("Processed SQL query.");
							JSONObject jretobj = new JSONObject();
							while(rs.next()) {
								JSONObject currentObject = initCurrentJSONObject(rs);
								
								jretobj.put("0", currentObject);
							}
							jretobj.put("length", 1);
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
					
					private JSONObject initCurrentJSONObjectPicky(String[] columns, String[] prevVals) throws JSONException {
						JSONObject currentObject = new JSONObject();
						for (int i=0; i < columns.length; i++) {
							currentObject.put(columns[i], prevVals[i]);
						}
						return currentObject;
					}

					
					private JSONObject initCurrentJSONObject(ResultSet rs) throws JSONException, SQLException {
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
						return currentObject;
					}
					
				}.init(server, client);
				connection.start();
			}
		}
	}
}
