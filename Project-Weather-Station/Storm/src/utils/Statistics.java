package utils;

import java.util.List;

import org.ejml.simple.SimpleMatrix;

public class Statistics {
	
	public static String maximumLikelihoodPrior(List<Cluster> clusters, double[] x) {
		String label = null;
		double max = Double.NEGATIVE_INFINITY;
		for (Cluster cluster : clusters) {
			double l = cluster.getPrior() * likelihood(cluster, x);
			if (l > max) {
				max = l;
				label = cluster.getLabel();
			}
		}
		return label;
	}
	
	public static String maximumLikelihood(List<Cluster> clusters, double[] x) {
		String label = null;
		double max = Double.NEGATIVE_INFINITY;
		for (Cluster cluster : clusters) {
			double l = likelihood(cluster, x);
			if (l > max) {
				max = l;
				label = cluster.getLabel();
			}
		}
		return label;
	}
	
	/**
	 * Returns the likelihood of an observation x stemming from the cluster defined by the {@link Cluster}-Object.
	 * @param cluster The cluster to be tested against
	 * @param x The observation to be tested.
	 * @return likelihood of observation for this cluster
	 */
	public static double likelihood(Cluster cluster, double[] x) {
		
		double normF = cluster.getNormF();
		double[] mean = cluster.getMean();
		double[][] invM = cluster.getInvM();
		
		return likelihood(normF, mean, invM, x);
		
	}

	/**
	 * Returns the likelihood of an observation x stemming from the cluster defined by the other parameters.
	 * @param normF <b>(Normalization Factor):</b> Pre-calculated value of 1./(sqrt((2*pi)^(length(mean)) * det(covariancematrix))).
	 * @param mean <b>:</b> the mean of the cluster 
	 * @param invM <b>(Inverted (Covariance-)Matrix):</b> the inverse of the covariance matrix
	 * @param x The observation to be tested.
	 * @return likelihood of observation for this cluster
	 */
	private static double likelihood(double normF, double[] mean, double[][] invM, double[] x) {
		if (x.length != mean.length) {
			throw new IllegalArgumentException("Dimensions of x and mean do not aggree.");
		}
		if (invM[0].length != invM.length) {
			throw new IllegalArgumentException("invM must be quadratic.");
		}
		if (invM.length != x.length) {
			throw new IllegalArgumentException("Dimensions of invM and x or mean do not aggree.");
		}
		
		double[][] differences = new double[x.length][1];
		for (int i = 0; i < x.length; i++) {
			differences[i][0] = x[i] - mean[i];
		}
		SimpleMatrix diffMat = new SimpleMatrix(differences);
		SimpleMatrix invMat = new SimpleMatrix(invM);
		
		double temp = diffMat.transpose().mult(invMat).mult(diffMat).get(0);
		double result = normF * Math.exp(-0.5 * temp);
		return result;
		
	}
	
}
