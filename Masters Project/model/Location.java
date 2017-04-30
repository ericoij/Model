package model;

public class Location {

	private Level oneThousand;
	private Level nineTwentyFive;
	private Level eightFifty;
	private Level sevenHundred;
	private Level fiveHundred;
	private Level threeHundred;
	private Level twoFifty;
	private Level twoHundred;

	private double latitude, longitude;

	private Position local = new Position();

	public Location(double lat, double lng) {
		setLongitude(lng);
		setLatitude(lat);
		setLocal(new Position(lat, lng));
	}

	public Location(double lat, double lng, Location location) {
		setLongitude(lng);
		setLatitude(lat);
		setLocal(new Position(lat, lng));
	}

	public Location() {
//		setOneThousand(new Level());
//		setNineTwentyFive(new Level());
//		setEightFifty(new Level());
//		setSevenHundred(new Level());
//		setFiveHundred(new Level());
	}

	public Level getOneThousand() {
		return oneThousand;
	}

	public void setOneThousand(Level oneThousand) {
		this.oneThousand = oneThousand;
	}

	public Level getNineTwentyFive() {
		return nineTwentyFive;
	}

	public void setNineTwentyFive(Level nineTwenttyFive) {
		this.nineTwentyFive = nineTwenttyFive;
	}

	public Level getEightFifty() {
		return eightFifty;
	}

	public void setEightFifty(Level eightFifty) {
		this.eightFifty = eightFifty;
	}

	public Level getSevenHundred() {
		return sevenHundred;
	}

	public void setSevenHundred(Level sevenHundred) {
		this.sevenHundred = sevenHundred;
	}

	public Level getFiveHundred() {
		return fiveHundred;
	}

	public void setFiveHundred(Level fiveHundred) {
		this.fiveHundred = fiveHundred;
	}

	public Level getThreeHundred() {
		return threeHundred;
	}

	public void setThreeHundred(Level threeHundred) {
		this.threeHundred = threeHundred;
	}

	public Level getTwoFifty() {
		return twoFifty;
	}

	public void setTwoFifty(Level twoFifty) {
		this.twoFifty = twoFifty;
	}

	public Level getTwoHundred() {
		return twoHundred;
	}

	public void setTwoHundred(Level twoHundred) {
		this.twoHundred = twoHundred;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
		local.setLatitude(latitude);
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
		local.setLongitude(longitude);
	}
	

	public Position getLocal() {
		return local;
	}

	public void setLocal(Position local) {
		this.local = local;
	}

}
