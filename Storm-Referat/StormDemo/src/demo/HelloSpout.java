package demo;

import java.util.Map;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.IRichSpout;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;

public class HelloSpout implements IRichSpout {
	private SpoutOutputCollector coll;

	@Override
	public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {
		coll = collector;
	}

	@Override
	public void close() {

	}

	@Override
	public void activate() {

	}

	@Override
	public void deactivate() {

	}

	@Override
	public void nextTuple() {
		coll.emit(new Values("hello"), new Values("hello"));
		System.out.println("fired hello");
	}

	@Override
	public void ack(Object msgId) {

	}

	@Override
	public void fail(Object msgId) {
		coll.emit(new Values("hello"), new Values("hello"));
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("hello"));
	}

	@Override
	public Map<String, Object> getComponentConfiguration() {
		return null;
	}

}
