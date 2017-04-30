package model;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
//import java.util.Calendar;
//import java.util.Date;
//import java.util.GregorianCalendar;

public class Balloon {

	public ArrayList<Level> levels = new ArrayList<Level>();
	public Location activeLocation;
	String lat, lon;
	Level ActiveLevel;
//	Calendar today = Calendar.getInstance();

	public Balloon() {

	}

	public void storeValues() {
		int i = 0;
		String buffer = "";
		String lineType = "";
		String value = "";
//		Calendar balloonDate = today;
		try {
			FileReader fr = new FileReader("/Users/ericoij/Desktop/balloon/balloon.d");
			BufferedReader br = new BufferedReader(fr);

			while ((buffer = br.readLine()) != null) {
				ActiveLevel = new Level();

				if (buffer.isEmpty())
					lineType = "empty";
				else if (buffer.charAt(0) == '1')
					lineType = "level";
				else if (buffer.charAt(0) == '#') {
					lineType = "station";
//					balloonDate = new GregorianCalendar(Integer.parseInt(buffer.substring(14, 17)),
//							Integer.parseInt(buffer.substring(19, 20)), Integer.parseInt(buffer.substring(22, 23)));
//					System.out.println(balloonDate.getWeekYear());
				} else
					lineType = "BadRead";
				System.out.println(lineType);

				if (lineType == "station") {
					if (buffer.length() == 71) {

						lat = buffer.substring(56, 62).replaceAll(" ", "");
						lon = buffer.substring(63, 71).replaceAll(" ", "");
						double latitude = Integer.parseInt(lat) * Math.pow(10, -4);
						double longitude = Integer.parseInt(lon) * Math.pow(10, -4);

						activeLocation = new Location(latitude, longitude);
						System.out.println("Made a new station, Lat =" + lat + "  Long = " + lon);
					}
				} else if (lineType == "level") {
					System.out.println("writing level" + buffer);
					value = buffer.substring(9, 15).replaceAll(" ", "");
					double pressure = ((double) Integer.parseInt(value));
					value = buffer.substring(16, 21).replaceAll(" ", "");
					double geo = ((double) Integer.parseInt(value));
					value = buffer.substring(22, 27).replaceAll(" ", "");
					double temp = (273.15+.1*(double) Integer.parseInt(value));
					value = buffer.substring(28, 33).replaceAll(" ", "");
					double rh = ((double) Integer.parseInt(value));
					value = buffer.substring(34, 39).replaceAll(" ", "");
					double dpdp = (.1*(double) Integer.parseInt(value));
					value = buffer.substring(40, 45).replaceAll(" ", "");
					double wd = ((double) Integer.parseInt(value));
					value = buffer.substring(46, 51).replaceAll(" ", "");
					double ws = ((double) Integer.parseInt(value));
					ActiveLevel = new Level(pressure, geo, ws, wd, dpdp, temp, rh);

					storeLevel(ActiveLevel);
				} else if (lineType == "empty") {
					ModelMap.addBalloon(activeLocation);
					activeLocation = new Location();
					System.out.println("Add Balloon, LAT: " + lat + "  LON: " + lon + "number of balloons" + i);
					i++;
				}
			}
			br.close();
			fr.close();
		} catch (

		IOException ioe) {
			System.out.println("Exception thrown !!!" + ioe.getMessage());
		}
		System.out.println("this is here");
	}

	public void storeLevel(Level level) {
		if (level.getPressure() == 92500) {
			activeLocation.setNineTwentyFive(level);
		} else if (level.getPressure() == 100000) {
			activeLocation.setOneThousand(level);
		} else if (level.getPressure() == 85000) {
			activeLocation.setEightFifty(level);
		} else if (level.getPressure() == 70000) {
			activeLocation.setSevenHundred(level);
		} else if (level.getPressure() == 50000) {
			activeLocation.setFiveHundred(level);
		} else if (level.getPressure() == 30000) {
			activeLocation.setThreeHundred(level);
		} else if (level.getPressure() == 25000) {
			activeLocation.setTwoFifty(level);
		} else if (level.getPressure() == 20000) {
			activeLocation.setTwoHundred(level);
		}

	}
}
