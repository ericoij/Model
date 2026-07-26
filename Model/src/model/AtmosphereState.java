package model;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Multi-level pressure-coordinate atmospheric state.
 */
public final class AtmosphereState {
	public static final List<Integer> STANDARD_LEVELS_HPA = List.of(850, 700, 500, 300, 200);
	private final LinkedHashMap<Integer, GridState> levels = new LinkedHashMap<>();
	private final LinkedHashMap<Integer, double[][]> omega = new LinkedHashMap<>();

	public AtmosphereState(Map<Integer, GridState> source) {
		for (int pressure : STANDARD_LEVELS_HPA) {
			GridState grid = source.get(pressure);
			if (grid == null) throw new IllegalArgumentException("Missing " + pressure + " hPa grid");
			levels.put(pressure, grid);
			omega.put(pressure, new double[grid.rows()][grid.columns()]);
		}
		validateGeometry();
	}

	public GridState level(int pressureHpa) {
		GridState grid = levels.get(pressureHpa);
		if (grid == null) throw new IllegalArgumentException("Unsupported pressure level: " + pressureHpa);
		return grid;
	}

	public double[][] omega(int pressureHpa) {
		return omega.get(pressureHpa);
	}

	void setOmega(int pressureHpa, double[][] values) {
		omega.put(pressureHpa, values);
	}

	public Instant validTime() {
		return level(500).validTime();
	}

	public AtmosphereState copy() {
		Map<Integer, GridState> copies = new LinkedHashMap<>();
		for (var entry : levels.entrySet()) copies.put(entry.getKey(), entry.getValue().copy());
		AtmosphereState copy = new AtmosphereState(copies);
		for (var entry : omega.entrySet()) {
			double[][] source = entry.getValue();
			double[][] target = new double[source.length][source[0].length];
			for (int row = 0; row < source.length; row++) {
				System.arraycopy(source[row], 0, target[row], 0, source[row].length);
			}
			copy.setOmega(entry.getKey(), target);
		}
		return copy;
	}

	public double maxWindSpeed() {
		return levels.values().stream().mapToDouble(GridState::maxWindSpeed).max().orElse(0);
	}

	public double maxAbsOmega() {
		double maximum = 0;
		for (double[][] field : omega.values()) {
			for (double[] row : field) {
				for (double value : row) maximum = Math.max(maximum, Math.abs(value));
			}
		}
		return maximum;
	}

	public void validatePhysical() {
		for (GridState grid : levels.values()) grid.validatePhysical();
		if (maxAbsOmega() > 5.01) {
			throw new IllegalStateException("Vertical pressure velocity exceeded 5 Pa/s");
		}
	}

	private void validateGeometry() {
		GridState reference = level(500);
		for (GridState grid : levels.values()) {
			if (grid.rows() != reference.rows() || grid.columns() != reference.columns()
					|| grid.spacingDeg() != reference.spacingDeg()
					|| grid.south() != reference.south() || grid.west() != reference.west()) {
				throw new IllegalArgumentException("All pressure grids must share one geometry");
			}
		}
	}
}
