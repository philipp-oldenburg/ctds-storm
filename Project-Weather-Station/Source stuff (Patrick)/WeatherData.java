
public class WeatherData {
	private double temp ;
	private double pressure;
	private double humidity;
	private double windSpeed;
	private double windDegree;
	private double light;
	private String owmName;
	private String owmDesc;
	
	
	public double getTemp() {
		return temp;
	}
	public void setTemp(double temp) {
		this.temp = temp;
	}
	public double getPressure() {
		return pressure;
	}
	public void setPressure(double pressure) {
		this.pressure = pressure;
	}
	public double getHumidity() {
		return humidity;
	}
	public void setHumidity(double humidity) {
		this.humidity = humidity;
	}
	public double getWindSpeed() {
		return windSpeed;
	}
	public void setWindSpeed(double windspeed) {
		this.windSpeed = windspeed;
	}
	public double getLight() {
		return light;
	}
	public void setLight(double light) {
		this.light = light;
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
	public void setWindDegree(float windDegree) {
		this.windDegree = windDegree;
	}
	public double getWindDegree() {
		return windDegree;
	}
}
