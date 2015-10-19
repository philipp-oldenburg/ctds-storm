import java.util.Map;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.IRichSpout;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;
import backtype.storm.utils.Utils;

public class TemperatureSpout implements IRichSpout {

	private SpoutOutputCollector collector;
	
	private static final double EMISSION_PERIOD = 20000; // 2 seconds
	private long lastEmission;

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
            collector.emit(new Values(Math.random()));
            lastEmission = System.currentTimeMillis();
            return;
	    }
	    Utils.sleep(50);
	}

	@Override
	public void open(Map arg0, TopologyContext arg1, SpoutOutputCollector arg2) {
		collector = arg2;
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer arg0) {
		arg0.declare(new Fields("temperature"));
	}

	@Override
	public Map<String, Object> getComponentConfiguration() {
		return null;
	}

}
