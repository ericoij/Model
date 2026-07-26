package model;

/**
 * Eastward (u) and northward (v) wind components in metres per second.
 */
public final class WindVector {
	private double u;
	private double v;

	/**
	 * Convert meteorological wind direction ("from", clockwise from north) to
	 * Cartesian components.
	 */
	public WindVector(double speedMs, double directionDeg) {
		if (!Double.isFinite(speedMs) || speedMs < 0 || speedMs > 200) {
			throw new IllegalArgumentException("Invalid wind speed: " + speedMs);
		}
		if (!Double.isFinite(directionDeg) || directionDeg < 0 || directionDeg > 360) {
			throw new IllegalArgumentException("Invalid wind direction: " + directionDeg);
		}
		double radians = Math.toRadians(directionDeg);
		this.u = -speedMs * Math.sin(radians);
		this.v = -speedMs * Math.cos(radians);
	}

	private WindVector(double u, double v, boolean components) {
		if (!Double.isFinite(u) || !Double.isFinite(v)) {
			throw new IllegalArgumentException("Wind components must be finite");
		}
		this.u = u;
		this.v = v;
	}

	public static WindVector fromComponents(double u, double v) {
		return new WindVector(u, v, true);
	}

	public double getU() {
		return u;
	}

	public double getV() {
		return v;
	}

	public double getSpeed() {
		return Math.hypot(u, v);
	}

	public double getDirection() {
		double direction = Math.toDegrees(Math.atan2(-u, -v));
		return direction < 0 ? direction + 360 : direction;
	}

	public void setU(double u) {
		if (!Double.isFinite(u)) throw new IllegalArgumentException("u must be finite");
		this.u = u;
	}

	public void setV(double v) {
		if (!Double.isFinite(v)) throw new IllegalArgumentException("v must be finite");
		this.v = v;
	}

	public WindVector getWindVector() {
		return fromComponents(u, v);
	}

	public void setWindVector(double u, double v) {
		if (!Double.isFinite(u) || !Double.isFinite(v)) {
			throw new IllegalArgumentException("Wind components must be finite");
		}
		this.u = u;
		this.v = v;
	}
}
