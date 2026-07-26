package model;

public class Position {
	private double latitude;
	private double longitude;

	public Position() {

	}

	public Position(double lat, double lon) {
		this.longitude = lon;
		this.latitude = lat;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
}
