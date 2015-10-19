import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.utils.Utils;

public class Starter {

	public static void main(String[] args) {

		TopologyBuilder builder = new TopologyBuilder();

		builder.setSpout("temperature", new TemperatureSpout(), 1);
		builder.setSpout("airpressure", new AirPressureSpout(), 1);
		builder.setBolt("processor", new ProcessingBolt(), 5).shuffleGrouping("airpressure").shuffleGrouping("temperature");
		builder.setBolt("visualization", new VisualizationBolt(), 2).shuffleGrouping("processor");

		Config conf = new Config();
		conf.setDebug(true);

		LocalCluster cluster = new LocalCluster();
		cluster.submitTopology("test", conf, builder.createTopology());
		Utils.sleep(100000);
		cluster.killTopology("test");
		cluster.shutdown();

	}

}
