package model;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Builds a smooth 500 hPa analysis from time-aligned radiosonde observations.
 */
public final class Analysis {
	private static final int NEIGHBOR_COUNT = 8;
	private static final double WEIGHT_FLOOR_KM = 75;

	private Analysis() {
	}

	public static GridState build(List<Location> input, double south, double north,
			double west, double east, double spacingDeg) {
		return buildLevel(input, 500, south, north, west, east, spacingDeg);
	}

	public static GridState buildLevel(List<Location> input, int pressureHpa, double south, double north,
			double west, double east, double spacingDeg) {
		List<Location> usable = input.stream()
				.filter(location -> levelAt(location, pressureHpa) != null)
				.filter(location -> levelAt(location, pressureHpa).isForecastUsable())
				.filter(location -> location.getLatitude() >= south - 8 && location.getLatitude() <= north + 8)
				.filter(location -> location.getLongitude() >= west - 12 && location.getLongitude() <= east + 12)
				.toList();
		if (usable.size() < 4) {
			throw new IllegalArgumentException("At least four valid 500 hPa observations are required");
		}

		Instant cycle = selectBestCycle(usable);
		List<Location> aligned = alignToCycle(usable, cycle);
		if (aligned.size() < 4) {
			throw new IllegalArgumentException("Fewer than four observations share a usable analysis cycle");
		}

		GridState grid = new GridState(south, north, west, east, spacingDeg, cycle, aligned.size(), pressureHpa);
		for (int row = 0; row < grid.rows(); row++) {
			for (int column = 0; column < grid.columns(); column++) {
				fillCell(grid, row, column, aligned, pressureHpa);
			}
		}
		// Radiosondes are sparse relative to this grid. Gentle passes suppress
		// station-scale seams before derivatives are taken.
		grid.smoothInterior(0.18);
		grid.smoothInterior(0.18);
		grid.validatePhysical();
		return grid;
	}

	private static Instant selectBestCycle(List<Location> observations) {
		Map<Instant, Integer> counts = new HashMap<>();
		for (Location location : observations) {
			if (location.getObservedAt() != null) counts.merge(location.getObservedAt(), 1, Integer::sum);
		}
		return counts.entrySet().stream()
				.max(Comparator.<Map.Entry<Instant, Integer>>comparingInt(Map.Entry::getValue)
						.thenComparing(Map.Entry::getKey))
				.map(Map.Entry::getKey)
				.orElse(null);
	}

	private static List<Location> alignToCycle(List<Location> observations, Instant cycle) {
		if (cycle == null) return observations;
		List<Location> exact = observations.stream()
				.filter(location -> cycle.equals(location.getObservedAt()))
				.toList();
		if (exact.size() >= 4) return exact;
		Instant earliest = cycle.minus(Duration.ofHours(6));
		Instant latest = cycle.plus(Duration.ofHours(6));
		return observations.stream()
				.filter(location -> location.getObservedAt() != null)
				.filter(location -> !location.getObservedAt().isBefore(earliest)
						&& !location.getObservedAt().isAfter(latest))
				.toList();
	}

	private static void fillCell(GridState grid, int row, int column, List<Location> observations,
			int pressureHpa) {
		double latitude = grid.latitude(row);
		double longitude = grid.longitude(column);
		List<WeightedObservation> nearest = new ArrayList<>();
		for (Location location : observations) {
			double distance = distanceKm(latitude, longitude, location.getLatitude(), location.getLongitude());
			nearest.add(new WeightedObservation(location, distance));
		}
		nearest.sort(Comparator.comparingDouble(WeightedObservation::distanceKm));
		if (nearest.size() > NEIGHBOR_COUNT) nearest = nearest.subList(0, NEIGHBOR_COUNT);

		double totalWeight = 0;
		double u = 0;
		double v = 0;
		double height = 0;
		double temperature = 0;
		double humidity = 0;
		double humidityWeight = 0;
		for (WeightedObservation item : nearest) {
			double weight = 1.0 / (item.distanceKm * item.distanceKm
					+ WEIGHT_FLOOR_KM * WEIGHT_FLOOR_KM);
			Level level = levelAt(item.location, pressureHpa);
			WindVector wind = level.windVector();
			u += wind.getU() * weight;
			v += wind.getV() * weight;
			height += level.getGeoHeight() * weight;
			temperature += level.getTemperature() * weight;
			totalWeight += weight;
			if (level.getRelativeHumidity() != null && Double.isFinite(level.getRelativeHumidity())) {
				humidity += level.getRelativeHumidity() * weight;
				humidityWeight += weight;
			}
		}
		grid.u[row][column] = u / totalWeight;
		grid.v[row][column] = v / totalWeight;
		grid.height[row][column] = height / totalWeight;
		grid.temperature[row][column] = temperature / totalWeight;
		grid.humidity[row][column] = humidityWeight == 0 ? 50 : humidity / humidityWeight;
	}

	static Level levelAt(Location location, int pressureHpa) {
		return switch (pressureHpa) {
			case 1000 -> location.getOneThousand();
			case 925 -> location.getNineTwentyFive();
			case 850 -> location.getEightFifty();
			case 700 -> location.getSevenHundred();
			case 500 -> location.getFiveHundred();
			case 300 -> location.getThreeHundred();
			case 250 -> location.getTwoFifty();
			case 200 -> location.getTwoHundred();
			default -> throw new IllegalArgumentException("Unsupported pressure level: " + pressureHpa);
		};
	}

	static double distanceKm(double lat1, double lon1, double lat2, double lon2) {
		double phi1 = Math.toRadians(lat1);
		double phi2 = Math.toRadians(lat2);
		double dPhi = phi2 - phi1;
		double dLambda = Math.toRadians(lon2 - lon1);
		double a = Math.sin(dPhi / 2) * Math.sin(dPhi / 2)
				+ Math.cos(phi1) * Math.cos(phi2) * Math.sin(dLambda / 2) * Math.sin(dLambda / 2);
		return 6371.0 * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
	}

	private record WeightedObservation(Location location, double distanceKm) {
	}
}
