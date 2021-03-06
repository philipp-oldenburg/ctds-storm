package weather;

import backtype.storm.Config;
import backtype.storm.StormSubmitter;
import backtype.storm.generated.AlreadyAliveException;
import backtype.storm.generated.InvalidTopologyException;
import backtype.storm.topology.TopologyBuilder;

public class Starter {

	public static void main(String[] args) {

		TopologyBuilder builder = new TopologyBuilder();

		builder.setSpout("dbspout", new DataBaseSpout(), 1);
		builder.setBolt("fsbolt", new FeatureSpaceBolt(), 3).shuffleGrouping("dbspout");
		builder.setBolt("cfbolt", new ClassificationBolt(), 12).shuffleGrouping("fsbolt");
		builder.setBolt("rcbolt", new ReceptionBolt(), 1).shuffleGrouping("cfbolt");

		Config conf = new Config();
		conf.setNumWorkers(2);
//		conf.setDebug(true);

//		LocalCluster cluster = new LocalCluster();
//		cluster.submitTopology("test", conf, builder.createTopology());
//		Utils.sleep(100000);
//		cluster.killTopology("test");
//		cluster.shutdown();
		
		try {
			StormSubmitter.submitTopology("helloworldtopology", conf, builder.createTopology());
		} catch (AlreadyAliveException | InvalidTopologyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
