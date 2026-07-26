package model;

import java.time.Instant;

/**
 * A rectangular 500 hPa analysis/forecast grid.
 */
public final class GridState {
	private final double south;
	private final double west;
	private final double spacingDeg;
	private final int rows;
	private final int columns;
	private final Instant validTime;
	private final int observationCount;
	private final int pressureHpa;
	final double[][] u;
	final double[][] v;
	final double[][] height;
	final double[][] temperature;
	final double[][] humidity;

	public GridState(double south, double north, double west, double east, double spacingDeg,
			Instant validTime, int observationCount) {
		this(south, north, west, east, spacingDeg, validTime, observationCount, 500);
	}

	public GridState(double south, double north, double west, double east, double spacingDeg,
			Instant validTime, int observationCount, int pressureHpa) {
		if (spacingDeg <= 0 || north <= south || east <= west) {
			throw new IllegalArgumentException("Invalid grid bounds or spacing");
		}
		this.south = south;
		this.west = west;
		this.spacingDeg = spacingDeg;
		this.rows = (int) Math.round((north - south) / spacingDeg) + 1;
		this.columns = (int) Math.round((east - west) / spacingDeg) + 1;
		this.validTime = validTime;
		this.observationCount = observationCount;
		this.pressureHpa = pressureHpa;
		u = new double[rows][columns];
		v = new double[rows][columns];
		height = new double[rows][columns];
		temperature = new double[rows][columns];
		humidity = new double[rows][columns];
	}

	private GridState(GridState source, Instant newValidTime) {
		this(source.south, source.north(), source.west, source.east(), source.spacingDeg,
				newValidTime, source.observationCount, source.pressureHpa);
		copyFields(source, this);
	}

	public GridState copy() {
		return new GridState(this, validTime);
	}

	public GridState copyAt(Instant newValidTime) {
		return new GridState(this, newValidTime);
	}

	private static void copyFields(GridState from, GridState to) {
		for (int row = 0; row < from.rows; row++) {
			System.arraycopy(from.u[row], 0, to.u[row], 0, from.columns);
			System.arraycopy(from.v[row], 0, to.v[row], 0, from.columns);
			System.arraycopy(from.height[row], 0, to.height[row], 0, from.columns);
			System.arraycopy(from.temperature[row], 0, to.temperature[row], 0, from.columns);
			System.arraycopy(from.humidity[row], 0, to.humidity[row], 0, from.columns);
		}
	}

	public int rows() { return rows; }
	public int columns() { return columns; }
	public double south() { return south; }
	public double north() { return south + (rows - 1) * spacingDeg; }
	public double west() { return west; }
	public double east() { return west + (columns - 1) * spacingDeg; }
	public double spacingDeg() { return spacingDeg; }
	public double latitude(int row) { return south + row * spacingDeg; }
	public double longitude(int column) { return west + column * spacingDeg; }
	public Instant validTime() { return validTime; }
	public int observationCount() { return observationCount; }
	public int pressureHpa() { return pressureHpa; }

	public double windSpeed(int row, int column) {
		return Math.hypot(u[row][column], v[row][column]);
	}

	public double maxWindSpeed() {
		double maximum = 0;
		for (int row = 0; row < rows; row++) {
			for (int column = 0; column < columns; column++) {
				maximum = Math.max(maximum, windSpeed(row, column));
			}
		}
		return maximum;
	}

	double sample(double[][] field, double latitude, double longitude) {
		double rowPosition = clamp((latitude - south) / spacingDeg, 0, rows - 1);
		double columnPosition = clamp((longitude - west) / spacingDeg, 0, columns - 1);
		int row0 = (int) Math.floor(rowPosition);
		int column0 = (int) Math.floor(columnPosition);
		int row1 = Math.min(row0 + 1, rows - 1);
		int column1 = Math.min(column0 + 1, columns - 1);
		double rowFraction = rowPosition - row0;
		double columnFraction = columnPosition - column0;
		double lower = lerp(field[row0][column0], field[row0][column1], columnFraction);
		double upper = lerp(field[row1][column0], field[row1][column1], columnFraction);
		return lerp(lower, upper, rowFraction);
	}

	private static double lerp(double start, double end, double fraction) {
		return start + (end - start) * fraction;
	}

	private static double clamp(double value, double minimum, double maximum) {
		return Math.max(minimum, Math.min(maximum, value));
	}

	void restoreBoundaryFrom(GridState boundary) {
		for (int column = 0; column < columns; column++) {
			copyCell(boundary, 0, column);
			copyCell(boundary, rows - 1, column);
		}
		for (int row = 1; row < rows - 1; row++) {
			copyCell(boundary, row, 0);
			copyCell(boundary, row, columns - 1);
		}
	}

	private void copyCell(GridState source, int row, int column) {
		u[row][column] = source.u[row][column];
		v[row][column] = source.v[row][column];
		height[row][column] = source.height[row][column];
		temperature[row][column] = source.temperature[row][column];
		humidity[row][column] = source.humidity[row][column];
	}

	void smoothInterior(double amount) {
		smooth(u, amount);
		smooth(v, amount);
		smooth(height, amount);
		smooth(temperature, amount);
		smooth(humidity, amount);
	}

	private void smooth(double[][] field, double amount) {
		double[][] original = new double[rows][columns];
		for (int row = 0; row < rows; row++) {
			System.arraycopy(field[row], 0, original[row], 0, columns);
		}
		for (int row = 1; row < rows - 1; row++) {
			for (int column = 1; column < columns - 1; column++) {
				double neighborMean = (original[row - 1][column] + original[row + 1][column]
						+ original[row][column - 1] + original[row][column + 1]) * 0.25;
				field[row][column] = lerp(original[row][column], neighborMean, amount);
			}
		}
	}

	public void validatePhysical() {
		for (int row = 0; row < rows; row++) {
			for (int column = 0; column < columns; column++) {
				requireFinite(u[row][column], "u", row, column);
				requireFinite(v[row][column], "v", row, column);
				requireFinite(height[row][column], "height", row, column);
				requireFinite(temperature[row][column], "temperature", row, column);
				requireFinite(humidity[row][column], "humidity", row, column);
				double speed = windSpeed(row, column);
				if (speed > 200) {
					throw new IllegalStateException("Forecast wind exceeded 200 m/s at " + row + "," + column);
				}
				if (height[row][column] < -500 || height[row][column] > 15000) {
					throw new IllegalStateException("Invalid pressure-level height at " + row + "," + column);
				}
				if (temperature[row][column] < 180 || temperature[row][column] > 330) {
					throw new IllegalStateException("Invalid 500 hPa temperature at " + row + "," + column);
				}
			}
		}
	}

	private static void requireFinite(double value, String field, int row, int column) {
		if (!Double.isFinite(value)) {
			throw new IllegalStateException(field + " is not finite at " + row + "," + column);
		}
	}
}
