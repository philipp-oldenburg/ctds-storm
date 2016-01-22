package weather;
import java.util.Map;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.IRichBolt;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

public class FeatureSpaceBolt implements IRichBolt {

	private OutputCollector collector;

	@Override
	public void cleanup() {}

	@Override
	public void execute(Tuple rawVector) {
		Values featureVector = toFeatureSpace(rawVector);
		collector.emit(rawVector, featureVector);
		collector.ack(rawVector);
	}

	private Values toFeatureSpace(Tuple raw) {
		String time = raw.getStringByField("ctime");
		double temperature = raw.getDoubleByField("ctemp");
		double pressure = raw.getDoubleByField("cpres");
		double humidity = raw.getDoubleByField("chumi");
		double windspeed = raw.getDoubleByField("cwind");
		double deltatemperature = temperature - raw.getDoubleByField("otemp");
		double deltapressure = pressure - raw.getDoubleByField("opres");
		double deltahumidity = humidity - raw.getDoubleByField("ohumi");
		double deltawindspeed = windspeed - raw.getDoubleByField("owind");
		return new Values(
				time,
				temperature, pressure, humidity, windspeed,
				deltatemperature, deltapressure, deltahumidity, deltawindspeed);
	}

	@Override
	public void prepare(Map arg0, TopologyContext arg1, OutputCollector arg2) {
		collector = arg2;
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer arg0) {
		arg0.declare(new Fields("ctime", "ctemp", "cpres", "chumi", "cwind", "dtemp", "dpres", "dhumi", "dwind"));
	}

	@Override
	public Map<String, Object> getComponentConfiguration() {
		return null;
	}

}
