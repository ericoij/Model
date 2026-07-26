package model;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

public final class ModelTests {
	private static int assertions;

	private ModelTests() {
	}

	public static void main(String[] args) {
		testIgraUnits();
		testMissingWindRejected();
		testWindRoundTrip();
		testTimeAlignedAnalysis();
		testCalmFieldStaysCalm();
		testForecastActuallyAdvances();
		testLatestSoundingExtraction();
		testEndToEndFileForecast();
		System.out.println("Passed " + assertions + " model assertions.");
	}

	private static void testIgraUnits() {
		String line = "10  1456  50000  5913B  -78B  775    32   186   180 ";
		Level level = Balloon.parseLevelRecord(line);
		check(level != null, "valid IGRA level should parse");
		near(level.getPressure(), 50000, 0, "pressure");
		near(level.getGeoHeight(), 5913, 0, "height");
		near(level.getTemperature(), 265.35, 1e-9, "temperature");
		near(level.getRelativeHumidity(), 77.5, 1e-9, "humidity");
		near(level.getWindSpeed(), 18, 1e-9, "wind speed");
		near(level.windVector().getU(), 1.8815, 0.01, "u wind");
		near(level.windVector().getV(), 17.9014, 0.01, "v wind");
	}

	private static void testMissingWindRejected() {
		String line = "10  1456  50000  5913B  -78B  775    32   186 -9999 ";
		check(Balloon.parseLevelRecord(line) == null, "missing wind must not become a vector");
		boolean rejected = false;
		try {
			new WindVector(-9999, 180);
		} catch (IllegalArgumentException expected) {
			rejected = true;
		}
		check(rejected, "negative sentinel must be rejected");
	}

	private static void testWindRoundTrip() {
		WindVector wind = new WindVector(25, 270);
		near(wind.getU(), 25, 1e-10, "270-degree u");
		near(wind.getV(), 0, 1e-10, "270-degree v");
		near(wind.getSpeed(), 25, 1e-10, "round-trip speed");
		near(wind.getDirection(), 270, 1e-10, "round-trip direction");
	}

	private static void testTimeAlignedAnalysis() {
		Instant cycle = Instant.parse("2026-07-25T12:00:00Z");
		List<Location> observations = List.of(
				observation(31, -99, cycle, 15, 250, 5800),
				observation(31, -91, cycle, 18, 260, 5820),
				observation(39, -99, cycle, 20, 270, 5760),
				observation(39, -91, cycle, 22, 280, 5780),
				observation(35, -95, cycle.minusSeconds(86400), 190, 90, 5000));
		GridState analysis = Analysis.build(observations, 30, 40, -100, -90, 1);
		check(analysis.observationCount() == 4, "analysis should use one observation cycle");
		check(analysis.maxWindSpeed() < 30, "analysis should remain physically plausible");
		analysis.validatePhysical();
		GridState forecast = new Physics().forecastHours(analysis, 9);
		check(forecast.maxWindSpeed() < 100, "nine-hour forecast should remain bounded");
		forecast.validatePhysical();
	}

	private static void testCalmFieldStaysCalm() {
		GridState calm = syntheticGrid(0);
		GridState forecast = new Physics().forecastHours(calm, 3);
		near(forecast.maxWindSpeed(), 0, 1e-10, "calm flat field");
		forecast.validatePhysical();
	}

	private static void testForecastActuallyAdvances() {
		GridState moving = syntheticGrid(18);
		for (int row = 0; row < moving.rows(); row++) {
			for (int column = 0; column < moving.columns(); column++) {
				moving.temperature[row][column] += column * 0.5;
			}
		}
		Physics physics = new Physics();
		GridState hourOne = physics.forecastHours(moving, 1);
		GridState hourTwo = physics.forecastHours(moving, 2);
		double centerOne = hourOne.temperature[hourOne.rows() / 2][hourOne.columns() / 2];
		double centerTwo = hourTwo.temperature[hourTwo.rows() / 2][hourTwo.columns() / 2];
		check(Math.abs(centerOne - centerTwo) > 1e-5, "successive forecast hours must differ");
		check(hourTwo.validTime().equals(moving.validTime().plusSeconds(7200)),
				"forecast valid time should advance");
		hourTwo.validatePhysical();
	}

	private static void testEndToEndFileForecast() {
		Path directory = null;
		try {
			directory = Files.createTempDirectory("weather-model-test-");
			Path input = directory.resolve("sample.d");
			StringBuilder data = new StringBuilder();
			data.append(header("USM00000001", 31, -99)).append('\n')
					.append(multiLevels(0, 240, 140))
					.append(header("USM00000002", 31, -91)).append('\n')
					.append(multiLevels(20, 250, 160))
					.append(header("USM00000003", 39, -99)).append('\n')
					.append(multiLevels(-20, 260, 180))
					.append(header("USM00000004", 39, -91)).append('\n')
					.append(multiLevels(10, 270, 200));
			Files.writeString(input, data);

			Balloon reader = new Balloon(input);
			reader.storeValues();
			check(reader.getObservations().size() == 4, "file reader should retain four valid stations");
			AtmosphereState analysis = AtmosphericAnalysis.build(
					reader.getObservations(), 30, 40, -100, -90, 1);
			AtmosphereState forecast = new HydrostaticModel().forecastHours(analysis, 2);
			Path output = directory.resolve("forecast.csv");
			ForecastWriter.writeAtmosphereCsv(forecast, output);
			check(Files.size(output) > 1000, "forecast writer should create a populated CSV");
			GridState reference = forecast.level(500);
			check(Files.readAllLines(output).size()
					== reference.rows() * reference.columns() * 5 + 1,
					"forecast CSV should contain every grid cell and pressure level");
			check(forecast.validTime().equals(Instant.parse("2026-07-25T14:00:00Z")),
					"multi-level forecast time should advance");
			check(forecast.maxWindSpeed() < 100, "multi-level forecast winds should remain bounded");
			check(forecast.maxAbsOmega() > 0.001 && forecast.maxAbsOmega() <= 5,
					"diagnosed omega should be nonzero and bounded");
			for (int row = 0; row < reference.rows(); row++) {
				for (int column = 0; column < reference.columns(); column++) {
					near(forecast.omega(850)[row][column], 0, 1e-9,
							"lower pressure boundary omega");
					near(forecast.omega(200)[row][column], 0, 1e-9,
							"upper pressure boundary omega");
					check(forecast.level(200).height[row][column]
							> forecast.level(850).height[row][column],
							"hydrostatic height must increase upward");
				}
			}
		} catch (IOException ex) {
			throw new AssertionError("End-to-end file test failed", ex);
		} finally {
			if (directory != null) deleteTemporaryTree(directory);
		}
	}

	private static void testLatestSoundingExtraction() {
		String older = headerAt("USM00000001", 31, -99, "00") + "\n"
				+ level(50000, 5800, -80, 500, 240, 140) + "\n";
		String newer = headerAt("USM00000001", 31, -99, "12") + "\n"
				+ level(50000, 5820, -70, 550, 250, 160) + "\n";
		String extracted = NoaaIngest.extractLatestSounding(older + newer);
		check(extracted.contains(" 12 "), "NOAA ingest should select the latest sounding");
		check(!extracted.contains(" 00 "), "NOAA ingest should discard older soundings");
	}

	private static String header(String stationId, double latitude, double longitude) {
		return headerAt(stationId, latitude, longitude, "12");
	}

	private static String headerAt(String stationId, double latitude, double longitude, String hour) {
		char[] line = " ".repeat(80).toCharArray();
		line[0] = '#';
		put(line, 1, String.format("%-11s", stationId));
		put(line, 13, "2026");
		put(line, 18, "07");
		put(line, 21, "25");
		put(line, 24, hour);
		put(line, 55, String.format("%7d", Math.round(latitude * 10000)));
		put(line, 63, String.format("%8d", Math.round(longitude * 10000)));
		return new String(line);
	}

	private static String multiLevels(int heightOffset, int directionDeg, int speedTenthsMs) {
		return level(85000, 1500 + heightOffset, 120, 550, directionDeg, speedTenthsMs) + "\n"
				+ level(70000, 3000 + heightOffset, 20, 500, directionDeg, speedTenthsMs) + "\n"
				+ level(50000, 5800 + heightOffset, -80, 450, directionDeg, speedTenthsMs) + "\n"
				+ level(30000, 9000 + heightOffset, -350, 350, directionDeg, speedTenthsMs) + "\n"
				+ level(20000, 11800 + heightOffset, -550, 250, directionDeg, speedTenthsMs) + "\n";
	}

	private static String level(int pressurePa, int height, int temperatureTenthsC, int humidityTenthsPct,
			int directionDeg, int speedTenthsMs) {
		char[] line = " ".repeat(55).toCharArray();
		line[0] = '1';
		put(line, 9, String.format("%6d", pressurePa));
		put(line, 16, String.format("%5d", height));
		put(line, 22, String.format("%5d", temperatureTenthsC));
		put(line, 28, String.format("%5d", humidityTenthsPct));
		put(line, 34, String.format("%5d", 30));
		put(line, 40, String.format("%5d", directionDeg));
		put(line, 46, String.format("%5d", speedTenthsMs));
		return new String(line);
	}

	private static void put(char[] destination, int offset, String value) {
		for (int index = 0; index < value.length(); index++) {
			destination[offset + index] = value.charAt(index);
		}
	}

	private static void deleteTemporaryTree(Path directory) {
		try (var paths = Files.walk(directory)) {
			paths.sorted((left, right) -> right.compareTo(left)).forEach(path -> {
				try {
					Files.deleteIfExists(path);
				} catch (IOException ignored) {
				}
			});
		} catch (IOException ignored) {
		}
	}

	private static GridState syntheticGrid(double windSpeed) {
		GridState grid = new GridState(32, 38, -100, -94, 1,
				Instant.parse("2026-07-25T12:00:00Z"), 4);
		for (int row = 0; row < grid.rows(); row++) {
			for (int column = 0; column < grid.columns(); column++) {
				grid.u[row][column] = windSpeed;
				grid.v[row][column] = 0;
				grid.height[row][column] = 5700;
				grid.temperature[row][column] = 260;
				grid.humidity[row][column] = 45;
			}
		}
		return grid;
	}

	private static Location observation(double latitude, double longitude, Instant time,
			double speed, double direction, double height) {
		Location location = new Location(latitude, longitude);
		location.setObservedAt(time);
		location.setFiveHundred(new Level(50000.0, height, speed, direction,
				10.0, 260.0, 45.0));
		return location;
	}

	private static void near(double actual, double expected, double tolerance, String label) {
		check(Math.abs(actual - expected) <= tolerance,
				label + " expected " + expected + " but got " + actual);
	}

	private static void check(boolean condition, String message) {
		assertions++;
		if (!condition) throw new AssertionError(message);
	}
}
