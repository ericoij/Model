package model;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
//import java.util.Map;

public class ModelMap {

	private final int BALLOONCOUNT = 96;
	private final double LATLONSCALE = .25;
	private final double LATLOW = 30;
	private final double LATHIGH = 50;
	private final double LONLOW = -120;
	private final double LONHIGH = -80;

	private static ArrayList<Location> Map = new ArrayList<Location>();// <Latitude><Longitude>;
	private static ArrayList<Location> Balloons = new ArrayList<Location>();
	private static Location activeLocation;
	static FileWriter fw;// = new FileWriter("MapData.d");

	public ModelMap() {
		// initialize map,
		int number = 0;
		for (int i = 0; i <= (1 / LATLONSCALE) * (Math.abs(LONHIGH - LONLOW)); i++) { // 160
			for (int j = 0; j <= (1 / LATLONSCALE) * (Math.abs(LATHIGH - LATLOW)); j++) {

				activeLocation = new Location();
				activeLocation.setLatitude(LATLOW + j * LATLONSCALE);
				activeLocation.setLongitude(LONHIGH - i * LATLONSCALE);

				Location nearest, second;
				nearest = findNearestBalloon(activeLocation);
				second = findSecNearestBalloon(activeLocation);
				number = number + 1;
				activeLocation = WieghtedAverage(nearest, second, activeLocation);
				Map.add(activeLocation);

			}
		}

		showMap();
		write500();
		writeBalloonData();
		try {
			writeWindVectors();
		} catch (Exception exception) {
			System.out.println("exceptionThrown");
		}
	}

	public Location initializeOutside(Location loc) { // this does thisd
		Location nearest = findNearestBalloon(loc);
		// nearest.getFiveHundred().showLevel();
		if (nearest != null) {
			if (nearest.getOneThousand() != null) {
				loc.setOneThousand(new Level(nearest.getFiveHundred().getPressure(),
						nearest.getFiveHundred().getGeoHeight(), nearest.getFiveHundred().getWindSpeed(),
						nearest.getFiveHundred().getWindDirection(), nearest.getFiveHundred().getDewPointDepression(),
						nearest.getFiveHundred().getTempurature(), nearest.getFiveHundred().getRelativeHumidity()));
			}
			if (nearest.getNineTwentyFive() != null) {
				loc.setNineTwentyFive(new Level(nearest.getNineTwentyFive().getPressure(),
						nearest.getNineTwentyFive().getGeoHeight(), nearest.getNineTwentyFive().getWindSpeed(),
						nearest.getNineTwentyFive().getWindDirection(),
						nearest.getNineTwentyFive().getDewPointDepression(),
						nearest.getNineTwentyFive().getTempurature(),
						nearest.getNineTwentyFive().getRelativeHumidity()));
			}
			if (nearest.getEightFifty() != null) {
				loc.setEightFifty(new Level(nearest.getEightFifty().getPressure(),
						nearest.getEightFifty().getGeoHeight(), nearest.getEightFifty().getWindSpeed(),
						nearest.getEightFifty().getWindDirection(), nearest.getEightFifty().getDewPointDepression(),
						nearest.getEightFifty().getTempurature(), nearest.getEightFifty().getRelativeHumidity()));
			}
			if (nearest.getSevenHundred() != null) {
				loc.setSevenHundred(new Level(nearest.getSevenHundred().getPressure(),
						nearest.getSevenHundred().getGeoHeight(), nearest.getSevenHundred().getWindSpeed(),
						nearest.getSevenHundred().getWindDirection(), nearest.getSevenHundred().getDewPointDepression(),
						nearest.getSevenHundred().getTempurature(), nearest.getSevenHundred().getRelativeHumidity()));
			}
			if (nearest.getFiveHundred() != null) {
				loc.setFiveHundred(new Level(nearest.getFiveHundred().getPressure(),
						nearest.getFiveHundred().getGeoHeight(), nearest.getFiveHundred().getWindSpeed(),
						nearest.getFiveHundred().getWindDirection(), nearest.getFiveHundred().getDewPointDepression(),
						nearest.getFiveHundred().getTempurature(), nearest.getFiveHundred().getRelativeHumidity()));
			}
			if (nearest.getThreeHundred() != null) {
				loc.setThreeHundred(new Level(nearest.getThreeHundred().getPressure(),
						nearest.getThreeHundred().getGeoHeight(), nearest.getThreeHundred().getWindSpeed(),
						nearest.getThreeHundred().getWindDirection(), nearest.getThreeHundred().getDewPointDepression(),
						nearest.getThreeHundred().getTempurature(), nearest.getThreeHundred().getRelativeHumidity()));
			}
			if (nearest.getTwoFifty() != null) {
				loc.setTwoFifty(new Level(nearest.getTwoFifty().getPressure(), nearest.getTwoFifty().getGeoHeight(),
						nearest.getTwoFifty().getWindSpeed(), nearest.getTwoFifty().getWindDirection(),
						nearest.getTwoFifty().getDewPointDepression(), nearest.getTwoFifty().getTempurature(),
						nearest.getTwoFifty().getRelativeHumidity()));
			}
			if (nearest.getTwoHundred() != null) {
				loc.setTwoHundred(new Level(nearest.getTwoHundred().getPressure(),
						nearest.getTwoHundred().getGeoHeight(), nearest.getTwoHundred().getWindSpeed(),
						nearest.getTwoHundred().getWindDirection(), nearest.getTwoHundred().getDewPointDepression(),
						nearest.getTwoHundred().getTempurature(), nearest.getTwoHundred().getRelativeHumidity()));
			}
		}
		return loc;
	}

	public Location initializeInside(Location loc) {

		return loc;
	}

	// private void gatherBalloonData() {
	// int i = 1; // why is this 1, not zero?
	// while (i < BALLOONCOUNT) {
	// getBalloons().get(i).setLongitude((getBalloons().get(i).getLongitude() *
	// Math.pow(10, -4)));
	// getBalloons().get(i).setLatitude((getBalloons().get(i).getLatitude() *
	// Math.pow(10, -4)));
	// if (getBalloons().get(i).getLongitude() < LONLOW ||
	// getBalloons().get(i).getLatitude() > LONHIGH
	// || getBalloons().get(i).getLatitude() > LATHIGH ||
	// getBalloons().get(i).getLatitude() < LATLOW) {
	// getBalloons().remove(i);
	// }
	//
	// }
	// }

	private Location findNearestBalloon(Location loc) {

		// Balloon nearest = new Balloon();
		int smallInd = 0;
		double dlon, dlat, a, c, dSmall, d;
		if (Balloons.get(1) != null) {

			dSmall = distance(Balloons.get(1).getLocal(), loc.getLocal());// =
			for (int i = 2; i < Balloons.size(); i++) {
				d = distance(Balloons.get(i).getLocal(), loc.getLocal());// 6371
																			// *
																			// c;
				if (d < dSmall) {
					dSmall = d;
					smallInd = i;
				}
			}
		}
		// System.out.println("smalling: " + smallInd);
		return Balloons.get(smallInd);
		// gatherBalloonData();
	}

	private Location findSecNearestBalloon(Location loc) {
		int smallInd = 0;
		int secInd = 1;
		double dlon, dlat, a, c, dSmall, d, dSec, tmpD;

		dSmall = distance(Balloons.get(1).getLocal(), loc.getLocal());// 6371 *
																		// c;
		dSec = distance(Balloons.get(2).getLocal(), loc.getLocal());// 6371 * c;
		if (dSmall > dSec) {
			tmpD = dSmall;
			dSmall = dSec;
			dSec = tmpD;
			smallInd = 1;
			secInd = 0;
		}

		for (int i = 3; i < Balloons.size(); i++) {

			d = distance(Balloons.get(i).getLocal(), loc.getLocal());// 6371 *
																		// c;
			if (d < dSmall) {
				tmpD = dSmall;
				dSmall = d;
				secInd = smallInd;
				smallInd = i;
				dSec = tmpD;
			} else if (d < dSec) {
				dSec = d;
				secInd = i;
			}
		}
		return Balloons.get(secInd);
	}

	public double deltaXY(Position one, Position two) {
		double d1;
		double dlon, dlat, a, c;

		dlon = one.getLongitude() - two.getLongitude();
		dlat = one.getLatitude() - two.getLatitude();
		a = Math.pow(Math.sin(dlat / 2), 2)
				+ Math.cos(two.getLatitude()) * Math.cos(one.getLatitude()) * Math.pow((Math.sin(dlon / 2)), 2);
		c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		d1 = 6371 * c;

		return d1;
	}

	public static double distance(Position one, Position two) {
		Double lat1 = one.getLatitude();
		Double lat2 = two.getLatitude();
		Double lon1 = one.getLongitude();
		Double lon2 = two.getLongitude();
		// Double el1 = (double) 0;
		// Double el2 = (double) 0;
		final int R = 6371; // Radius of the earth

		Double latDistance = Math.toRadians(lat2 - lat1);
		Double lonDistance = Math.toRadians(lon2 - lon1);
		Double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) + Math.cos(Math.toRadians(lat1))
				* Math.cos(Math.toRadians(lat2)) * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
		Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		double distance = R * c * 1000; // convert to meters

		// double height = el1 - el2;

		distance = Math.pow(distance, 2);// + Math.pow(height, 2);

		return Math.sqrt(distance);
	}

	public Location WieghtedAverage(Location one, Location two, Location loc) {
		double d1, d2;
		double dlon, dlat, a, c;
		double geo, dpdp, u, v, temp, rh;

		d1 = distance(one.getLocal(), loc.getLocal());// 6371 * c;

		d2 = distance(two.getLocal(), loc.getLocal());// 6371 * c;

		if (one.getOneThousand() != null && two.getOneThousand() != null) {
			loc.setOneThousand(new Level());
			geo = 0;
			u = 0;
			v = 0;
			dpdp = 0;
			temp = 0;
			rh = 0;

			System.out.println(one.getOneThousand().getWindVector());

			geo = one.getOneThousand().getGeoHeight() * (d2 / (d1 + d2))
					+ two.getOneThousand().getGeoHeight() * (d1 / (d1 + d2));
			u = one.getOneThousand().getWindSpeed() * (d2 / (d1 + d2))
					+ two.getOneThousand().getWindSpeed() * (d1 / (d1 + d2));
			v = one.getOneThousand().getWindDirection() * (d2 / (d1 + d2))
					+ two.getOneThousand().getWindDirection() * (d1 / (d1 + d2));
			dpdp = one.getOneThousand().getDewPointDepression() * (d2 / (d1 + d2))
					+ two.getOneThousand().getDewPointDepression() * (d1 / (d1 + d2));
			temp = one.getOneThousand().getTempurature() * (d2 / (d1 + d2))
					+ two.getOneThousand().getTempurature() * (d1 / (d1 + d2));
			rh = one.getOneThousand().getRelativeHumidity() * (d2 / (d1 + d2))
					+ two.getOneThousand().getRelativeHumidity() * (d1 / (d1 + d2));

			if (!Double.isNaN(geo)) {
				geo = 0.0;
			}
			if (!Double.isNaN(u)) {
				u = 0.0;
			}
			if (!Double.isNaN(v)) {
				v = 0.0;
			}
			if (!Double.isNaN(dpdp)) {
				dpdp = 0.0;
			}
			if (!Double.isNaN(temp)) {
				temp = 0.0;
			}
			if (!Double.isNaN(rh)) {
				rh = 0.0;
			}

			WindVector tempWV = new WindVector(u, v, true);

			loc.setOneThousand(new Level(1000.0, geo, tempWV, dpdp, temp, rh));

		}
		if (one.getNineTwentyFive() != null && two.getNineTwentyFive() != null) {
			loc.setNineTwentyFive(new Level());
			geo = 0;
			u = 0;
			v = 0;
			dpdp = 0;
			temp = 0;
			rh = 0;

			// System.out.println(
			// one.getNineTwentyFive().getWindVector().getU());

			geo = one.getNineTwentyFive().getGeoHeight() * (d2 / (d1 + d2))
					+ two.getNineTwentyFive().getGeoHeight() * (d1 / (d1 + d2));
			u = one.getNineTwentyFive().getWindSpeed() * (d2 / (d1 + d2))
					+ two.getNineTwentyFive().getWindSpeed() * (d1 / (d1 + d2));
			v = one.getNineTwentyFive().getWindDirection() * (d2 / (d1 + d2))
					+ two.getNineTwentyFive().getWindDirection() * (d1 / (d1 + d2));
			dpdp = one.getNineTwentyFive().getDewPointDepression() * (d2 / (d1 + d2))
					+ two.getNineTwentyFive().getDewPointDepression() * (d1 / (d1 + d2));
			temp = one.getNineTwentyFive().getTempurature() * (d2 / (d1 + d2))
					+ two.getNineTwentyFive().getTempurature() * (d1 / (d1 + d2));
			rh = one.getNineTwentyFive().getRelativeHumidity() * (d2 / (d1 + d2))
					+ two.getNineTwentyFive().getRelativeHumidity() * (d1 / (d1 + d2));

			if (Double.isNaN(geo)) {
				geo = 0.0;
			}
			if (Double.isNaN(u)) {
				u = 0.0;
			}
			if (Double.isNaN(v)) {
				v = 0.0;
			}
			if (Double.isNaN(dpdp)) {
				dpdp = 0.0;
			}
			if (Double.isNaN(temp)) {
				temp = 0.0;
			}
			if (Double.isNaN(rh)) {
				rh = 0.0;
			}
			WindVector tempWV = new WindVector(u, v, true);

			loc.setNineTwentyFive(new Level(925.0, geo, tempWV, dpdp, temp, rh));
		}

		if (one.getEightFifty() != null && two.getEightFifty() != null) {
			loc.setEightFifty(new Level());
			geo = 0;
			u = 0;
			v = 0;
			dpdp = 0;
			temp = 0;
			rh = 0;

			geo = one.getEightFifty().getGeoHeight() * (d2 / (d1 + d2))
					+ two.getEightFifty().getGeoHeight() * (d1 / (d1 + d2));
			u = one.getEightFifty().getWindSpeed() * (d2 / (d1 + d2))
					+ two.getEightFifty().getWindSpeed() * (d1 / (d1 + d2));
			v = one.getEightFifty().getWindDirection() * (d2 / (d1 + d2))
					+ two.getEightFifty().getWindDirection() * (d1 / (d1 + d2));
			dpdp = one.getEightFifty().getDewPointDepression() * (d2 / (d1 + d2))
					+ two.getEightFifty().getDewPointDepression() * (d1 / (d1 + d2));
			temp = one.getEightFifty().getTempurature() * (d2 / (d1 + d2))
					+ two.getEightFifty().getTempurature() * (d1 / (d1 + d2));
			rh = one.getEightFifty().getRelativeHumidity() * (d2 / (d1 + d2))
					+ two.getEightFifty().getRelativeHumidity() * (d1 / (d1 + d2));

			if (Double.isNaN(geo)) {
				geo = 0.0;
			}
			if (Double.isNaN(u)) {
				u = 0.0;
			}
			if (Double.isNaN(v)) {
				v = 0.0;
			}
			if (Double.isNaN(dpdp)) {
				dpdp = 0.0;
			}
			if (Double.isNaN(temp)) {
				temp = 0.0;
			}
			if (Double.isNaN(rh)) {
				rh = 0.0;
			}
			WindVector tempWV = new WindVector(u, v, true);

			loc.setEightFifty(new Level(850.0, geo, tempWV, dpdp, temp, rh));
		}

		if (one.getSevenHundred() != null && two.getSevenHundred() != null) {
			loc.setSevenHundred(new Level());
			geo = 0;
			u = 0;
			v = 0;
			dpdp = 0;
			temp = 0;
			rh = 0;

			geo = one.getSevenHundred().getGeoHeight() * (d2 / (d1 + d2))
					+ two.getSevenHundred().getGeoHeight() * (d1 / (d1 + d2));
			u = one.getSevenHundred().getWindSpeed() * (d2 / (d1 + d2))
					+ two.getSevenHundred().getWindSpeed() * (d1 / (d1 + d2));
			v = one.getSevenHundred().getWindDirection() * (d2 / (d1 + d2))
					+ two.getSevenHundred().getWindDirection() * (d1 / (d1 + d2));
			dpdp = one.getSevenHundred().getDewPointDepression() * (d2 / (d1 + d2))
					+ two.getSevenHundred().getDewPointDepression() * (d1 / (d1 + d2));
			temp = one.getSevenHundred().getTempurature() * (d2 / (d1 + d2))
					+ two.getSevenHundred().getTempurature() * (d1 / (d1 + d2));
			rh = one.getSevenHundred().getRelativeHumidity() * (d2 / (d1 + d2))
					+ two.getSevenHundred().getRelativeHumidity() * (d1 / (d1 + d2));

			if (Double.isNaN(geo)) {
				geo = 0.0;
			}
			if (Double.isNaN(u)) {
				u = 0.0;
			}
			if (Double.isNaN(v)) {
				v = 0.0;
			}
			if (Double.isNaN(dpdp)) {
				dpdp = 0.0;
			}
			if (Double.isNaN(temp)) {
				temp = 0.0;
			}
			if (Double.isNaN(rh)) {
				rh = 0.0;
			}
			WindVector tempWV = new WindVector(u, v, true);

			loc.setSevenHundred(new Level(700.0, geo, tempWV, dpdp, temp, rh));
		}
		loc.setFiveHundred(new Level());
		if (one.getFiveHundred() != null && two.getFiveHundred() != null) {
			loc.setFiveHundred(new Level());
			geo = 0;
			u = 0;
			v = 0;
			dpdp = 0;
			temp = 0;
			rh = 0;

			System.out.println(one.getFiveHundred().getWindVector().getU());

			geo = one.getFiveHundred().getGeoHeight() * (d2 / (d1 + d2))
					+ two.getFiveHundred().getGeoHeight() * (d1 / (d1 + d2));
			u = one.getFiveHundred().getWindVector().getU() * (d2 / (d1 + d2))
					+ two.getFiveHundred().getWindVector().getU() * (d1 / (d1 + d2));
			v = one.getFiveHundred().getWindVector().getV() * (d2 / (d1 + d2))
					+ two.getFiveHundred().getWindVector().getV() * (d1 / (d1 + d2));
			dpdp = one.getFiveHundred().getDewPointDepression() * (d2 / (d1 + d2))
					+ two.getFiveHundred().getDewPointDepression() * (d1 / (d1 + d2));
			temp = one.getFiveHundred().getTempurature() * (d2 / (d1 + d2))
					+ two.getFiveHundred().getTempurature() * (d1 / (d1 + d2));
			rh = one.getFiveHundred().getRelativeHumidity() * (d2 / (d1 + d2))
					+ two.getFiveHundred().getRelativeHumidity() * (d1 / (d1 + d2));

			if (Double.isNaN(geo)) {
				geo = 0.0;
			}
			if (Double.isNaN(u)) {
				u = 0.0;
			}
			if (Double.isNaN(v)) {
				v = 0.0;
			}
			if (Double.isNaN(dpdp)) {
				dpdp = 0.0;
			}
			if (Double.isNaN(temp)) {
				temp = 0.0;
			}
			if (Double.isNaN(rh)) {
				rh = 0.0;
			}
			WindVector tempWV = new WindVector(u, v, true);

			loc.setFiveHundred(new Level(500.0, geo, tempWV, dpdp, temp, rh));
		}

		if (one.getThreeHundred() != null && two.getThreeHundred() != null) {
			loc.setThreeHundred(new Level());
			geo = 0;
			u = 0;
			v = 0;
			dpdp = 0;
			temp = 0;
			rh = 0;

			geo = one.getThreeHundred().getGeoHeight() * (d2 / (d1 + d2))
					+ two.getThreeHundred().getGeoHeight() * (d1 / (d1 + d2));
			u = one.getThreeHundred().getWindSpeed() * (d2 / (d1 + d2))
					+ two.getThreeHundred().getWindSpeed() * (d1 / (d1 + d2));
			v = one.getThreeHundred().getWindDirection() * (d2 / (d1 + d2))
					+ two.getThreeHundred().getWindDirection() * (d1 / (d1 + d2));
			dpdp = one.getThreeHundred().getDewPointDepression() * (d2 / (d1 + d2))
					+ two.getThreeHundred().getDewPointDepression() * (d1 / (d1 + d2));
			temp = one.getThreeHundred().getTempurature() * (d2 / (d1 + d2))
					+ two.getThreeHundred().getTempurature() * (d1 / (d1 + d2));
			rh = one.getThreeHundred().getRelativeHumidity() * (d2 / (d1 + d2))
					+ two.getThreeHundred().getRelativeHumidity() * (d1 / (d1 + d2));

			if (Double.isNaN(geo)) {
				geo = 0.0;
			}
			if (Double.isNaN(u)) {
				u = 0.0;
			}
			if (Double.isNaN(v)) {
				v = 0.0;
			}
			if (Double.isNaN(dpdp)) {
				dpdp = 0.0;
			}
			if (Double.isNaN(temp)) {
				temp = 0.0;
			}
			if (Double.isNaN(rh)) {
				rh = 0.0;
			}
			WindVector tempWV = new WindVector(u, v, true);

			loc.setThreeHundred(new Level(300.0, geo, tempWV, dpdp, temp, rh));
		}

		if (one.getTwoFifty() != null && two.getTwoFifty() != null) {
			loc.setTwoFifty(new Level());
			geo = 0;
			u = 0;
			v = 0;
			dpdp = 0;
			temp = 0;
			rh = 0;

			geo = one.getTwoFifty().getGeoHeight() * (d2 / (d1 + d2))
					+ two.getTwoFifty().getGeoHeight() * (d1 / (d1 + d2));
			u = one.getTwoFifty().getWindSpeed() * (d2 / (d1 + d2))
					+ two.getTwoFifty().getWindSpeed() * (d1 / (d1 + d2));
			v = one.getTwoFifty().getWindDirection() * (d2 / (d1 + d2))
					+ two.getTwoFifty().getWindDirection() * (d1 / (d1 + d2));
			dpdp = one.getTwoFifty().getDewPointDepression() * (d2 / (d1 + d2))
					+ two.getTwoFifty().getDewPointDepression() * (d1 / (d1 + d2));
			temp = one.getTwoFifty().getTempurature() * (d2 / (d1 + d2))
					+ two.getTwoFifty().getTempurature() * (d1 / (d1 + d2));
			rh = one.getTwoFifty().getRelativeHumidity() * (d2 / (d1 + d2))
					+ two.getTwoFifty().getRelativeHumidity() * (d1 / (d1 + d2));

			if (Double.isNaN(geo)) {
				geo = 0.0;
			}
			if (Double.isNaN(u)) {
				u = 0.0;
			}
			if (Double.isNaN(v)) {
				v = 0.0;
			}
			if (Double.isNaN(dpdp)) {
				dpdp = 0.0;
			}
			if (Double.isNaN(temp)) {
				temp = 0.0;
			}
			if (Double.isNaN(rh)) {
				rh = 0.0;
			}
			WindVector tempWV = new WindVector(u, v, true);

			loc.setTwoFifty(new Level(250.0, geo, tempWV, dpdp, temp, rh));
		}

		if (one.getTwoHundred() != null && two.getTwoHundred() != null) {
			loc.setTwoHundred(new Level());
			geo = 0;
			u = 0;
			v = 0;
			dpdp = 0;
			temp = 0;
			rh = 0;

			geo = one.getTwoHundred().getGeoHeight() * (d2 / (d1 + d2))
					+ two.getTwoHundred().getGeoHeight() * (d1 / (d1 + d2));
			u = one.getTwoHundred().getWindSpeed() * (d2 / (d1 + d2))
					+ two.getTwoHundred().getWindSpeed() * (d1 / (d1 + d2));
			v = one.getTwoHundred().getWindDirection() * (d2 / (d1 + d2))
					+ two.getTwoHundred().getWindDirection() * (d1 / (d1 + d2));
			dpdp = one.getTwoHundred().getDewPointDepression() * (d2 / (d1 + d2))
					+ two.getTwoHundred().getDewPointDepression() * (d1 / (d1 + d2));
			temp = one.getTwoHundred().getTempurature() * (d2 / (d1 + d2))
					+ two.getTwoHundred().getTempurature() * (d1 / (d1 + d2));
			rh = one.getTwoHundred().getRelativeHumidity() * (d2 / (d1 + d2))
					+ two.getTwoHundred().getRelativeHumidity() * (d1 / (d1 + d2));

			if (Double.isNaN(geo)) {
				geo = 0.0;
			}
			if (Double.isNaN(u)) {
				u = 0.0;
			}
			if (Double.isNaN(v)) {
				v = 0.0;
			}
			if (Double.isNaN(dpdp)) {
				dpdp = 0.0;
			}
			if (Double.isNaN(temp)) {
				temp = 0.0;
			}
			if (Double.isNaN(rh)) {
				rh = 0.0;
			}
			WindVector tempWV = new WindVector(u, v, true);

			loc.setTwoHundred(new Level(200.0, geo, tempWV, dpdp, temp, rh));
		}
		return loc;
	}

	public ArrayList<Location> getMap() {
		return Map;
	}

	public static void setMap(ArrayList<Location> map) {
		Map = map;
	}

	public static ArrayList<Location> getBalloons() {
		return Balloons;
	}

	public static void setBalloons(ArrayList<Location> balloons) {
		Balloons = balloons;
	}

	public static void addBalloon(Location loc) {
		Balloons.add(loc);
	}

	public static Location getBalloon(int index) {
		Location loc = new Location();
		loc = Balloons.get(index);
		return loc;
	}

	public static Position getBalloonLocal(int index) {
		Position local = new Position();
		local = Balloons.get(index).getLocal();
		return local;
	}

	public static void showMap() {
		try {
			fw = new FileWriter("/Users/ericoij/Desktop/balloon/MapData.d");
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(
					"latitude, longitude, 1000GEO, 1000WS, 1000WD, 1000DPD, 1000TEMP, 1000RH, 925GEO, 925WS, 925WD, 925DPD, 925TEMP, 925RH, 850GEO, 850WS, 850WD, 850DPD, 850TEMP, 850RH, 700GEO, 700WS, 700WD, 700DPD, 700TEMP, 700RH, 500GEO, 500WS, 500WD, 500DPD, 500TEMP, 500RH, 300GEO, 300WS, 300WD, 300DPD, 300TEMP, 300RH, 250GEO, 250WS, 250WD, 250DPD, 250TEMP, 250RH 200GEO, 200WS, 200WD, 200DPD, 200TEMP, 200RH");
			bw.newLine();
			String buffer = "";
			for (int i = 0; i < Map.size(); i++) {

				// System.out.println("Latitude: " +
				// Map.get(i).getLocal().getLatitude() + " Longitude: "
				// + Map.get(i).getLocal().getLongitude());
				// bw.write(Map.get(i).getLocal().getLatitude() + "," +
				// Map.get(i).getLocal().getLongitude() + "\n");
				buffer = MapToCSVString(i);
				buffer = buffer.replaceAll("-9999", "");
				buffer = buffer.replaceAll("-8888", "");
				bw.write(buffer);
				bw.newLine();

			}
			bw.close();
			fw.close();
		} catch (

		IOException ioe) {
		}
	}

	public static void writeBalloonData() {
		try {
			fw = new FileWriter("/Users/ericoij/Desktop/balloon/BalloonsData.d");
			BufferedWriter bw = new BufferedWriter(fw);
			System.out.println("Balloon Size:" + Balloons.size());
			bw.write(
					"latitude, longitude, 1000GEO, 1000WS, 1000WD, 1000DPD, 1000TEMP, 1000RH, 925GEO, 925WS, 925WD, 925DPD, 925TEMP, 925RH, 850GEO, 850WS, 850WD, 850DPD, 850TEMP, 850RH, 700GEO, 700WS, 700WD, 700DPD, 700TEMP, 700RH, 500GEO, 500WS, 500WD, 500DPD, 500TEMP, 500RH, 300GEO, 300WS, 300WD, 300DPD, 300TEMP, 300RH, 250GEO, 250WS, 250WD, 250DPD, 250TEMP, 250RH 200GEO, 200WS, 200WD, 200DPD, 200TEMP, 200RH");
			bw.newLine();
			String buffer = "";
			for (int i = 1; i < Balloons.size(); i++) {
				if (Balloons.get(i).getFiveHundred() != null) {
					System.out.println("Latitude: " + Balloons.get(i).getLocal().getLatitude() + " Longitude: "
							+ Balloons.get(i).getLocal().getLongitude() + "  500 temp: "
							+ Balloons.get(i).getFiveHundred().getTempurature());
					// bw.write("Latitude: " +
					// Balloons.get(i).getLocal().getLatitude() + " Longitude: "
					// + Balloons.get(i).getLocal().getLongitude() + " 500 temp:
					// "
					// + Balloons.get(i).getFiveHundred().getTempurature() +
					// "\n");
					buffer = BalloonToCSVString(i);
					buffer = buffer.replaceAll("-9999", "");
					buffer = buffer.replaceAll("-8888", "");
					bw.write(buffer);
					bw.newLine();

				}
			}
			bw.close();
			fw.close();
		} catch (

		IOException ioe) {
		}

	}

	public static void cleanBalloons() {
		for (int i = 0; i < Balloons.size(); i++) {
			Balloons.remove(i);
		}
	}

	public static boolean containsLocal(Position local) {

		for (int i = 0; i < ModelMap.getBalloons().size(); i++) {
			if (getBalloonLocal(i) == local) {
				return true;
			}
		}
		return false;
	}

	public double getDelta() {

		return LATLONSCALE;
	}

	public static String MapToCSVString(int i) {
		String buffer = "";

		{
			buffer += Map.get(i).getLatitude() + ",";
			buffer += Map.get(i).getLongitude() + ",";
			if (Map.get(i).getOneThousand() != null) {
				buffer += Map.get(i).getOneThousand().getGeoHeight() + "," + Map.get(i).getOneThousand().getWindSpeed()
						+ "," + Map.get(i).getOneThousand().getWindDirection() + ","
						+ Map.get(i).getOneThousand().getDewPointDepression() + ","
						+ Map.get(i).getOneThousand().getTempurature() + ","
						+ Map.get(i).getOneThousand().getRelativeHumidity() + ",";
			} else {
				buffer += " , , , , , ,";
			}
			if (Map.get(i).getNineTwentyFive() != null) {
				buffer += Map.get(i).getNineTwentyFive().getGeoHeight() + ","
						+ Map.get(i).getNineTwentyFive().getWindSpeed() + ","
						+ Map.get(i).getNineTwentyFive().getWindDirection() + ","
						+ Map.get(i).getNineTwentyFive().getDewPointDepression() + ","
						+ Map.get(i).getNineTwentyFive().getTempurature() + ","
						+ Map.get(i).getNineTwentyFive().getRelativeHumidity() + ",";
			} else {
				buffer += " , , , , , ,";
			}
			if (Map.get(i).getEightFifty() != null) {
				buffer += Map.get(i).getEightFifty().getGeoHeight() + "," + Map.get(i).getEightFifty().getWindSpeed()
						+ "," + Map.get(i).getEightFifty().getWindDirection() + ","
						+ Map.get(i).getEightFifty().getDewPointDepression() + ","
						+ Map.get(i).getEightFifty().getTempurature() + ","
						+ Map.get(i).getEightFifty().getRelativeHumidity() + ",";
			} else {
				buffer += " , , , , , ,";
			}
			if (Map.get(i).getSevenHundred() != null) {
				buffer += Map.get(i).getSevenHundred().getGeoHeight() + ","
						+ Map.get(i).getSevenHundred().getWindSpeed() + ","
						+ Map.get(i).getSevenHundred().getWindDirection() + ","
						+ Map.get(i).getSevenHundred().getDewPointDepression() + ","
						+ Map.get(i).getSevenHundred().getTempurature() + ","
						+ Map.get(i).getSevenHundred().getRelativeHumidity() + ",";
			} else {
				buffer += " , , , , , ,";
			}
			if (Map.get(i).getFiveHundred() != null) {
				buffer += Map.get(i).getFiveHundred().getGeoHeight() + "," + Map.get(i).getFiveHundred().getWindSpeed()
						+ "," + Map.get(i).getFiveHundred().getWindDirection() + ","
						+ Map.get(i).getFiveHundred().getDewPointDepression() + ","
						+ Map.get(i).getFiveHundred().getTempurature() + ","
						+ Map.get(i).getFiveHundred().getRelativeHumidity() + ",";
			} else {
				buffer += " , , , , , ,";
			}
			if (Map.get(i).getThreeHundred() != null) {
				buffer += Map.get(i).getThreeHundred().getGeoHeight() + ","
						+ Map.get(i).getThreeHundred().getWindSpeed() + ","
						+ Map.get(i).getThreeHundred().getWindDirection() + ","
						+ Map.get(i).getThreeHundred().getDewPointDepression() + ","
						+ Map.get(i).getThreeHundred().getTempurature() + ","
						+ Map.get(i).getThreeHundred().getRelativeHumidity() + ",";
			} else {
				buffer += " , , , , , ,";
			}
			if (Map.get(i).getTwoFifty() != null) {
				buffer += Map.get(i).getTwoFifty().getGeoHeight() + "," + Map.get(i).getTwoFifty().getWindSpeed() + ","
						+ Map.get(i).getTwoFifty().getWindDirection() + ","
						+ Map.get(i).getTwoFifty().getDewPointDepression() + ","
						+ Map.get(i).getTwoFifty().getTempurature() + ","
						+ Map.get(i).getTwoFifty().getRelativeHumidity() + ",";
			} else {
				buffer += " , , , , , ,";
			}
			if (Map.get(i).getTwoHundred() != null) {
				buffer += Map.get(i).getTwoHundred().getGeoHeight() + "," + Map.get(i).getTwoHundred().getWindSpeed()
						+ "," + Map.get(i).getTwoHundred().getWindDirection() + ","
						+ Map.get(i).getTwoHundred().getDewPointDepression() + ","
						+ Map.get(i).getTwoHundred().getTempurature() + ","
						+ Map.get(i).getTwoHundred().getRelativeHumidity();
			} else {
				buffer += " , , , , , ";
			}

		}
		return buffer;
	}

	public static String BalloonToCSVString(int i) {
		String buffer = "";
		if (Balloons.isEmpty()) {
			{
				buffer += Balloons.get(i).getLatitude() + ",";
				buffer += Balloons.get(i).getLongitude() + ",";
				if (Balloons.get(i).getOneThousand() != null) {
					buffer += Balloons.get(i).getOneThousand().getGeoHeight() + ","
							+ Balloons.get(i).getOneThousand().getWindSpeed() + ","
							+ Balloons.get(i).getOneThousand().getWindDirection() + ","
							+ Balloons.get(i).getOneThousand().getDewPointDepression() + ","
							+ Balloons.get(i).getOneThousand().getTempurature() + ","
							+ Balloons.get(i).getOneThousand().getRelativeHumidity() + ",";
				} else {
					buffer += " , , , , , ,";
				}
				if (Balloons.get(i).getNineTwentyFive() != null) {
					buffer += Balloons.get(i).getNineTwentyFive().getGeoHeight() + ","
							+ Balloons.get(i).getNineTwentyFive().getWindSpeed() + ","
							+ Balloons.get(i).getNineTwentyFive().getWindDirection() + ","
							+ Balloons.get(i).getNineTwentyFive().getDewPointDepression() + ","
							+ Balloons.get(i).getNineTwentyFive().getTempurature() + ","
							+ Balloons.get(i).getNineTwentyFive().getRelativeHumidity() + ",";
				} else {
					buffer += " , , , , , ,";
				}
				if (Balloons.get(i).getEightFifty() != null) {
					buffer += Balloons.get(i).getEightFifty().getGeoHeight() + ","
							+ Balloons.get(i).getEightFifty().getWindSpeed() + ","
							+ Balloons.get(i).getEightFifty().getWindDirection() + ","
							+ Balloons.get(i).getEightFifty().getDewPointDepression() + ","
							+ Balloons.get(i).getEightFifty().getTempurature() + ","
							+ Balloons.get(i).getEightFifty().getRelativeHumidity() + ",";
				} else {
					buffer += " , , , , , ,";
				}
				if (Balloons.get(i).getSevenHundred() != null) {
					buffer += Balloons.get(i).getSevenHundred().getGeoHeight() + ","
							+ Balloons.get(i).getSevenHundred().getWindSpeed() + ","
							+ Balloons.get(i).getSevenHundred().getWindDirection() + ","
							+ Balloons.get(i).getSevenHundred().getDewPointDepression() + ","
							+ Balloons.get(i).getSevenHundred().getTempurature() + ","
							+ Balloons.get(i).getSevenHundred().getRelativeHumidity() + ",";
				} else {
					buffer += " , , , , , ,";
				}
				if (Balloons.get(i).getFiveHundred() != null) {
					buffer += Balloons.get(i).getFiveHundred().getGeoHeight() + ","
							+ Balloons.get(i).getFiveHundred().getWindSpeed() + ","
							+ Balloons.get(i).getFiveHundred().getWindDirection() + ","
							+ Balloons.get(i).getFiveHundred().getDewPointDepression() + ","
							+ Balloons.get(i).getFiveHundred().getTempurature() + ","
							+ Balloons.get(i).getFiveHundred().getRelativeHumidity() + ",";
				} else {
					buffer += " , , , , , ,";
				}
				if (Balloons.get(i).getThreeHundred() != null) {
					buffer += Balloons.get(i).getThreeHundred().getGeoHeight() + ","
							+ Balloons.get(i).getThreeHundred().getWindSpeed() + ","
							+ Balloons.get(i).getThreeHundred().getWindDirection() + ","
							+ Balloons.get(i).getThreeHundred().getDewPointDepression() + ","
							+ Balloons.get(i).getThreeHundred().getTempurature() + ","
							+ Balloons.get(i).getThreeHundred().getRelativeHumidity() + ",";
				} else {
					buffer += " , , , , , ,";
				}
				if (Balloons.get(i).getTwoFifty() != null) {
					buffer += Balloons.get(i).getTwoFifty().getGeoHeight() + ","
							+ Balloons.get(i).getTwoFifty().getWindSpeed() + ","
							+ Balloons.get(i).getTwoFifty().getWindDirection() + ","
							+ Balloons.get(i).getTwoFifty().getDewPointDepression() + ","
							+ Balloons.get(i).getTwoFifty().getTempurature() + ","
							+ Balloons.get(i).getTwoFifty().getRelativeHumidity() + ",";
				} else {
					buffer += " , , , , , ,";
				}
				if (Balloons.get(i).getTwoHundred() != null) {
					buffer += Balloons.get(i).getTwoHundred().getGeoHeight() + ","
							+ Balloons.get(i).getTwoHundred().getWindSpeed() + ","
							+ Balloons.get(i).getTwoHundred().getWindDirection() + ","
							+ Balloons.get(i).getTwoHundred().getDewPointDepression() + ","
							+ Balloons.get(i).getTwoHundred().getTempurature() + ","
							+ Balloons.get(i).getTwoHundred().getRelativeHumidity();
				} else {
					buffer += " , , , , , ";
				}

			}
			return buffer;
		}
		return "";
	}

	public static void write500() {

		try {
			fw = new FileWriter("/Users/ericoij/Desktop/balloon/FiveHundred.d");
			BufferedWriter bw = new BufferedWriter(fw);
			System.out.println("Balloon Size:" + Balloons.size());
			bw.write("latitude, longitude, 500GEO, 500WS, 500WD, 500DPD, 500TEMP, 500RH, 500U, 500V");
			bw.newLine();

			for (int i = 0; i < Map.size(); i++) {
				String buffer = "";
				buffer += Map.get(i).getLatitude() + ",";
				buffer += Map.get(i).getLongitude() + ",";

				if (Map.get(i).getFiveHundred() != null) {
					buffer += Map.get(i).getFiveHundred().getGeoHeight() + ","
							+ Map.get(i).getFiveHundred().getWindSpeed() + ","
							+ Map.get(i).getFiveHundred().getWindDirection() + ","
							+ Map.get(i).getFiveHundred().getDewPointDepression() + ","
							+ Map.get(i).getFiveHundred().getTempurature() + ","
							+ Map.get(i).getFiveHundred().getRelativeHumidity() + ",";
																				// ","
					if (Map.get(i).getFiveHundred().getWindVector() != null) {
						buffer += Map.get(i).getFiveHundred().getWindVector().getU() + ","
								+ Map.get(i).getFiveHundred().getWindVector().getV();
						
					} else{
						buffer += " , ";
					}
				} else {
					buffer += " , , , , , ,";
				}
				buffer = buffer.replaceAll("-9999", "");
				buffer = buffer.replaceAll("-8888", "");
				bw.write(buffer);
				bw.newLine();

			}
			bw.close();
			fw.close();
		} catch (

		IOException ioe) {
		}

	}
	
	public static void write500(String filename, ArrayList<Location> writeMap) {

		try {
			fw = new FileWriter("/Users/ericoij/Desktop/balloon/model/" + filename);
			BufferedWriter bw = new BufferedWriter(fw);
//			System.out.println("Balloon Size:" + Balloons.size());
			bw.write("latitude, longitude, 500GEO, 500WS, 500WD, 500DPD, 500TEMP, 500RH, 500U, 500V");
			bw.newLine();

			for (int i = 0; i < writeMap.size(); i++) {
				String buffer = "";
				buffer += writeMap.get(i).getLatitude() + ",";
				buffer += writeMap.get(i).getLongitude() + ",";

				if (writeMap.get(i).getFiveHundred() != null) {
					buffer += writeMap.get(i).getFiveHundred().getGeoHeight() + ","
							+ writeMap.get(i).getFiveHundred().getWindSpeed() + ","
							+ writeMap.get(i).getFiveHundred().getWindDirection() + ","
							+ writeMap.get(i).getFiveHundred().getDewPointDepression() + ","
							+ writeMap.get(i).getFiveHundred().getTempurature() + ","
							+ writeMap.get(i).getFiveHundred().getRelativeHumidity() + ",";
																				// ","
					if (writeMap.get(i).getFiveHundred().getWindVector() != null) {
						buffer += writeMap.get(i).getFiveHundred().getWindVector().getU() + ","
								+ writeMap.get(i).getFiveHundred().getWindVector().getV();
						
					} else{
						buffer += " , ";
					}
				} else {
					buffer += " , , , , , ,";
				}
				buffer = buffer.replaceAll("-9999", "");
				buffer = buffer.replaceAll("-8888", "");
				bw.write(buffer);
				bw.newLine();

			}
			bw.close();
			fw.close();
		} catch (

		IOException ioe) {
		}

	}

	public static void writeWindVectors() throws IOException {
		ArrayList<WindVector> WVList = new ArrayList<WindVector>();
		System.out.println("printing wind vectors");
		for (int i = 1; i < Map.size(); i++) {
			System.out.println("doing line " + i);
			if (Map.get(i).getEightFifty().getWindSpeed() != null
					&& Map.get(i).getEightFifty().getWindDirection() != null) {
				WVList.set(i, new WindVector(Map.get(i).getEightFifty().getWindSpeed(),
						Map.get(i).getEightFifty().getWindDirection()));
			} else {
				WVList.set(i, new WindVector(0, 0));
			}
		}

		fw = new FileWriter("/Users/ericoij/Desktop/balloon/WindVectors.d");
		BufferedWriter bw = new BufferedWriter(fw);
		for (int i = 1; i < Map.size(); i++) {
			bw.write(Map.get(i).getLatitude() + "," + Map.get(i).getLongitude() + "," + WVList.get(i).getU() + ","
					+ WVList.get(i).getV());
		}
		bw.close();
		fw.close();
	}

}
