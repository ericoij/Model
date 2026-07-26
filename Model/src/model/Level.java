package model;

/**
 * One radiosonde pressure level. SI units are used internally.
 */
public class Level {
	private Double pressure;
	private Double geoHeight;
	private Double windSpeed;
	private Double windDirection;
	private Double dewPointDepression;
	private Double temperature;
	private Double relativeHumidity;

	public Level(Double pressure, Double geoHeight, Double windSpeed, Double windDirection,
			Double dewPointDepression, Double temperature, Double relativeHumidity) {
		this.pressure = pressure;
		this.geoHeight = geoHeight;
		this.windSpeed = windSpeed;
		this.windDirection = windDirection;
		this.dewPointDepression = dewPointDepression;
		this.temperature = temperature;
		this.relativeHumidity = relativeHumidity;
	}

	public Level() {
	}

	public boolean isForecastUsable() {
		return finite(pressure) && finite(geoHeight) && finite(windSpeed)
				&& windSpeed >= 0 && windSpeed <= 200
				&& finite(windDirection) && windDirection >= 0 && windDirection <= 360
				&& finite(temperature) && temperature > 150 && temperature < 350;
	}

	private static boolean finite(Double value) {
		return value != null && Double.isFinite(value);
	}

	public WindVector windVector() {
		if (!isForecastUsable()) throw new IllegalStateException("Level does not contain a valid wind");
		return new WindVector(windSpeed, windDirection);
	}

	public Double getPressure() { return pressure; }
	public void setPressure(Double value) { pressure = value; }
	public Double getGeoHeight() { return geoHeight; }
	public void setGeoHeight(Double value) { geoHeight = value; }
	public Double getWindSpeed() { return windSpeed; }
	public void setWindSpeed(Double value) { windSpeed = value; }
	public Double getWindDirection() { return windDirection; }
	public void setWindDirection(Double value) { windDirection = value; }
	public Double getDewPointDepression() { return dewPointDepression; }
	public void setDewPointDepression(Double value) { dewPointDepression = value; }
	public Double getTemperature() { return temperature; }
	public Double getTempurature() { return temperature; } // Legacy spelling.
	public void setTemperature(Double value) { temperature = value; }
	public void setTempurature(Double value) { temperature = value; }
	public Double getRelativeHumidity() { return relativeHumidity; }
	public void setRelativeHumidity(Double value) { relativeHumidity = value; }

	public void showLevel() {
		System.out.printf("Pressure=%s Pa, height=%s m, wind=%s m/s from %s°, temperature=%s K, RH=%s%%%n",
				pressure, geoHeight, windSpeed, windDirection, temperature, relativeHumidity);
	}

	public void writeLevel() {
		// Retained for source compatibility with the original prototype.
	}
}
