public class WeatherData {
	private double temp ;
	private double pressure;
	private double humidity;
	private double sensorWindSpeed;
	private double owmWindSpeed;
	private double owmWindDegree;
	private double light;
	private String owmName;
	private String owmDesc;
	
	private boolean isDoubleNaN(double input) {
		float tmp = (float) input; 
		if (Float.compare(Float.NaN, tmp) == 0) {
			return true;
		}
		return false;
	}
	
	
	public double getTemp() {
		return temp;
	}
	public void setTemp(double temp) {
		this.temp = isDoubleNaN(temp) ? -300 : temp;
	}
	public double getPressure() {
		return pressure;
	}
	public void setPressure(double pressure) {
		this.pressure = isDoubleNaN(pressure) ? -1 : pressure;
	}
	public double getHumidity() {
		return humidity;
	}
	public void setHumidity(double humidity) {
		this.humidity = isDoubleNaN(humidity) ? -1 : humidity;
	}
	public double getOwmWindSpeed() {
		return owmWindSpeed;
	}
	public void setOwmWindSpeed(double owmWindSpeed) {
		this.owmWindSpeed = isDoubleNaN(owmWindSpeed) ? -1 : owmWindSpeed;
	}
	public double getLight() {
		return light;
	}
	public void setLight(double light) {
		this.light = isDoubleNaN(light) ? -1 : light;
	}
	public String getOwmName() {
		return owmName;
	}
	public void setOwmName(String owmName) {
		this.owmName = owmName;
	}
	public String getOwmDesc() {
		return owmDesc;
	}
	public void setOwmDesc(String owmDesc) {
		this.owmDesc = owmDesc;
	}
	public void setOwmWindDegree(double owmWindDegree) {
		this.owmWindDegree = isDoubleNaN(owmWindDegree) ? -1 : owmWindDegree;
	}
	public double getOwmWindDegree() {
		return owmWindDegree;
	}
	public void setSensorWindSpeed(double sensorWindSpeed) {
		this.sensorWindSpeed = isDoubleNaN(sensorWindSpeed) ? -1 : sensorWindSpeed;
	}
	public double getSensorWindSpeed() {
		return sensorWindSpeed;
	}
}
