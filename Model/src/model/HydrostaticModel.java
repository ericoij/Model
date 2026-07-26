package model;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Five-level hydrostatic pressure-coordinate model.
 */
public final class HydrostaticModel {
	private static final double EARTH_RADIUS_M = 6_371_000;
	private static final double DRY_AIR_GAS_CONSTANT = 287.05;
	private static final double GRAVITY = 9.80665;
	private static final double KAPPA = 287.05 / 1004.0;
	private static final double MAX_ABS_OMEGA_PA_S = 5;
	private static final double DT = Physics.DEFAULT_TIME_STEP_SECONDS;

	public AtmosphereState forecastHours(AtmosphereState analysis, int hours) {
		if (hours < 0 || hours > 48) throw new IllegalArgumentException("Forecast hours must be 0..48");
		AtmosphereState current = analysis.copy();
		AtmosphereState boundary = analysis.copy();
		int steps = (int) Math.round(hours * 3600 / DT);
		for (int step = 0; step < steps; step++) current = step(current, boundary);
		return current;
	}

	private AtmosphereState step(AtmosphereState source, AtmosphereState boundary) {
		Physics horizontalDynamics = new Physics();
		Map<Integer, GridState> transported = new LinkedHashMap<>();
		for (int pressure : AtmosphereState.STANDARD_LEVELS_HPA) {
			transported.put(pressure, horizontalDynamics.advance(
					source.level(pressure), boundary.level(pressure), 1, DT));
		}
		AtmosphereState next = new AtmosphereState(transported);
		computeOmega(next);
		applyVerticalTransport(next);
		enforceHydrostaticHeights(next);
		for (int pressure : AtmosphereState.STANDARD_LEVELS_HPA) {
			next.level(pressure).restoreBoundaryFrom(boundary.level(pressure));
		}
		next.validatePhysical();
		return next;
	}

	private static void computeOmega(AtmosphereState atmosphere) {
		List<Integer> levels = AtmosphereState.STANDARD_LEVELS_HPA;
		GridState reference = atmosphere.level(500);
		int count = levels.size();
		double[][][] divergence = new double[count][reference.rows()][reference.columns()];
		for (int index = 0; index < count; index++) {
			GridState grid = atmosphere.level(levels.get(index));
			double dy = EARTH_RADIUS_M * Math.toRadians(grid.spacingDeg());
			for (int row = 1; row < grid.rows() - 1; row++) {
				double dx = dy * Math.cos(Math.toRadians(grid.latitude(row)));
				for (int column = 1; column < grid.columns() - 1; column++) {
					double dudx = (grid.u[row][column + 1] - grid.u[row][column - 1]) / (2 * dx);
					double dvdy = (grid.v[row + 1][column] - grid.v[row - 1][column]) / (2 * dy);
					divergence[index][row][column] = dudx + dvdy;
				}
			}
		}

		double[][][] omega = new double[count][reference.rows()][reference.columns()];
		for (int index = count - 2; index >= 0; index--) {
			double deltaPressure = (levels.get(index) - levels.get(index + 1)) * 100.0;
			for (int row = 1; row < reference.rows() - 1; row++) {
				for (int column = 1; column < reference.columns() - 1; column++) {
					omega[index][row][column] = omega[index + 1][row][column]
							- 0.5 * (divergence[index][row][column]
							+ divergence[index + 1][row][column]) * deltaPressure;
				}
			}
		}

		double topPressure = levels.get(count - 1) * 100.0;
		double bottomPressure = levels.get(0) * 100.0;
		for (int index = 0; index < count; index++) {
			double fraction = (levels.get(index) * 100.0 - topPressure)
					/ (bottomPressure - topPressure);
			for (int row = 0; row < reference.rows(); row++) {
				for (int column = 0; column < reference.columns(); column++) {
					double corrected = omega[index][row][column]
							- omega[0][row][column] * fraction;
					omega[index][row][column] = clamp(corrected,
							-MAX_ABS_OMEGA_PA_S, MAX_ABS_OMEGA_PA_S);
				}
			}
			atmosphere.setOmega(levels.get(index), omega[index]);
		}
	}

	private static void applyVerticalTransport(AtmosphereState atmosphere) {
		List<Integer> levels = AtmosphereState.STANDARD_LEVELS_HPA;
		AtmosphereState original = atmosphere.copy();
		for (int index = 1; index < levels.size() - 1; index++) {
			int pressure = levels.get(index);
			GridState grid = atmosphere.level(pressure);
			GridState below = original.level(levels.get(index - 1));
			GridState above = original.level(levels.get(index + 1));
			double deltaPressure = (levels.get(index - 1) - levels.get(index + 1)) * 100.0;
			for (int row = 1; row < grid.rows() - 1; row++) {
				for (int column = 1; column < grid.columns() - 1; column++) {
					double omega = atmosphere.omega(pressure)[row][column];
					grid.u[row][column] += -omega
							* (below.u[row][column] - above.u[row][column]) / deltaPressure * DT;
					grid.v[row][column] += -omega
							* (below.v[row][column] - above.v[row][column]) / deltaPressure * DT;
					double verticalTemperatureAdvection = -omega
							* (below.temperature[row][column] - above.temperature[row][column])
							/ deltaPressure;
					double adiabatic = KAPPA * original.level(pressure).temperature[row][column]
							* omega / (pressure * 100.0);
					grid.temperature[row][column] +=
							(verticalTemperatureAdvection + adiabatic) * DT;
					grid.humidity[row][column] = clamp(grid.humidity[row][column]
							- omega * (below.humidity[row][column] - above.humidity[row][column])
							/ deltaPressure * DT, 0, 100);
				}
			}
			grid.smoothInterior(0.01);
		}
	}

	static void enforceHydrostaticHeights(AtmosphereState atmosphere) {
		List<Integer> levels = AtmosphereState.STANDARD_LEVELS_HPA;
		for (int index = 1; index < levels.size(); index++) {
			int lowerPressure = levels.get(index - 1);
			int upperPressure = levels.get(index);
			GridState lower = atmosphere.level(lowerPressure);
			GridState upper = atmosphere.level(upperPressure);
			for (int row = 0; row < lower.rows(); row++) {
				for (int column = 0; column < lower.columns(); column++) {
					double meanVirtualTemperature = 0.5 * (
							virtualTemperature(lower.temperature[row][column],
									lower.humidity[row][column], lowerPressure)
							+ virtualTemperature(upper.temperature[row][column],
									upper.humidity[row][column], upperPressure));
					upper.height[row][column] = lower.height[row][column]
							+ DRY_AIR_GAS_CONSTANT * meanVirtualTemperature / GRAVITY
							* Math.log((double) lowerPressure / upperPressure);
				}
			}
		}
	}

	private static double virtualTemperature(double temperatureK, double relativeHumidity, int pressureHpa) {
		double temperatureC = temperatureK - 273.15;
		double saturationVaporPressure = 6.112
				* Math.exp(17.67 * temperatureC / (temperatureC + 243.5));
		double vaporPressure = clamp(relativeHumidity, 0, 100) / 100.0
				* Math.min(saturationVaporPressure, pressureHpa * 0.95);
		double mixingRatio = 0.622 * vaporPressure / Math.max(1, pressureHpa - vaporPressure);
		return temperatureK * (1 + 0.61 * mixingRatio);
	}

	private static double clamp(double value, double minimum, double maximum) {
		return Math.max(minimum, Math.min(maximum, value));
	}
}
