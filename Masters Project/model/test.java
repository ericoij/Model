package model;

import java.util.ArrayList;

public class test {

	public static void main(String[] args) {

		Balloon balloon = new Balloon();
		balloon.storeValues();
		ModelMap map = new ModelMap();
		//// ModelMap run1 = new ModelMap(map.getMap());
		Physics phys = new Physics(map.getMap(), "ders0");
		ArrayList<Location> newMap = phys.getNewMap();
		map.write500("map0.d", newMap);
		for (int i = 0; i < 9; i++) {
			
			phys = new Physics(newMap, "ders" + i +".d");
			map.write500("map" + i+".d", phys.getNewMap());

		}

		// ModelMap.writeWindVectors(map);
	}
}
