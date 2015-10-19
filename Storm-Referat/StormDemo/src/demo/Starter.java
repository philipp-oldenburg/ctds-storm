package demo;
import backtype.storm.Config;
import backtype.storm.StormSubmitter;
import backtype.storm.generated.AlreadyAliveException;
import backtype.storm.generated.InvalidTopologyException;
import backtype.storm.topology.TopologyBuilder;

public class Starter {

	public static void main(String[] args) {

		TopologyBuilder builder = new TopologyBuilder();

		builder.setSpout("hello", new HelloSpout(), 1);
		builder.setBolt("world", new WorldBolt(), 1).shuffleGrouping("hello");
		builder.setBolt("!!!", new ExklamationBolt(), 1).shuffleGrouping("world");

		Config conf = new Config();
		conf.setDebug(true);
		conf.put(Config.TOPOLOGY_DEBUG, true);

		try {
			StormSubmitter.submitTopology("helloworld!!!topology", conf, builder.createTopology());
		} catch (AlreadyAliveException | InvalidTopologyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
