package helloworld;

import java.util.Map;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.IRichSpout;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;
import backtype.storm.utils.Utils;

public class HelloSpout implements IRichSpout {
	private SpoutOutputCollector coll;
	private long timestamp;
	private long sleeptime = 5000;
	

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
		if (System.currentTimeMillis() >= timestamp + sleeptime ) {
			coll.emit(new Values("hello"), new Values("hello"));
			
			System.out.println("hello");
			timestamp = System.currentTimeMillis();
		} else {
			Utils.sleep(50);
		}
		
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
