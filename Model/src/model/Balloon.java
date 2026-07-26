package model;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

/**
 * Reader for NOAA IGRA v2/v2.2 fixed-width sounding data.
 */
public class Balloon {
	private static final double MISSING_LIMIT = -8888;

	private final Path inputPath;
	private Location activeLocation;
	private final List<Location> observations = new ArrayList<>();

	public Balloon() {
		this(Path.of("balloon.d"));
	}

	public Balloon(Path inputPath) {
		this.inputPath = inputPath;
	}

	public void storeValues() {
		storeValues(inputPath);
	}

	/**
	 * Legacy entry point. Valid observations are also made available through
	 * {@link #getObservations()}.
	 */
	public void storeValues(Path path) {
		observations.clear();
		try (var reader = Files.newBufferedReader(path)) {
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.startsWith("#")) {
					finishSounding();
					activeLocation = parseHeader(line);
				} else if (activeLocation != null && !line.isEmpty() && line.charAt(0) == '1') {
					Level level = parseLevelRecord(line);
					if (level != null) storeLevel(activeLocation, level);
				}
			}
			finishSounding();
		} catch (IOException ex) {
			throw new IllegalStateException("Unable to read balloon data from " + path.toAbsolutePath(), ex);
		}
		if (observations.isEmpty()) {
			throw new IllegalStateException("No valid sounding headers found in " + path.toAbsolutePath());
		}
		System.out.println("Loaded " + observations.size() + " sounding observations from " + path.toAbsolutePath());
	}

	public List<Location> getObservations() {
		return List.copyOf(observations);
	}

	private void finishSounding() {
		if (activeLocation == null) return;
		if (activeLocation.getFiveHundred() != null && activeLocation.getFiveHundred().isForecastUsable()) {
			observations.add(activeLocation);
		}
		activeLocation = null;
	}

	static Location parseHeader(String line) {
		if (line.length() < 71) throw new IllegalArgumentException("Malformed IGRA header: " + line);
		Location location = new Location(numberAt(line, 55, 62) / 10000.0,
				numberAt(line, 63, 71) / 10000.0);
		location.setStationId(line.substring(1, 12).trim());
		try {
			int year = (int) numberAt(line, 13, 17);
			int month = (int) numberAt(line, 18, 20);
			int day = (int) numberAt(line, 21, 23);
			int hour = (int) numberAt(line, 24, 26);
			location.setObservedAt(LocalDateTime.of(year, month, day, hour, 0).toInstant(ZoneOffset.UTC));
		} catch (DateTimeException | NumberFormatException ignored) {
			location.setObservedAt(null);
		}
		return location;
	}

	static Level parseLevelRecord(String line) {
		if (line.length() < 51) return null;
		Double pressure = field(line, 9, 15, 1.0);
		Double height = field(line, 16, 21, 1.0);
		Double temperatureC = field(line, 22, 27, 0.1);
		Double humidity = field(line, 28, 33, 0.1);
		Double dewPointDepression = field(line, 34, 39, 0.1);
		Double direction = field(line, 40, 45, 1.0);
		Double speed = field(line, 46, 51, 0.1);

		if (pressure == null || height == null || temperatureC == null
				|| direction == null || speed == null) return null;
		if (speed < 0 || speed > 200 || direction < 0 || direction > 360) return null;
		double temperatureK = temperatureC + 273.15;
		Level level = new Level(pressure, height, speed, direction,
				dewPointDepression, temperatureK, humidity);
		return level.isForecastUsable() ? level : null;
	}

	private static Double field(String line, int start, int end, double scale) {
		try {
			double raw = numberAt(line, start, end);
			return raw <= MISSING_LIMIT ? null : raw * scale;
		} catch (NumberFormatException ex) {
			return null;
		}
	}

	private static double numberAt(String line, int start, int end) {
		return Double.parseDouble(line.substring(start, end).trim());
	}

	private static void storeLevel(Location location, Level level) {
		double pressure = level.getPressure();
		if (pressure == 100000) location.setOneThousand(level);
		else if (pressure == 92500) location.setNineTwentyFive(level);
		else if (pressure == 85000) location.setEightFifty(level);
		else if (pressure == 70000) location.setSevenHundred(level);
		else if (pressure == 50000) location.setFiveHundred(level);
		else if (pressure == 30000) location.setThreeHundred(level);
		else if (pressure == 25000) location.setTwoFifty(level);
		else if (pressure == 20000) location.setTwoHundred(level);
	}

	public void storeLevel(Level level) {
		if (activeLocation != null && level != null) storeLevel(activeLocation, level);
	}
}
