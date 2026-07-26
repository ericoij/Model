package model;

import java.util.List;

/**
 * Small programmatic facade around analysis and dynamics.
 */
public final class Model {
	public GridState forecast(List<Location> observations, int hours) {
		GridState analysis = Analysis.build(observations, 30, 50, -120, -80, 0.5);
		return new Physics().forecastHours(analysis, hours);
	}
}
