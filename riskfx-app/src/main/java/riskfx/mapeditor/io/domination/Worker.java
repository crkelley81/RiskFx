package riskfx.mapeditor.io.domination;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import reactor.core.publisher.Mono;
import riskfx.mapeditor.model.MapSkin;
import riskfx.mapeditor.model.TerritorySkin;
import riskfx.mapeditor.outliner.IntrinsicOutliner;
import riskfx.mapeditor.outliner.Outliner;

class Worker {

	public static class DominationTerritoryExt {

		private int index;

		public DominationTerritoryExt(int index) {
			this.index = index;
		}

		public int getColorIndex() {
			return index;
		}

	}

	private static final Logger LOG = Logger.getLogger(DominationMapImporter.class.getName());

	private static final Pattern PATTERN_TERRITORY = Pattern.compile("^(\\d+)\\s*(\\w+)\\s*(\\d+)\\s*(\\d+)\\s*(\\d+)$");
	
	

	public final MapSkin importMap() throws IOException {
		LOG.info("Importing Domination map from " + path + " on " + Thread.currentThread());

		final String name = path.getFileName().toString().replace(".zip", "");
		final String id = name;
		final MapSkin skin = new MapSkin(id, name, new DominationExt());

		try (ZipFile zipFile = new ZipFile(path.toFile())) {
			// Process the map file
			final ZipEntry mapFileEntry = zipFile.getEntry(name + ".map");
			try (InputStream mapFileStream = zipFile.getInputStream(mapFileEntry);
					BufferedReader reader = new BufferedReader(new InputStreamReader(mapFileStream))) {
				processMapFile(skin, reader.lines());
			}

			// Process the pic image
			final String picFileName = skin.as(DominationExt.class).get().getPicImageFileName();
			final ZipEntry picFileEntry = zipFile.getEntry(picFileName);
			if (picFileEntry == null)
				throw new IOException("Archive does not contain a file called " + picFileName);

			try (InputStream picStream = zipFile.getInputStream(picFileEntry)) {
				Image picImage = new Image(picStream);
				skin.setBackgroundImage(picImage);
			}

			// Process the map image
			final String mapFileName = skin.as(DominationExt.class).get().getMapImageFileName();
			final ZipEntry mapImageEntry = zipFile.getEntry(mapFileName);
			if (mapImageEntry == null)
				throw new IOException("Archive does not contain a file called " + mapFileName);

			try (InputStream mapStream = zipFile.getInputStream(mapImageEntry)) {
				Image mapImage = new Image(mapStream);
				skin.setPicImage(mapImage);
				processBoundaries(skin, mapImage);
			}
		}

		return skin;
	}

	private void processBoundaries(final MapSkin skin, final Image mapImage) {
		skin.territories().forEach(ts -> processBoundary(ts, mapImage));
	}

	private void processBoundary(final TerritorySkin ts, final Image mapImage) {
		final Outliner outliner= new IntrinsicOutliner(mapImage);
		final int i = ts.as(DominationTerritoryExt.class).get().getColorIndex();
		final Color c = Color.rgb(i, i, i);
		
		outliner.outlineSvg(c, (int) ts.getIndicatorX(), (int) ts.getIndicatorY())
//			.doOnSubscribe(s -> LOG.info("Processing boundary for " + ts))
//			.doOnNext(svg -> LOG.info("Boundary %s: %s".formatted(ts.getId(), svg)))
//			.doOnError(ex -> LOG.info("Error %s: %s".formatted(ts.getId(), ex)))
			.subscribe( svg -> ts.setBackgroundShape(svg), ex -> {});
	}

	private Mono<String> findBoundary(Image mapImage, int colorIndex, int i, int j) {
		// TODO Auto-generated method stub
		return null;
	}

	private static void processMapFile(final MapSkin skin, final Stream<String> lines) {
		lines.forEach(l -> handle(skin, l));
	}

	private static void handle(final MapSkin skin, final String line) {
		handleMapLine(skin, line);
		handlePicLine(skin, line);
		handleCountriesLine(skin, line);
		handleTerritoryLine(skin, line);
	}

	private static void handleCountriesLine(MapSkin skin, String line) {
		final DominationExt ext = skin.as(DominationExt.class).get();
		if (!ext.countriesFound() && line.contains("[countries]")) {
			ext.setCountriesFound(true);
		}
		else if (ext.countriesFound() && line.isBlank()) {
			ext.setCountriesEnded(true);
		}
		else if (ext.countriesFound() && line.contains("[borders]")) {
			ext.setCountriesEnded(true);
		}
	}

	private static void handlePicLine(MapSkin skin, String line) {
		if (line.startsWith("pic")) {
			final String mapFile = line.substring(4).trim();
			skin.as(DominationExt.class).ifPresent(ext -> ext.setPicImageFile(mapFile));

		}
	}

	private static void handleTerritoryLine(MapSkin skin, String line) {
		final DominationExt ext = skin.as(DominationExt.class).get();
		if (ext.countriesFound() && !ext.countriesEnded() && !line.isBlank()) {
			final Matcher matcher = PATTERN_TERRITORY.matcher(line);
			if (matcher.find()) {
				final int index = Integer.parseInt(matcher.group(1));
				final String name = matcher.group(2);
				final int continentIndex = Integer.parseInt(matcher.group(3));
				final int x = Integer.parseInt(matcher.group(4));
				final int y = Integer.parseInt(matcher.group(5));
				
//				log("New territory: %d %s %d %d %d", index, name, continentIndex, x, y);
				
				final TerritorySkin ts = skin.newTerritorySkin(name, name, x, y, new DominationTerritoryExt(index));			
			}
		}

	}

	private static void handleMapLine(final MapSkin skin, final String line) {
		if (line.startsWith("map")) {
			final String mapFile = line.substring(4).trim();
			skin.as(DominationExt.class).ifPresent(ext -> ext.setMapImageFile(mapFile));
		}
	}

	static class DominationExt {

		private String picImageFileName;
		private String mapImageFileName;
		private boolean countriesEnded;
		private boolean countriesFound;

		public void setMapImageFile(final String mapFile) {
			this.mapImageFileName = mapFile;
		}

		public void setCountriesEnded(boolean b) {
			this.countriesEnded = b;
		}

		public void setCountriesFound(boolean b) {
			this.countriesFound = b;
		}

		public String getMapImageFileName() {
			return this.mapImageFileName;
		}

		public String getPicImageFileName() {
			return this.picImageFileName;
		}

		public boolean countriesEnded() {
			return countriesEnded;
		}

		public boolean countriesFound() {
			return countriesFound;
		}

		public void setPicImageFile(final String mapFile) {
			this.picImageFileName = mapFile;
		}
	}

	
	private final Path path;

	public Worker(final Path path) {
		this.path = Objects.requireNonNull(path);
	}
}
