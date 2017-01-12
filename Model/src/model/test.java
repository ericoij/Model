package model;

public class test {

	public static void main(String[] args){
		
		Balloon balloon = new Balloon();
		balloon.storeValues();
		ModelMap map = new ModelMap();
		ModelMap run1 = new ModelMap(map.getMap());
	}
}
