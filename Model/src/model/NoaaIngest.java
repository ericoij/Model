package model;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Downloads a compact, current multi-station IGRA input directly from NOAA.
 */
public final class NoaaIngest {
	private static final String DIRECTORY =
			"https://www.ncei.noaa.gov/pub/data/igra/data/data-y2d/";
	private static final List<String> STATIONS = List.of(
			"USM00072786", "USM00072489", "USM00072381", "USM00072293",
			"USM00072681", "USM00072572", "USM00072672", "USM00072768",
			"USM00072365", "USM00072662", "USM00072562", "USM00072357",
			"USM00072249", "USM00072649", "USM00072645", "USM00072230",
			"USM00072206", "USM00072318", "USM00072520");

	private NoaaIngest() {
	}

	public static void main(String[] args) {
		Path output = args.length == 0 ? Path.of("latest-igra.d") : Path.of(args[0]);
		HttpClient client = HttpClient.newBuilder()
				.connectTimeout(Duration.ofSeconds(20))
				.followRedirects(HttpClient.Redirect.NORMAL)
				.build();
		String index = getText(client, DIRECTORY);
		List<String> soundings = new ArrayList<>();
		for (String station : STATIONS) {
			String archive = findArchive(index, station);
			byte[] zip = getBytes(client, DIRECTORY + archive);
			String stationText = unzipText(zip);
			String latest = extractLatestSounding(stationText);
			if (latest != null) {
				soundings.add(latest);
				System.out.println("Loaded " + station);
			}
		}
		if (soundings.size() < 10) {
			throw new IllegalStateException("Only " + soundings.size() + " current soundings were available");
		}
		try {
			Files.writeString(output, String.join(System.lineSeparator(), soundings)
					+ System.lineSeparator());
		} catch (IOException ex) {
			throw new IllegalStateException("Unable to write " + output.toAbsolutePath(), ex);
		}
		System.out.println("Wrote " + soundings.size() + " latest NOAA soundings to " + output.toAbsolutePath());
	}

	static String extractLatestSounding(String text) {
		String latestBlock = null;
		Instant latestTime = null;
		StringBuilder current = null;
		Instant currentTime = null;
		for (String line : text.split("\\R")) {
			if (line.startsWith("#")) {
				if (current != null && currentTime != null
						&& (latestTime == null || currentTime.isAfter(latestTime))) {
					latestBlock = current.toString();
					latestTime = currentTime;
				}
				current = new StringBuilder(line).append('\n');
				currentTime = Balloon.parseHeader(line).getObservedAt();
			} else if (current != null && !line.isEmpty() && line.charAt(0) == '1') {
				current.append(line).append('\n');
			}
		}
		if (current != null && currentTime != null
				&& (latestTime == null || currentTime.isAfter(latestTime))) {
			latestBlock = current.toString();
		}
		return latestBlock;
	}

	private static String findArchive(String index, String station) {
		Matcher matcher = Pattern.compile(Pattern.quote(station)
				+ "-data-[^\"<>]+?\\.txt\\.zip", Pattern.CASE_INSENSITIVE).matcher(index);
		if (!matcher.find()) throw new IllegalStateException("No NOAA archive found for " + station);
		return matcher.group();
	}

	private static String getText(HttpClient client, String url) {
		return new String(getBytes(client, url), StandardCharsets.UTF_8);
	}

	private static byte[] getBytes(HttpClient client, String url) {
		HttpRequest request = HttpRequest.newBuilder(URI.create(url))
				.header("User-Agent", "ericoij-hydrostatic-model/2.0")
				.timeout(Duration.ofSeconds(60))
				.build();
		try {
			HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
			if (response.statusCode() != 200) {
				throw new IllegalStateException("NOAA returned " + response.statusCode() + " for " + url);
			}
			return response.body();
		} catch (IOException ex) {
			throw new IllegalStateException("Unable to download " + url, ex);
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException("Download interrupted", ex);
		}
	}

	private static String unzipText(byte[] archive) {
		try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(archive))) {
			ZipEntry entry;
			while ((entry = zip.getNextEntry()) != null) {
				if (!entry.isDirectory() && entry.getName().endsWith(".txt")) {
					return new String(zip.readAllBytes(), StandardCharsets.UTF_8);
				}
			}
		} catch (IOException ex) {
			throw new IllegalStateException("Unable to unpack NOAA archive", ex);
		}
		throw new IllegalStateException("NOAA archive did not contain a text sounding");
	}
}
