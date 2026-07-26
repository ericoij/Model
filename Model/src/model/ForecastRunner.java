package model;

import java.nio.file.Path;
import java.util.List;

public final class ForecastRunner {
	private static final double SOUTH = 30;
	private static final double NORTH = 50;
	private static final double WEST = -120;
	private static final double EAST = -80;
	private static final double SPACING_DEGREES = 0.5;

	private ForecastRunner() {
	}

	public static void main(String[] args) {
		if (args.length < 1 || args.length > 3) {
			System.err.println("Usage: java model.ForecastRunner <igra-input> [output-directory] [hours]");
			System.exit(2);
		}
		Path input = Path.of(args[0]);
		Path output = args.length >= 2 ? Path.of(args[1]) : Path.of("forecast-output");
		int hours = args.length >= 3 ? Integer.parseInt(args[2]) : 9;
		if (hours < 1 || hours > 48) throw new IllegalArgumentException("hours must be 1..48");

		Balloon reader = new Balloon(input);
		reader.storeValues();
		List<Location> observations = reader.getObservations();
		GridState analysis = Analysis.build(observations, SOUTH, NORTH, WEST, EAST, SPACING_DEGREES);
		ForecastWriter.writeCsv(analysis, output.resolve("analysis-000.csv"));

		Physics physics = new Physics();
		for (int hour = 1; hour <= hours; hour++) {
			GridState forecast = physics.forecastHours(analysis, hour);
			ForecastWriter.writeCsv(forecast,
					output.resolve(String.format("forecast-%03d.csv", hour)));
			System.out.printf("hour %02d: max wind %.1f m/s%n", hour, forecast.maxWindSpeed());
		}
		System.out.printf("Wrote %d-hour 500 hPa forecast from %d time-aligned observations to %s%n",
				hours, analysis.observationCount(), output.toAbsolutePath());
	}
}
