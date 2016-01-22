package weather;
import java.io.File;
import java.util.List;
import java.util.Map;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.IRichBolt;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import utils.Cluster;
import utils.Statistics;
import utils.Utils;

public class ClassificationBolt implements IRichBolt {
	
	private static final String CLUSTERPATH = "/home/pi/Documents/data";

	private OutputCollector collector = null;
	private List<Cluster> clusterList; //List of weather clusters (clear, rain, clouds, ...);

	@Override
	public void cleanup() {}

	@Override
	public void execute(Tuple featureVector) {
		Values labeledVector = classify(featureVector);
		collector.emit(featureVector, labeledVector);
		collector.ack(featureVector);
	}

	private Values classify(Tuple featureVector) {
		String time = featureVector.getStringByField("ctime");
		double[] x = new double[8];
		
		x[0] = featureVector.getDoubleByField("ctemp");
		x[1] = featureVector.getDoubleByField("cpres");
		x[2] = featureVector.getDoubleByField("chumi");
		x[3] = featureVector.getDoubleByField("cwind");
		x[4] = featureVector.getDoubleByField("dtemp");
		x[5] = featureVector.getDoubleByField("dpres");
		x[6] = featureVector.getDoubleByField("dhumi");
		x[7] = featureVector.getDoubleByField("dwind");
		
		String label = Statistics.maximumLikelihoodPrior(clusterList, x);
		
		return new Values(
				time,
				x[0], x[1], x[2], x[3],
				label);
	}

	@Override
	public void prepare(Map arg0, TopologyContext arg1, OutputCollector arg2) {
		if (clusterList == null) {
			clusterList = Utils.loadClusters(new File(CLUSTERPATH));
		}
		collector = arg2;
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer arg0) {
		arg0.declare(new Fields("ctime", "ctemp", "cpres", "chumi", "cwind", "label"));
	}

	@Override
	public Map<String, Object> getComponentConfiguration() {
		return null;
	}

}
