package oldutils;

import java.util.Vector;

public class DataPointOld {
	private Vector<Double> data;
	private int label;
	
	public DataPointOld(int newLabel, Vector<Double> newData) {
		setData(newData);
		setLabel(newLabel);
	}
	
	public DataPointOld(int newLabel, double... newData) {
		Vector<Double> newVector = new Vector<Double>(newData.length);
		for (double d : newData) {
			newVector.add(d);
		}
		setData(newVector);
		setLabel(newLabel);
	}

	/**
	 * @return the data
	 */
	public Vector<Double> getData() {
		return data;
	}

	/**
	 * @param data the data to set
	 */
	public void setData(Vector<Double> data) {
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
