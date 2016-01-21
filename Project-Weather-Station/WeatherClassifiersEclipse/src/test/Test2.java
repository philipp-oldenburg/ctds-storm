package test;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;

import com.jmatio.io.MatFileReader;
import com.jmatio.types.MLDouble;

import utils.Cluster;
import utils.Statistics;
import utils.Utils;

public class Test2 {
	
	public static void main(String[] args) {
//		double norm = 1;
//		double[][] inv = {{1,2,3},{4,5,6},{7,8,9}};
//		double[] mean = {0,0,0};
		
//		double[][] differences = {{1},{1},{1}};
		
		//p0_norm * exp(-(1/2)*(x-mean0)^T * p0_inv *(x-mean0));
		
//		double temp;
//		double res;
//		
//		SimpleMatrix diffMat = new SimpleMatrix(differences);
//		SimpleMatrix invMat = new SimpleMatrix(inv);
//		
//		temp = diffMat.transpose().mult(invMat).mult(diffMat).get(0);
//		res = norm * Math.exp(-0.5 * temp);
//		System.out.println(res);
		
//		double[] x = {1,1,1};
//		Cluster testCluster = new Cluster("test", norm, mean, inv);
//		System.out.println(Statistics.likelihood(testCluster, x));
//		
//		double clearnorm = 4.058935363275917e-04;
//		double[] clearmean = {0.439247353996,1010.758064516129,86.021994134897,5.034457437803};
//		double[][] clearinv = { { 1.091395216805286,  0.313427629105911,  0.041110571030026, -0.136712290954650},
//                                { 0.313427629105911,  0.110924924084152,  0.021334620090865,  0.005209684207152},
//                                { 0.041110571030026,  0.021334620090865,  0.024516740776447,  0.022064798147021},
//                                {-0.136712290954650,  0.005209684207152,  0.022064798147021,  0.718182782668519} };
//		double[] cleartest = {4.92222256130642, 998, 93, 3.24000000953674};
//		
//		Cluster clearCluster = new Cluster("clear", clearnorm, clearmean, clearinv);
//		System.out.println(Statistics.likelihood(clearCluster, cleartest));
		
//		-----------------------------------------------------------------------------------------------------------
		File[] clusterFiles = new File("res").listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".cluster.mat");
			}
		});
		ArrayList<Cluster> clusterList = new ArrayList<Cluster>();
		for (File file : clusterFiles) {
			String name = file.getName().substring(0, file.getName().indexOf('.'));
			clusterList.add(Utils.readCluster(name, file));
		}
		System.out.println(clusterList.size());
		
		double[] test = {5.37222120496962, 996, 87, 5.61999988555908};
		System.out.println(Statistics.maximumLikelihood(clusterList, test));
//		------------------------------------------------------------------------------------------------------------
		
		try {
			MatFileReader reader = new MatFileReader(new File("res/data.mat"));
			MLDouble mldata = (MLDouble) reader.getContent().get("data");
			double[][] data = mldata.getArray();
			int sum = 0;
			for (int i = 0; i < data.length; i++) {
				String classification = Statistics.maximumLikelihood(clusterList, data[i]);
				System.out.println(classification);
				if (classification.equals("Clear")) {
					sum++;
				}
			}
			System.out.println(sum + " / " + data.length + " = " + (100.0 * sum)/data.length + "%");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
}
