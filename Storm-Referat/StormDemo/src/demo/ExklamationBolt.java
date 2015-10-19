package demo;

import java.util.Map;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.IRichBolt;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

public class ExklamationBolt implements IRichBolt {

	private OutputCollector coll;

	@Override
	public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
		coll = collector;
	}

	@Override
	public void execute(Tuple input) {
		String world = input.getString(0);
		String helloworld = world + "!!!";
		coll.emit(input, new Values(helloworld));
	}

	@Override
	public void cleanup() {

	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("!!!"));
	}

	@Override
	public Map<String, Object> getComponentConfiguration() {
		return null;
	}

}
