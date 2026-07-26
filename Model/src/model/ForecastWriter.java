package model;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

public final class ForecastWriter {
	private ForecastWriter() {
	}

	public static void writeCsv(GridState grid, Path path) {
		try {
			Files.createDirectories(path.toAbsolutePath().getParent());
			try (BufferedWriter writer = Files.newBufferedWriter(path)) {
				writer.write("valid_time,latitude,longitude,pressure_hpa,height_m,"
						+ "wind_speed_ms,wind_direction_deg,temperature_c,relative_humidity_pct,u_ms,v_ms");
				writer.newLine();
				for (int row = 0; row < grid.rows(); row++) {
					for (int column = 0; column < grid.columns(); column++) {
						WindVector wind = WindVector.fromComponents(
								grid.u[row][column], grid.v[row][column]);
						writer.write(String.format(Locale.ROOT,
								"%s,%.3f,%.3f,500,%.3f,%.3f,%.3f,%.3f,%.3f,%.3f,%.3f",
								grid.validTime() == null ? "" : grid.validTime(),
								grid.latitude(row), grid.longitude(column),
								grid.height[row][column], wind.getSpeed(), wind.getDirection(),
								grid.temperature[row][column] - 273.15,
								grid.humidity[row][column],
								wind.getU(), wind.getV()));
						writer.newLine();
					}
				}
			}
		} catch (IOException ex) {
			throw new IllegalStateException("Unable to write forecast to " + path.toAbsolutePath(), ex);
		}
	}
}
