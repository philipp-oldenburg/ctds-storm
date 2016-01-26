package weather;
//import java.io.BufferedReader;
import java.io.IOException;
//import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.IRichBolt;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;

public class ReceptionBolt implements IRichBolt {

	private OutputCollector collector;
	
	private Socket socket = null;
	private PrintWriter dout;
//	private BufferedReader din;
	
	private static final String SERVER_IP = "192.168.2.124";
	private static final int SERVER_PORT = 9002;
	
	private static final SimpleDateFormat TIMESTAMP_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	private static long newestOWM = 0;
	private static long newestSENS = 0;

	@Override
	public void cleanup() {}

	/**
	 * Sends new classified data if it is newer than last sent classified data.
	 */
	@Override
	public void execute(Tuple resultVector) {
		
		boolean sendClearance = false;
		String source = resultVector.getStringByField("sourc");
		try {
			long time = TIMESTAMP_FORMAT.parse(resultVector.getStringByField("ctime")).getTime();
			if (source.equals("OWM") && time > newestOWM) {
				newestOWM = time;
				sendClearance = true;
			} else if (source.equals("SENS") && time > newestSENS) {
				newestSENS = time;
				sendClearance = true;
			}
		} catch (ParseException e) {
			System.err.println("Did not send labeled data as timestamp was bad");
			//just don't send.
		}
		
		if (sendClearance) {
			sendToServer(
					resultVector.getStringByField("ctime"),
					resultVector.getDoubleByField("ctemp"),
					resultVector.getDoubleByField("cpres"),
					resultVector.getDoubleByField("chumi"),
					resultVector.getDoubleByField("cwind"),
					resultVector.getStringByField("label"),
					resultVector.getStringByField("sourc"));
		}
		
		collector.ack(resultVector);
	}
	
	private void sendToServer(String time, Double temp, Double pres, Double humi, Double wind, String label, String source) {
		JSONObject currentObject = new JSONObject();
		try {
			currentObject.put("timestamp", time);
			currentObject.put("temperature", temp);
			currentObject.put("pressure", pres);
			currentObject.put("humidity", humi);
			currentObject.put("sensorwindspeed", wind);
			currentObject.put("label", label);
			currentObject.put("source", source);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		System.out.println("Sent to server: " + currentObject.toString());
		dout.println(currentObject.toString());
		dout.flush();
		
	}
	
	@Override
	public void prepare(Map arg0, TopologyContext arg1, OutputCollector arg2) {
		
		if (socket == null) {
			try {
				socket = new Socket(SERVER_IP, SERVER_PORT);
				dout = new PrintWriter(socket.getOutputStream());
//				din = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				socket.setSoTimeout(2000);
			} catch (IOException e1) {
				System.err.println("REC: COULD NOT CONNECT TO SERVER ON IP " + SERVER_IP + " PORT " + SERVER_PORT);
				e1.printStackTrace();
			}
		}
		collector = arg2;
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer arg0) {
		arg0.declare(new Fields("ctime", "ctemp", "cpres", "chumi", "cwind", "label"));
	}

	@Override
	public Map<String, Object> getComponentConfiguration() {
		return null;
	}

}
