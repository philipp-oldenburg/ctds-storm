package weather;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.IRichSpout;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;
import backtype.storm.utils.Utils;

public class DataBaseSpout implements IRichSpout {

	private SpoutOutputCollector collector;
	
	private static final double EMISSION_PERIOD = 5000; // 5 seconds
	private long lastEmission;

	private Socket socket;
	private PrintWriter dout;
	private BufferedReader din;
	
	private static final String SERVER_IP = "192.168.2.124";
	private static final int SERVER_PORT = 9001;
	
	private static final SimpleDateFormat TIMESTAMP_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	@Override
	public void ack(Object arg0) {}

	@Override
	public void activate() {}

	@Override
	public void close() {}

	@Override
	public void deactivate() {}

	@Override
	public void fail(Object arg0) {}

	/**
	 * gets from server and passes on first sensor then owm data
	 */
	@Override
	public void nextTuple() {
		if (lastEmission == 0 || lastEmission + EMISSION_PERIOD <= System.currentTimeMillis()) {
			
			Values result = fetchReading(false);
			if (result != null) collector.emit(result);
			result = fetchReading(true);
			if (result != null) collector.emit(result);
			
            lastEmission = System.currentTimeMillis();
            return;
	    }
	    Utils.sleep(50);
	}
	
//	new Values(
//		"2015-12-23 15:37:31",
//		6.122222476535374, 995.0, 75.0, 3.240000009536743,
//		5.822221967909072, 996.0, 81.0, 4.539999961853027);

	private Values fetchReading(boolean owm) {
		String reading = null;
		try {
			reading = requestAndWaitForResponse(owm ? "NEWOWM" : "NEWSENS");
			System.out.println("Response to '" + (owm ? "NEWOWM" : "NEWSENS") + "': " + reading);
			JSONObject wrapperjson = new JSONObject(reading);
			if (wrapperjson != null && wrapperjson.has("0")) {
				JSONObject json = (JSONObject) wrapperjson.get("0");
				if (json != null) {
					String timestamp = json.getString("timestamp");
					long time;
					try {
						time = TIMESTAMP_FORMAT.parse(timestamp).getTime();
					} catch (ParseException e) {
						time = System.currentTimeMillis();
					}
					String newTimeStamp = TIMESTAMP_FORMAT.format(new Date(time - 3600000));
					reading = requestAndWaitForResponse(newTimeStamp + ";" + (owm ? "OWM" : "SENS"));
					System.out.println("Response to '" + newTimeStamp + ";" + (owm ? "OWM" : "SENS") + "': " + reading);
					wrapperjson = new JSONObject(reading);
					if (wrapperjson != null && wrapperjson.has("0")) {
						JSONObject jsonOld = (JSONObject) wrapperjson.get("0");
						if (jsonOld != null) {
							return new Values(
									json.getString("timestamp"),
									json.getDouble(owm ? "owmtemperature" : "temperature"),
									json.getDouble(owm ? "owmpressure" : "pressure"),
									json.getDouble(owm ? "owmhumidity" : "humidity"),
									json.getDouble(owm ? "owmwindspeed" : "sensorwindspeed"),
									jsonOld.getDouble("temperature"),
									jsonOld.getDouble("pressure"),
									jsonOld.getDouble("humidity"),
									jsonOld.getDouble("windspeed"),
									owm ? "OWM" : "SENS");
						}
					}
				}
			}
			
			return null;
		} catch (IOException e) {
			System.err.println("IO exception in fetchReading() in DataBaseSpout: " + e.getMessage());
			return null;
		} catch (JSONException e) {
			System.err.println("JSON exception in fetchReading() in DataBaseSpout: " + e.getMessage());
			return null;
		} catch (Exception e) {
			System.err.println("Unknown exception in fetchReading() in DataBaseSpout: " + e.getMessage());
			return null;
		}
		
	}
	
	private String requestAndWaitForResponse(String request) throws IOException{
		dout.println(request);
		dout.flush();
		
		String res = din.readLine();
		return res;
	}

	@Override
	public void open(Map arg0, TopologyContext arg1, SpoutOutputCollector arg2) {
		try {
			socket = new Socket(SERVER_IP, SERVER_PORT);
			dout = new PrintWriter(socket.getOutputStream());
			din = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			socket.setSoTimeout(2000);
		} catch (IOException e1) {
			System.err.println("DAT: COULD NOT CONNECT TO SERVER ON IP " + SERVER_IP + " PORT " + SERVER_PORT);
			e1.printStackTrace();
		}
		collector = arg2;
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer arg0) {
		arg0.declare(new Fields("ctime", "ctemp", "cpres", "chumi", "cwind", "otemp", "opres", "ohumi", "owind", "sourc"));
	}

	@Override
	public Map<String, Object> getComponentConfiguration() {
		return null;
	}

}
