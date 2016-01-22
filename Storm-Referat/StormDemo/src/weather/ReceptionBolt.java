package weather;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
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
	private BufferedReader din;
	
	private static final String SERVER_IP = "192.168.2.124";
	private static final int SERVER_PORT = 9002;

	@Override
	public void cleanup() {}

	@Override
	public void execute(Tuple resultVector) {
		sendToServer(
				resultVector.getStringByField("ctime"),
				resultVector.getDoubleByField("ctemp"),
				resultVector.getDoubleByField("cpres"),
				resultVector.getDoubleByField("chumi"),
				resultVector.getDoubleByField("cwind"),
				resultVector.getStringByField("label"));
		collector.ack(resultVector);
	}
	
	private void sendToServer(String time, Double temp, Double pres, Double humi, Double wind, String label) {
		JSONObject currentObject = new JSONObject();
		try {
			currentObject.put("timestamp", time);
			currentObject.put("temperature", temp);
			currentObject.put("pressure", pres);
			currentObject.put("humidity", humi);
			currentObject.put("sensorwindspeed", wind);
			currentObject.put("label", label);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		dout.println(currentObject.toString());
		dout.flush();
		
	}
	
	@Override
	public void prepare(Map arg0, TopologyContext arg1, OutputCollector arg2) {
		
		if (socket == null) {
			try {
				socket = new Socket(SERVER_IP, SERVER_PORT);
				dout = new PrintWriter(socket.getOutputStream());
				din = new BufferedReader(new InputStreamReader(socket.getInputStream()));
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
