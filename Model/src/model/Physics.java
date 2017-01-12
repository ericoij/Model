package model;

public class Physics {
	
	public Physics(){
		
	}
	
	public static double DuDx(Location loc, Level lev){
		
		
		
		
		
		return 0;
	}
	
	public Location DUDX(Location one, Location two, Location loc) {
		double d1, d2;
		double dlon, dlat, a, c;

		
		
		dlon = one.getLongitude() - loc.getLongitude();
		dlat = one.getLatitude() - loc.getLatitude();
		a = Math.pow(Math.sin(dlat / 2), 2)
				+ Math.cos(loc.getLatitude()) * Math.cos(one.getLatitude()) * Math.pow((Math.sin(dlon / 2)), 2);
		c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		d1 = 6371 * c;

		dlon = two.getLongitude() - loc.getLongitude();
		dlat = two.getLatitude() - loc.getLatitude();
		a = Math.pow(Math.sin(dlat / 2), 2)
				+ Math.cos(loc.getLatitude()) * Math.cos(two.getLatitude()) * Math.pow((Math.sin(dlon / 2)), 2);
		c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		d2 = 6371 * c;
		
		
		
		
	return loc;	
	}
}
