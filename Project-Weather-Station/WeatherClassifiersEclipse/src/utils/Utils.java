package utils;

import java.io.File;
import java.util.Map;

import com.jmatio.io.MatFileReader;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLDouble;

public class Utils {
	
	/**
	 * Reads MatLab .mat file. Must include variables norm (scalar), mean (row vector), inv (quadratic matrix)
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
			
			return new Cluster(label, norm, mean, inv);
			
		} catch (Exception e) {
			throw new IllegalArgumentException("Could not read cluster " + file.getAbsolutePath());
		}
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
//	public static double[] getMean(List<DataPoint> dataPoints) {
//		double n = dataPoints.size();
//		double[] result = dataPoints.get(0).getData();
//		for (int i = 1; i < n; i++) {
//			double[] vector = dataPoints.get(i).getData();
//			for (int j = 0; j < result.length; j++) {
//				result[j] += vector[j];
//			}
//		}
//		System.out.println(result[0]);
//		for (int j = 0; j < result.length; j++) {
//			result[j] /= n;
//		}
//		return result;
//	}
}
