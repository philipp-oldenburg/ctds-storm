package utils;

public class Cluster {

	private String label;
	private double normF;
	private double[] mean;
	private double[][] invM;
	private double prior;
	
	public Cluster(String label, double normF, double[] mean, double[][] invM, double prior) {
		if (invM[0].length != invM.length) {
			throw new IllegalArgumentException("invM must be quadratic.");
		}
		if (invM.length != mean.length) {
			throw new IllegalArgumentException("Dimensions of invM and mean do not aggree.");
		}

		this.label = label;
		this.normF = normF;
		this.mean = mean;
		this.invM = invM;
		this.prior = prior;
	}
	
	/**
	 * @return the normF
	 */
	public double getNormF() {
		return normF;
	}
	/**
	 * @return the mean
	 */
	public double[] getMean() {
		return mean;
	}
	/**
	 * @return the invM
	 */
	public double[][] getInvM() {
		return invM;
	}

	/**
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * @return the prior
	 */
	public double getPrior() {
		return prior;
	}
	
}
