package weather;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
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
	
	private static final String SERVER_IP = "192.168.2.125";
	private static final int SERVER_PORT = 9001;
	
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

	@Override
	public void nextTuple() {
		if (lastEmission == 0 || lastEmission + EMISSION_PERIOD <= System.currentTimeMillis()) {
			
			Values result = fetchReading();
			if (result != null) collector.emit(result);
			
            lastEmission = System.currentTimeMillis();
            return;
	    }
	    Utils.sleep(50);
	}

	private Values fetchReading() {
		String reading = null;
		try {
			reading = requestAndWaitForResponse("NEWOWM");
			JSONObject json = new JSONObject(reading);
			System.err.println(json.getString("timestamp"));//TODO remove
			
		} catch (IOException e) {
			System.err.println("IO exception in fetchReading() in DataBaseSpout: " + e.getMessage());
			return null;
		} catch (JSONException e) {
			System.err.println("JSON exception in fetchReading() in DataBaseSpout: " + e.getMessage());
			return null;
		}
		return new Values( //TODO remove
        		"2015-12-23 15:37:31",
        		6.122222476535374, 995.0, 75.0, 3.240000009536743,
        		5.822221967909072, 996.0, 81.0, 4.539999961853027);
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
			System.err.println("COULD NOT CONNECT TO SERVER ON IP " + SERVER_IP + " PORT " + SERVER_PORT);
			e1.printStackTrace();
		}
		collector = arg2;
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer arg0) {
		arg0.declare(new Fields("ctime", "ctemp", "cpres", "chumi", "cwind", "otemp", "opres", "ohumi", "owind"));
	}

	@Override
	public Map<String, Object> getComponentConfiguration() {
		return null;
	}

}
