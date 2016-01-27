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
import java.util.Date;
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
	
	private static final int BOLTPORT = 9002;

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
		
		private static final long ACQUIREDATAINTERVAL = 15000;
		
		private static final double TEMPDEFAULTVALUE = -300;
		private static final double PRESDEFAULTVALUE = -1;
		private static final double HUMDEFAULTVALUE = -1;
		private static final double WSPEEDDEFAULTVALUE = -1;
		private static final float WDEGREEDEFAULTVALUE = -1;
		private static final double LIGHTDEFAULTVALUE = -1;

		private static final long WEATHER_REQUEST_TIMEOUT = 2;
		
		private SensorClientMkII client;
		private Receiver receiver;
		boolean reconnectorRunning = false;
		private Connection dbConnection;
		private boolean sensorServerAvailable;
		private boolean weathermapAccessible;
		
		private double curSensTemp;
		private double curSensPres;
		private double curSensHumi;
		private double curSensWind;
		private double curSensLumi;
		private JSONObject curSensData;
		
		private String sensorServerAddress;

		private OpenWeatherMap owmap;
		

		/**Constructor
		 * 
		 * @param cli SensorClient instance which the collector shall use to acquire data.
		 * @param sensorServerAvailable  If true, data is collected from given SensorClient (where possible). Otherwise, all data
		 * is taken from openweathermap.org.
		 */
		public DataCollector(SensorClientMkII cli, boolean sensorServerAvailable) {
			this.client = cli;
			this.sensorServerAvailable = sensorServerAvailable;
			if (cli == null) {
				sensorServerAddress = "deffi.thecuslink.com";
			} else {
				sensorServerAddress = cli.getSensorServerAddress();
			}
			receiver = new Receiver() {

				@Override
				public void receivedTemperature(double temperature) {
					curSensTemp = temperature;
				}

				@Override
				public void receivedPressure(double pressure) {
					curSensPres = pressure;
				}

				@Override
				public void receivedAltitude(double altitude) {}

				@Override
				public void receivedSealevelPressure(double sealevelpressure) {}

				@Override
				public void receivedHumidity(double humidity) {
					curSensHumi = humidity;
				}

				@Override
				public void receivedLight(double light) {
					curSensLumi = light;
				}

				@Override
				public void receivedWindSpeed(double windspeed) {
					curSensWind = windspeed;
				}

				@Override
				public void receivedAllData(JSONObject json) {
					curSensData = json;
				}

				@Override
				public void receivedpong() {}
				
				@Override
				public void connectionReset() {
					System.out.println("SensorClient unreachable.");
					if (!reconnectorRunning) {
						SCReconnector reconnector = new SCReconnector(this);
						reconnector.start();
					}
				}
				
			};
			client.init(receiver);
		}
		
		private class SCReconnector extends Thread {
			private Receiver rec;
			
			public SCReconnector(Receiver r) {
				rec = r;
			}
			
			public void run() {
				reconnectorRunning = true;
				System.out.println("Started reconnector");
				while (true) {
					try {
						client = new SensorClientMkII(sensorServerAddress, 1338);
						System.out.println("created new SensorClient");
						client.init(rec);
//						sensorServerAvailable = true;
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
				if (client != null) {
					client.requestAllData();
					try {
						client.requestWindSpeed();
					} catch (Exception e1) {
						System.out.println("Failed to request wind speed.");
						e1.printStackTrace();
					}
				}
				//Not sure if needed
//				else {
//					System.out.println("SensorClient unreachable.");
//					if (!reconnectorRunning) {
//						SCReconnector reconnector = new SCReconnector();
//						reconnector.start();
//					}
//				}
				System.out.println("---------------------------------------");
				Date date = new Date();
				System.out.println(date.toString());
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
					System.out.println("Did not receive response from openweathermap within "+WEATHER_REQUEST_TIMEOUT+" seconds.");
					//e.printStackTrace();
				}
				sensorServerAvailable = true;
				
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
			
			
			try {
				data.setTemp(curSensData.getDouble("temperature"));
				System.out.println("set temp");
			} catch (Exception e) {
				data.setTemp(TEMPDEFAULTVALUE);
			}
			
			try {
				data.setPressure(curSensData.getDouble("pressure"));
				System.out.println("set pres");
			} catch (Exception e) {
				data.setPressure(PRESDEFAULTVALUE);
			}
			
			
			try {
				data.setHumidity(curSensData.getDouble("humidity"));			
				System.out.println("set hum");
			} catch (Exception e) {
				data.setHumidity(HUMDEFAULTVALUE);
			}
			
			try {
				data.setSensorWindSpeed(curSensWind);
			} catch (Exception e1) {
				data.setSensorWindSpeed(WSPEEDDEFAULTVALUE);
			}
//			data.setSensorWindSpeed(WSPEEDDEFAULTVALUE);
			System.out.println("set ws");
			
			
			try {
				data.setLight(curSensData.getDouble("luminosity"));
				System.out.println("set light");
			} catch (Exception e) {
				data.setLight(LIGHTDEFAULTVALUE);
			}
		}
		
		
		/**Damn 'Muricans.
		 * 
		 * @param temperature temperature in degrees fahrenheit.
		 * @return	temperature in degrees celsius.
		 */
		private double fahrenheitToCelsius(float temperature) {
			return ((5.0/9.0) * (temperature - 32.0));
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
	public DataBaseManager(SensorClientMkII client, boolean sensorServerAvailable) {
		collector = new DataCollector(client, sensorServerAvailable);
		collector.start();
		
		DataProvider provider = new DataProvider();
		provider.start();
		
		
		
		ServerSocket boltServer = null;
		try {
			boltServer = new ServerSocket(BOLTPORT);
		} catch (IOException e) {
			System.out.println("Port already in use");
			e.printStackTrace();
			return;
		}
		
		Socket resultBolt = null;
		System.out.println("waiting for result Bolt");
		try {
			resultBolt = boltServer.accept();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		Thread resultBoltConnection = new Thread() {
			
			private Socket client;
			private ServerSocket server;
			private DataProvider provider;
			
			public Thread init(ServerSocket server, Socket client, DataProvider provider) {
				this.server = server;
				this.client = client;
				this.provider = provider;
				return this;
			}
			public void run() {
				try {
					OutputStreamWriter out = new OutputStreamWriter(client.getOutputStream(), StandardCharsets.UTF_8);
					BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
					
					String input;
					
					while((input = in.readLine()) != null) {
						JSONObject temp = null;
						try {
							temp = new JSONObject(input);
							if(temp.get("source").equals("OWM")) {
								provider.setCurrentClassifiedWeatherOWM(temp);
							} else if (temp.get("source").equals("SENS")) {
								provider.setCurrentClassifiedWeatherSENS(temp);
							} else {
								provider.setCurrentClassifiedWeatherSENS(temp);
								provider.setCurrentClassifiedWeatherOWM(temp);
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
//						if (temp != null) {
//							System.out.println(temp.toString());
//						} else System.out.println("JSONObject from ResultBolt is null.");
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
		}.init(boltServer, resultBolt, provider);
		resultBoltConnection.start();
		
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
		private JSONObject currentOWMClass;
		private JSONObject currentSENSClass;
		
		public void setCurrentClassifiedWeatherOWM(JSONObject obj) {
			currentOWMClass = obj;
		}

		public void setCurrentClassifiedWeatherSENS(JSONObject obj) {
			currentSENSClass = obj;
		}
		
		/**Waits for connection on port 9001 and accepts connection in new thread, providing the following methods for acquisition of data:<br>
		 * 	-Replies to "NEWOWM" with latest data tuple which contains valid owm data.<br>
		 * 	-Replies to "NEWSENS" with latest data tuple which contains valid sensor data.<br>
		 * 	-Replies to "yyyy-MM-dd HH:mm:ss" with the data tuple which is closest, yet still older than the given timestamp.<br>
		 * 	-Replies to "yyyy-MM-dd HH:mm:ss;yyyy-MM-dd HH:mm:ss;(distinct)" with all data tuples in specified interval. Replace (distinct) with either
		 * 	"TRUE" or "FALSE" to get distinct or all results.<br>
		 * 	-Replies to "yyyy-MM-dd HH:mm:ss;yyyy-MM-dd HH:mm:ss;(distinct);(arg0);(arg1);...;(argX)" with all  data tuples in specified interval. Replace
		 * 	(distinct) with either "TRUE" or "FALSE" to get distinct or all results. Use args to specify the columns which shall be returned.<br>
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
								if (input.equals("NEWOWM") || input.equals("NEWSENS")) {
									sendLatestDataVector(out, input);
								} else if (input.equals("CLASSDATA")) {
									out.write(currentOWMClass.toString());
									out.flush();
								} else if (input.equals("CLASSSENS")) {
									out.write(currentSENSClass.toString());
									out.flush();
								} else {
									String[] inputParts = input.split(";");
									if (inputParts.length == 1) {
										if(!isValidTimestamp(inputParts[0])) {
											out.write("IMD" + System.getProperty("line.separator"));
											out.flush();
											System.out.println("Invalid message detected.");
										} else {
											System.out.println("Requested closest data Vector");
											sendDataVectorClosest(out, inputParts[0], null);
										}
									} else if (inputParts.length == 2) {
										if(!isValidTimestamp(inputParts[0]) || !(inputParts[1].equals("OWM") || inputParts[1].equals("SENS")) ) {
											out.write("IMD" + System.getProperty("line.separator"));
											out.flush();
											System.out.println("Invalid message detected.");
										} else {
											System.out.println("Requested newest meaningful data vector older than timestamp");
											sendDataVectorClosest(out, inputParts[0], inputParts[1]);
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
//							e.printStackTrace();
						}
					}
					
					private void sendDataVectorClosest(OutputStreamWriter out, String timestamp, String input) throws IOException {
						
						registerDriver();
						Connection conn = establishConnection();
						System.out.println("Established DBConnection for WebServer.");
						String query = "";
						if (input != null && input.equals("OWM")) {
							query += "SELECT timestamp, owmtemperature, owmpressure, owmhumidity, owmwindspeed FROM weatherdatalog WHERE timestamp < '"+ timestamp +"' ";
							query += "AND owmtemperature <> '-300' AND owmpressure <> '-1' AND owmhumidity <> '-1' AND owmwindspeed <> '-1' ";
						} else if (input != null && input.equals("SENS")) {
							query += "SELECT timestamp, temperature, pressure, humidity, sensorwindspeed FROM weatherdatalog WHERE timestamp < '"+ timestamp +"' ";
							query += "AND temperature <> '-300' AND pressure <> '-1' AND humidity <> '-1' AND sensorwindspeed <> '-1' ";
						}
						query += "ORDER BY id DESC LIMIT 1;";
						
						try {
							Statement statement = conn.createStatement();
							ResultSet rs = statement.executeQuery(query);
							System.out.println("Processed SQL query.");
							JSONObject jretobj = new JSONObject();
							while(rs.next()) {
								JSONObject currentObject = new JSONObject();
								if (input.equals("OWM")) {
									currentObject.put("temperature", Double.toString(rs.getDouble("owmtemperature")));
									currentObject.put("pressure", Double.toString(rs.getDouble("owmpressure")));
									currentObject.put("humidity", Double.toString(rs.getDouble("owmhumidity")));
									currentObject.put("windspeed", Double.toString(rs.getDouble("owmwindspeed")));
									currentObject.put("timestamp", rs.getTimestamp("timestamp").toString());
								} else if (input.equals("SENS")) {
									currentObject.put("temperature", Double.toString(rs.getDouble("temperature")));
									currentObject.put("pressure", Double.toString(rs.getDouble("pressure")));
									currentObject.put("humidity", Double.toString(rs.getDouble("humidity")));
									currentObject.put("windspeed", Double.toString(rs.getDouble("sensorwindspeed")));
									currentObject.put("timestamp", rs.getTimestamp("timestamp").toString());
								}
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
						boolean useAllColumns = (columns == null);
						String[] allColumns = {"id", "timestamp", "owmtemperature", "temperature", "owmpressure", "pressure", "owmhumidity", "humidity", "sensorwindspeed", "owmwindspeed", "owmwinddegree", "light", "owmweathername", "owmweatherdesc"};
						registerDriver();
						Connection conn = establishConnection();
						System.out.println("Established DBConnection for WebServer.");
						String query = "SELECT ";
						if(!useAllColumns) {
							for (int i=0; i < columns.length-1; i++) {
								query += columns[i] + ", ";
							}
							query += columns[columns.length-1];
							if (!query.contains("timestamp")) query += ", timestamp";
						} else {
							query += "*";
						}
						query += " FROM weatherdatalog WHERE ";
						if (columns.length == 1 && distinct) {
							if (columns[0].contains("temperature")) {
								query += columns[0] + " <> '-300'";
							} else if (columns[0].contains("pressure") || columns[0].contains("humidity") || columns[0].contains("windspeed")) {
								query += columns[0] + " <> '-1'";
							}
							
						}
						query += " AND timestamp BETWEEN '" + timestamps[0] + "' AND '" + timestamps[1] + "';";
						try {
							Statement statement = conn.createStatement();
							ResultSet rs = statement.executeQuery(query);
							System.out.println("Processed SQL query.");
							JSONObject jretobj = new JSONObject();
							int counter = 0;
							
							if (useAllColumns) columns = allColumns;
							
							String[] prevVals = null;
							if (distinct) {
								prevVals = new String[columns.length];
								Arrays.fill(prevVals, "");
							}
							while(rs.next()) {
								JSONObject currentObject;
								
								if (distinct) {
									boolean changed = false;
									
									for (int i = 0; i < prevVals.length; i++) {
										String tmp;
										if (columns[i].equals("owmweathername") || columns[i].equals("owmweatherdesc")) {
											tmp = rs.getString(columns[i]);
										} else if (columns[i].equals("timestamp")) {
											prevVals[i] = rs.getTimestamp("timestamp").toString();
											continue;
										} else if (columns[i].equals("id")) {
											prevVals[i] = Integer.toString(rs.getInt("id"));
											continue;
										} else {
											tmp = Double.toString(rs.getDouble(columns[i]));
										}
										
										if (!tmp.equals(prevVals[i])) {
											changed = true;
											prevVals[i] = tmp;
										}
									}
									
									if (!changed) continue;
									currentObject = initCurrentJSONObjectPicky(columns, prevVals, rs, true);
								} else {
									
									currentObject = initCurrentJSONObjectPicky(columns, null, rs, false);
									
								}
								
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
					
					private void sendLatestDataVector(OutputStreamWriter out, String input) throws IOException {
						registerDriver();
						Connection conn = establishConnection();
						System.out.println("Established DBConnection for WebServer.");
						String query = "SELECT * FROM weatherdatalog ";
						
						if (input.equals("NEWOWM")) {
							query += "WHERE owmtemperature <> '-300' AND owmpressure <> '-1' AND owmhumidity <> '-1' AND owmwindspeed <> '-1' ";
						} else {
							query += "WHERE temperature <> '-300' AND pressure <> '-1' AND humidity <> '-1' AND sensorwindspeed <> '-1' ";
						}
						
						query += "ORDER BY id DESC LIMIT 1;";
						
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
					
					private JSONObject initCurrentJSONObjectPicky(String[] columns, String[] prevVals, ResultSet rs, boolean distinct) throws JSONException, SQLException {
						JSONObject currentObject = new JSONObject();
						if (distinct) {
							boolean hasTimestamp = false;
							for (int i=0; i < columns.length; i++) {
								if (columns[i].equals("timestamp")) hasTimestamp = true;
								currentObject.put(columns[i], prevVals[i]);
							}
							if (!hasTimestamp) currentObject.put("timestamp", rs.getTimestamp("timestamp").toString());
						} else {
							for (int i=0; i < columns.length; i++) {
								if (columns[i].equals("owmweathername") || columns[i].equals("owmweatherdesc")) {
									currentObject.put(columns[i], rs.getString(columns[i]));
								} else if (columns[i].equals("timestamp")) {
								} else if (columns[i].equals("id")) {
									currentObject.put(columns[i], rs.getDouble(columns[i]));
								} else {
									currentObject.put(columns[i], rs.getDouble(columns[i]));
								}
							}
							currentObject.put("timestamp", rs.getTimestamp("timestamp").toString());
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