package model;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class Physics {
	private final double LATLONSCALE = .25;
	private final double LATLOW = 30;
	private final double LATHIGH = 50;
	private final double LONLOW = -120;
	private final double LONHIGH = -80;
	private final double TIME = 60 ; // ONE HOUR
	private ArrayList<Location> newMap = new ArrayList<Location>();

	private ArrayList<differentialLevel> DifLevel;
	
	public Physics(ArrayList<Location> map, String filename) {
		try{
		ArrayList<Location> newMap = new ArrayList<Location>();
		FileWriter fw = new FileWriter("/Users/ericoij/Desktop/balloon/model/ders/" + filename);
		BufferedWriter bw = new BufferedWriter(fw);
		for (int i = 1; i < map.size(); i++) {
			Double dux, duy, dvx, dvy, dtx, dphix, dty, dphiy, Fcox, Fcoy, dudt=0.0, dvdt=0.0,dTx, dTy, dTdt = 0.0, dphidt=0.0;
			Location activeLocation = new Location(map.get(i).getLatitude(), map.get(i).getLongitude());
//			activeLocation.setLocal(map.get(i).getLocal());
			if (map.get(i).getFiveHundred() != null) {
				if (map.get(i).getFiveHundred().getWindSpeed() != null
						&& map.get(i).getFiveHundred().getWindDirection() != null) {
					if (map.get(i).getFiveHundred().getWindSpeed() != -9999.0) {
						
						if (map.get(i).getLatitude() != LATLOW && map.get(i).getLatitude() != LATHIGH
								&& map.get(i).getLongitude() != LONLOW && map.get(i).getLongitude() != LONHIGH && i > 81 && i < (12552)) {
							
							Double dy = distance(40, 40, 0, .5, 0, 0);
							Double dx = distance(map.get(i).getLatitude(), map.get(i).getLatitude(), 0, .5, 0, 0);

							System.out.println(
									dx + " " + dy + "  " + map.get(i).getLatitude() + "  " + map.get(i).getLongitude());

//							System.out.println(map.get(i - 81).getLongitude() + "  " + map.get(i + 81).getLongitude());
							if(i + 81 < map.size())
							if (map.get(i - 81).getFiveHundred() != null && map.get(i + 81).getFiveHundred() != null
									&& map.get(i + 1).getFiveHundred() != null
									&& map.get(i - 1).getFiveHundred() != null) {
								if (map.get(i - 81).getFiveHundred().getWindVector() != null
										&& map.get(i + 81).getFiveHundred().getWindVector() != null
										&& map.get(i - 1).getFiveHundred().getWindVector() != null
										&& map.get(i + 1).getFiveHundred().getWindVector() != null) {
									dux = map.get(i - 81).getFiveHundred().getWindVector().getU()
											- map.get(i + 81).getFiveHundred().getWindVector().getU();
									dvx = map.get(i - 81).getFiveHundred().getWindVector().getV()
											- map.get(i + 81).getFiveHundred().getWindVector().getV();
									duy = map.get(i - 1).getFiveHundred().getWindVector().getU()
											- map.get(i + 1).getFiveHundred().getWindVector().getU();
									dvy = map.get(i - 1).getFiveHundred().getWindVector().getV()
											- map.get(i + 1).getFiveHundred().getWindVector().getV();

									dtx = map.get(i - 81).getFiveHundred().getTempurature()
											- map.get(i + 81).getFiveHundred().getTempurature();
									dty = map.get(i - 1).getFiveHundred().getTempurature()
											- map.get(i + 1).getFiveHundred().getTempurature();
									
									

									if (map.get(i - 81).getFiveHundred().getGeoHeight() != null
											&& map.get(i + 81).getFiveHundred().getGeoHeight() != null
											&& map.get(i - 1).getFiveHundred().getGeoHeight() != null
											&& map.get(i + 1).getFiveHundred().getGeoHeight() != null) {
										dphix = map.get(i - 81).getFiveHundred().getGeoHeight()
												- map.get(i + 81).getFiveHundred().getGeoHeight();

										dphiy = map.get(i - 1).getFiveHundred().getGeoHeight()
												- map.get(i + 1).getFiveHundred().getGeoHeight();
										
										dTx = map.get(i - 81).getFiveHundred().getTempurature()
												- map.get(i + 81).getFiveHundred().getTempurature();
										
										dTy = map.get(i - 1).getFiveHundred().getTempurature()
												- map.get(i + 1).getFiveHundred().getTempurature();

										Fcox = 7.2921 * Math.pow(10, -5) * Math.sin(map.get(i).getLatitude())
												* map.get(i).getFiveHundred().getWindVector().getV();
										Fcoy = 7.2921 * Math.pow(10, -5) * Math.sin(map.get(i).getLatitude())
												* map.get(i).getFiveHundred().getWindVector().getU();

										dudt = -map.get(i).getFiveHundred().getWindVector().getU() * dux / dx
												- map.get(i).getFiveHundred().getWindVector().getV() * duy / dy
												- dphix / dx + Fcox;

										dvdt = -(map.get(i).getFiveHundred().getWindVector().getU() * dvx) / dx
												-( map.get(i).getFiveHundred().getWindVector().getV() * dvy) / dy
												- (dphiy / dy) + Fcoy;
										
										dTdt = - (dTx / dx) * map.get(i).getFiveHundred().getWindVector().getU() - 
												dTy / dy * map.get(i).getFiveHundred().getWindVector().getV();
										
										dphidt = dTdt * 461 * Math.log(500/1013.15) ;
										
									} else {
										dudt = 0.0;
										dvdt = 0.0;
										dphix = 0.0;
										dphiy = 0.0;
										Fcox = 0.0;
										Fcoy = 0.0;
										dTdt = 0.0;
										dphidt = 0.0;
									}
									System.out
											.println("dux: " + dux + " dvx: " + dvx + " duy: " + duy + " dvy: " + dvy);
									System.out.println(dudt + "    " + dvdt + "  U :"
											+ map.get(i).getFiveHundred().getWindVector().getU() + " phix" + (dphix/dx) + " phiy " + dphiy / dy + "fox: " +Fcox + "fcoy: " + Fcoy);
								} else {
									dux = (double) 0;
									duy = (double) 0;
									dvx = (double) 0;
									dvy = (double) 0;
									dtx = (double) 0;
									dphix = (double) 0;
									dty = (double) 0;
									dphiy = (double) 0;
									Fcox = (double) 0;
									Fcoy = (double) 0;
									dudt = (double) 0;
									dvdt = (double) 0;
									dTdt = 0.0;
									dphidt = 0.0;
								}
//								DifLevel.add(i, new differentialLevel(dudt, dvdt, dTdt));
								bw.write(map.get(i).getLatitude() + "," + map.get(i).getLongitude() + "," + ("dudt: " + dudt + " dvdt: " + dvdt + " dphidt: " + dphidt + " dvy: " + dTdt + " U: "
										+ map.get(i).getFiveHundred().getWindVector().getU() + " V: " + map.get(i).getFiveHundred().getWindVector().getV()+ " Fcox: " +Fcox +" Fcoy: "+Fcoy+ "\n") );
//								bw.write(dudt + "    " + dvdt + "  U :"
//										+ map.get(i).getFiveHundred().getWindVector().getU() + " phix" + (dphix/dx) + " phiy " + dphiy / dy + "fox: " +Fcox + "fcoy: " + Fcoy);
								
								activeLocation.setFiveHundred(new Level(500.0, map.get(i).getFiveHundred().getGeoHeight()* dphidt * TIME,
										new WindVector(map.get(i).getFiveHundred().getWindVector().getU() * dudt * TIME, 
										map.get(i).getFiveHundred().getWindVector().getV() * dvdt * TIME, true), map.get(i).getFiveHundred().getDewPointDepression(),
										map.get(i).getFiveHundred().getTempurature() * dTdt * TIME, map.get(i).getFiveHundred().getRelativeHumidity())
										);
								newMap.add(activeLocation);
								
							}
							
							// Double du =
							// map.get(i-82).getFiveHundred().getWindVector().getU()
							// -
							// map.get(i+82).getFiveHundred().getWindVector().getU();

						}
					}
				}
			}
			
		}
		setNewMap(newMap);
		bw.close();
		fw.close();
		}catch(IOException ioe){}
	}

	public Double deltaY(Position one, Position two) {
		Double d1;
		Double dlon, dlat, a, c;

		dlon = (one.getLongitude() - two.getLongitude()) * ((Math.PI) / 180);
		dlat = (one.getLatitude() - two.getLatitude()) * ((2 * Math.PI) / 360);
		a = Math.pow(Math.sin(dlat / 2), 2)
				+ Math.cos(two.getLatitude()) * Math.cos(one.getLatitude()) * Math.pow((Math.sin(dlon / 2)), 2);
		c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		d1 = 6371 * c;

		return d1;
	}

	public Double deltaX(Position one) {
		Double d1;
		Double dlon, dlat, a, c;

		dlon = ((.5) * (Math.PI) / 180);
		dlat = (0) * ((2 * Math.PI) / 360);
		// a = Math.pow(Math.sin(dlat / 2), 2)
		// + Math.cos(one.getLatitude()) * Math.cos(one.getLatitude()) *
		// Math.pow((Math.sin(dlon / 2)), 2);
		a = Math.pow(Math.sin(dlat / 2) * ((Math.PI) / 180), 2)
				+ Math.cos(one.getLatitude() * ((Math.PI) / 180)) * Math.cos(one.getLatitude() * ((Math.PI) / 180))
						* Math.pow((Math.sin(dlon / 2) * ((Math.PI) / 180)), 2);
		c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		d1 = 6371 * c;

		return d1;
	}

	/**
	 * Calculate distance between two points in latitude and longitude taking
	 * into account height difference. If you are not interested in height
	 * difference pass 0.0. Uses Haversine method as its base.
	 * 
	 * lat1, lon1 Start point lat2, lon2 End point el1 Start altitude in meters
	 * el2 End altitude in meters
	 * 
	 * @returns Distance in Meters
	 *          http://stackoverflow.com/questions/3694380/calculating-distance-between-two-points-using-latitude-longitude-what-am-i-doi
	 */
	public static double distance(double lat1, double lat2, double lon1, double lon2, double el1, double el2) {

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

	public ArrayList<Location> getNewMap() {
		return newMap;
	}

	public void setNewMap(ArrayList<Location> newMap) {
		this.newMap = newMap;
	}
	
	public static void writeDers(String filename, ArrayList<Location> writeMap) {

		try {
			FileWriter fw = new FileWriter("/Users/ericoij/Desktop/balloon/model/ders" + filename);
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
}