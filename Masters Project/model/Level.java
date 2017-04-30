package model;

public class Level {
	private Double pressure;
	private Double geoHeight;
	private Double windSpeed;
	private Double windDirection;
	private Double dewPointDepression;
	private Double tempurature;
	private Double relativeHumidity;
	private WindVector windVector;

	public Level(Double pres, Double geo, Double ws, Double wd, Double dpdp, Double tem, Double rh) {
		this.setPressure(pres);
		this.setGeoHeight(geo);
		this.setWindDirection(wd);
		this.setWindSpeed(ws);
		this.setDewPointDepression(dpdp);
		this.setTempurature(tem);
		this.setRelativeHumidity(rh);
		this.setWindVector( new WindVector(ws,wd));
	}
	public Level(Double pres, Double geo, WindVector wv, Double dpdp, Double tem, Double rh) {
		this.setPressure(pres);
		this.setGeoHeight(geo);
		this.setDewPointDepression(dpdp);
		this.setTempurature(tem);
		this.setRelativeHumidity(rh);
		this.setWindVector(wv);
		
		this.setWindDirection(0.0);
		this.setWindSpeed(0.0);
	}

	public Level() {
//		this.setWindVector(new WindVector(0.0,0.0));
	}

	public Double getPressure() {
		return pressure;
	}

	public void setPressure(Double pres) {
		this.pressure = pres;
	}

	public Double getGeoHeight() {
		return geoHeight;
	}

	public void setGeoHeight(Double geo) {
		this.geoHeight = geo;
	}

	public Double getWindSpeed() {
		return windSpeed;
	}

	public void setWindSpeed(Double ws) {
		this.windSpeed = ws;
	}

	public Double getWindDirection() {
		return windDirection;
	}

	public void setWindDirection(Double wd) {
		this.windDirection = wd;
	}

	public Double getDewPointDepression() {
		return dewPointDepression;
	}

	public void setDewPointDepression(Double dpdp) {
		this.dewPointDepression = dpdp;
	}

	public Double getTempurature() {
		if (tempurature != null) {
			return tempurature;
		}
		else
			return 9999.0;
	}

	public void setTempurature(Double tem) {
		this.tempurature = tem;
	}

	public Double getRelativeHumidity() {
		return relativeHumidity;
	}

	public void setRelativeHumidity(Double rh) {
		this.relativeHumidity = rh;
	}

	public void showLevel() {
		System.out.println("Pressure =" + pressure);
		System.out.println("geoHeight =" + geoHeight);
		System.out.println("windSpeed =" + windSpeed);
		System.out.println("windDirection =" + windDirection);
		System.out.println("dewPointDepression =" + dewPointDepression);
		System.out.println("tempurature =" + tempurature);
		System.out.println("relativeHumidity =" + relativeHumidity);
	}

	public void writeLevel() {

	}

	public WindVector getWindVector() {
		return windVector;
	}

	public void setWindVector(WindVector windVector) {
		this.windVector = windVector;
	}
}
