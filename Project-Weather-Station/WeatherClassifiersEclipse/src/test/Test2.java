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
		
		double[] testv = {5.37222120496962, 996, 87, 5.61999988555908};
		System.out.println(Statistics.maximumLikelihood(clusterList, testv));
//		------------------------------------------------------------------------------------------------------------
		
		try {
			MatFileReader reader = new MatFileReader(new File("res/data.mat"));
			MLDouble mldata = (MLDouble) reader.getContent().get("data");
			double[][] data = mldata.getArray();
			int sum = 0;
			int clear = 0;
			int rain = 0;
			int snow = 0;
			int clouds = 0;
			int mist = 0;
			int fog = 0;
			int drizzle = 0;
			int thunderstorm = 0;
			for (int i = 0; i < data.length; i++) {
				double[] test = new double[4];
				test[0] = data[i][0];
				test[1] = data[i][1];
				test[2] = data[i][2];
				test[3] = data[i][3];
				double truth = data[i][4];
				String classification = Statistics.maximumLikelihoodPrior(clusterList, test);
				System.out.println(classification);
				switch (classification) {
				case "Clear":
					clear++;
					if (truth == 0) sum++;
					break;
				case "Rain":
					rain++;
					if (truth == 1) sum++;
					break;
				case "Snow":
					snow++;
					if (truth == 2) sum++;
					break;
				case "Clouds":
					clouds++;
					if (truth == 3) sum++;
					break;
				case "Mist":
					mist++;
					if (truth == 4) sum++;
					break;
				case "Fog":
					fog++;
					if (truth == 5) sum++;
					break;
				case "Drizzle":
					drizzle++;
					if (truth == 6) sum++;
					break;
				case "Thunderstorm":
					thunderstorm++;
					if (truth == 7) sum++;
					break;

				default:
					throw new RuntimeException("Bad classification: " + classification);
				}
			}
			System.out.println(sum + " / " + data.length + " = " + (100.0 * sum)/data.length + "%");
			System.out.println("Clear: " + clear);
			System.out.println("Rain: " + rain);
			System.out.println("Snow: " + snow);
			System.out.println("Clouds: " + clouds);
			System.out.println("Mist: " + mist);
			System.out.println("Fog: " + fog);
			System.out.println("Drizzle: " + drizzle);
			System.out.println("Thunderstorm: " + thunderstorm);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
}
