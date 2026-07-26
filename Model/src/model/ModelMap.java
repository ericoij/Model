package model;

import java.util.ArrayList;
import java.util.List;

/**
 * Compatibility registry for code that used the original static balloon list.
 * New forecasts use {@link Analysis} and {@link GridState}.
 */
@Deprecated
public final class ModelMap {
	private static final ArrayList<Location> BALLOONS = new ArrayList<>();

	private ModelMap() {
	}

	public static void cleanBalloons() {
		BALLOONS.clear();
	}

	public static void addBalloon(Location location) {
		if (location != null) BALLOONS.add(location);
	}

	public static ArrayList<Location> getBalloons() {
		return new ArrayList<>(BALLOONS);
	}

	public static void setBalloons(ArrayList<Location> observations) {
		BALLOONS.clear();
		if (observations != null) BALLOONS.addAll(observations);
	}

	public static Location getBalloon(int index) {
		return BALLOONS.get(index);
	}

	public static Position getBalloonLocal(int index) {
		return BALLOONS.get(index).getLocal();
	}

	public static boolean containsLocal(Position position) {
		return BALLOONS.stream().anyMatch(location ->
				Double.compare(location.getLatitude(), position.getLatitude()) == 0
				&& Double.compare(location.getLongitude(), position.getLongitude()) == 0);
	}

	public static List<Location> observations() {
		return List.copyOf(BALLOONS);
	}
}
