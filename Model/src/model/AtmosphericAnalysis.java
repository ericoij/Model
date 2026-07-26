package model;

import java.time.Instant;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class AtmosphericAnalysis {
	private AtmosphericAnalysis() {
	}

	public static AtmosphereState build(List<Location> observations, double south, double north,
			double west, double east, double spacingDeg) {
		Instant cycle = bestCycle(observations);
		List<Location> aligned = observations.stream()
				.filter(location -> cycle == null || cycle.equals(location.getObservedAt()))
				.toList();
		if (aligned.size() < 4) {
			throw new IllegalArgumentException("At least four soundings from one cycle are required");
		}

		Map<Integer, GridState> levels = new LinkedHashMap<>();
		for (int pressure : AtmosphereState.STANDARD_LEVELS_HPA) {
			levels.put(pressure, Analysis.buildLevel(
					aligned, pressure, south, north, west, east, spacingDeg));
		}
		AtmosphereState atmosphere = new AtmosphereState(levels);
		HydrostaticModel.enforceHydrostaticHeights(atmosphere);
		atmosphere.validatePhysical();
		return atmosphere;
	}

	private static Instant bestCycle(List<Location> observations) {
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
}
