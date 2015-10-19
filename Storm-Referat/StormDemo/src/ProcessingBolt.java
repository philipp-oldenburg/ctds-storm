import java.util.Map;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.IRichBolt;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

public class ProcessingBolt implements IRichBolt {

	private OutputCollector collector;

	@Override
	public void cleanup() {}

	@Override
	public void execute(Tuple arg0) {
		Values tuple = doProcessing(arg0);
		collector.emit(arg0, tuple);
		collector.ack(arg0);
	}

	private Values doProcessing(Tuple arg0) {
		return new Values(arg0.getDouble(0) * 2); // <- really complex processing is done here
	}

	@Override
	public void prepare(Map arg0, TopologyContext arg1, OutputCollector arg2) {
		collector = arg2;
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer arg0) {
		arg0.declare(new Fields("processedTuple"));
	}

	@Override
	public Map<String, Object> getComponentConfiguration() {
		return null;
	}

}
