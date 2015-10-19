package helloworld;
import backtype.storm.Config;
import backtype.storm.StormSubmitter;
import backtype.storm.generated.AlreadyAliveException;
import backtype.storm.generated.InvalidTopologyException;
import backtype.storm.topology.TopologyBuilder;

public class Starter {

	public static void main(String[] args) {

		TopologyBuilder builder = new TopologyBuilder();

		builder.setSpout("hello", new HelloSpout(), 4);
		builder.setBolt("world", new WorldBolt(), 4).shuffleGrouping("hello");

		Config conf = new Config();

		conf.setNumWorkers(2);
		try {
			StormSubmitter.submitTopology("helloworldtopology", conf, builder.createTopology());
		} catch (AlreadyAliveException | InvalidTopologyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
