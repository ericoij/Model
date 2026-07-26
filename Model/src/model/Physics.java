package model;

import java.time.Instant;

/**
 * Stable educational single-level dynamics for the 500 hPa surface.
 *
 * <p>Horizontal fields are transported with semi-Lagrangian advection. Wind is
 * then advanced with the pressure-gradient and Coriolis terms; the Coriolis
 * rotation is integrated analytically instead of with unstable forward Euler.
 * This is deliberately a limited barotropic model, not an operational NWP
 * system.</p>
 */
public final class Physics {
	public static final double DEFAULT_TIME_STEP_SECONDS = 300;
	private static final double EARTH_RADIUS_M = 6_371_000;
	private static final double EARTH_ROTATION_RATE = 7.2921159e-5;
	private static final double GRAVITY = 9.80665;

	public GridState advance(GridState initial, int steps, double timeStepSeconds) {
		return advance(initial, initial, steps, timeStepSeconds);
	}

	public GridState advance(GridState initial, GridState fixedBoundary, int steps, double timeStepSeconds) {
		if (steps < 0 || timeStepSeconds <= 0 || !Double.isFinite(timeStepSeconds)) {
			throw new IllegalArgumentException("Invalid forecast step configuration");
		}
		GridState boundary = fixedBoundary.copy();
		GridState current = initial.copy();
		for (int step = 0; step < steps; step++) {
			current = step(current, boundary, timeStepSeconds);
		}
		return current;
	}

	public GridState forecastHours(GridState initial, int hours) {
		if (hours < 0 || hours > 48) throw new IllegalArgumentException("Forecast hours must be 0..48");
		int steps = (int) Math.round(hours * 3600 / DEFAULT_TIME_STEP_SECONDS);
		return advance(initial, steps, DEFAULT_TIME_STEP_SECONDS);
	}

	private GridState step(GridState source, GridState boundary, double dt) {
		Instant nextTime = source.validTime() == null
				? null : source.validTime().plusMillis(Math.round(dt * 1000));
		GridState transported = source.copyAt(nextTime);

		for (int row = 0; row < source.rows(); row++) {
			double latitude = source.latitude(row);
			double cosLatitude = Math.max(0.2, Math.cos(Math.toRadians(latitude)));
			for (int column = 0; column < source.columns(); column++) {
				double longitude = source.longitude(column);
				double departureLatitude = latitude
						- Math.toDegrees(source.v[row][column] * dt / EARTH_RADIUS_M);
				double departureLongitude = longitude
						- Math.toDegrees(source.u[row][column] * dt / (EARTH_RADIUS_M * cosLatitude));
				transported.u[row][column] = source.sample(source.u, departureLatitude, departureLongitude);
				transported.v[row][column] = source.sample(source.v, departureLatitude, departureLongitude);
				transported.height[row][column] = source.sample(source.height, departureLatitude, departureLongitude);
				transported.temperature[row][column] =
						source.sample(source.temperature, departureLatitude, departureLongitude);
				transported.humidity[row][column] =
						source.sample(source.humidity, departureLatitude, departureLongitude);
			}
		}

		GridState next = transported.copy();
		double dy = EARTH_RADIUS_M * Math.toRadians(source.spacingDeg());
		for (int row = 1; row < source.rows() - 1; row++) {
			double latitude = source.latitude(row);
			double dx = dy * Math.cos(Math.toRadians(latitude));
			double coriolis = 2 * EARTH_ROTATION_RATE * Math.sin(Math.toRadians(latitude));
			for (int column = 1; column < source.columns() - 1; column++) {
				double dzdx = (transported.height[row][column + 1]
						- transported.height[row][column - 1]) / (2 * dx);
				double dzdy = (transported.height[row + 1][column]
						- transported.height[row - 1][column]) / (2 * dy);
				double accelerationX = -GRAVITY * dzdx;
				double accelerationY = -GRAVITY * dzdy;
				rotateAndForce(next, transported, row, column, coriolis,
						accelerationX, accelerationY, dt);
			}
		}

		next.smoothInterior(0.015);
		next.restoreBoundaryFrom(boundary);
		next.validatePhysical();
		return next;
	}

	private static void rotateAndForce(GridState output, GridState input, int row, int column,
			double coriolis, double accelerationX, double accelerationY, double dt) {
		double oldU = input.u[row][column];
		double oldV = input.v[row][column];
		if (Math.abs(coriolis) < 1e-12) {
			output.u[row][column] = oldU + accelerationX * dt;
			output.v[row][column] = oldV + accelerationY * dt;
			return;
		}
		double angle = coriolis * dt;
		double sine = Math.sin(angle);
		double cosine = Math.cos(angle);
		output.u[row][column] = oldU * cosine + oldV * sine
				+ accelerationX * sine / coriolis
				+ accelerationY * (1 - cosine) / coriolis;
		output.v[row][column] = -oldU * sine + oldV * cosine
				+ accelerationX * (cosine - 1) / coriolis
				+ accelerationY * sine / coriolis;
	}
}
