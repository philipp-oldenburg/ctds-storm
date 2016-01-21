package oldutils;

public class DataPoint {
	private double[] data;
	private int label;
	
	public DataPoint(int newLabel, double... newData) {
		setData(newData);
		setLabel(newLabel);
	}

	/**
	 * @return the data
	 */
	public double[] getData() {
		return data.clone();
	}

	/**
	 * @param data the data to set
	 */
	public void setData(double[] data) {
		this.data = data;
	}

	/**
	 * @return the label
	 */
	public int getLabel() {
		return label;
	}

	/**
	 * @param label the label to set
	 */
	public void setLabel(int label) {
		this.label = label;
	}
	
}
