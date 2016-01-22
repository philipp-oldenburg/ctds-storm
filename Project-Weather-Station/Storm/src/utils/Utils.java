package utils;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.jmatio.io.MatFileReader;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLDouble;

public class Utils {
	
	public static List<Cluster> loadClusters(File directory) {
		if (!directory.isDirectory()) {
			throw new IllegalArgumentException("Bad directory");
		}
		File[] clusterFiles = directory.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".cluster.mat");
			}
		});
		if (clusterFiles.length <= 0) {
			throw new IllegalArgumentException("No clusters found. Clusters must end with '.cluster.mat'");
		}
		ArrayList<Cluster> clusterList = new ArrayList<Cluster>();
		for (File file : clusterFiles) {
			String name = file.getName().substring(0, file.getName().indexOf('.'));
			clusterList.add(Utils.readCluster(name, file));
		}
		return clusterList;
	}
	
	/**
	 * Reads MatLab .mat file. Must include variables norm (scalar), mean (row vector), inv (quadratic matrix), prior (scalar)
	 * @param label
	 * @param file
	 * @return
	 */
	public static Cluster readCluster(String label, File file) {
		try {
			MatFileReader reader = new MatFileReader(file);
			Map<String, MLArray> data = reader.getContent();
			
			MLDouble normmat = (MLDouble) data.get("norm");
			double[][] normarr = normmat.getArray();
			double norm = normarr[0][0];
			
			MLDouble meanmat = (MLDouble) data.get("mean");
			double[][] meanarr = meanmat.getArray();
			double[] mean = meanarr[0];
			
			MLDouble invmat = (MLDouble) data.get("inv");
			double[][] inv = invmat.getArray();
			
			double prior = 1;
			if (data.containsKey("prior")) {
				MLDouble priormat = (MLDouble) data.get("prior");
				double[][] priorarr = priormat.getArray();
				prior = priorarr[0][0];
			}
			
			return new Cluster(label, norm, mean, inv, prior);
			
		} catch (Exception e) {
			throw new IllegalArgumentException("Could not read cluster " + file.getAbsolutePath());
		}
	}
	
}
