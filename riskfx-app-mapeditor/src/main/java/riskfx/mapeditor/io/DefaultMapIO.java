package riskfx.mapeditor.io;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import riskfx.mapeditor.model.MapSkin;

class DefaultMapIO implements MapIO {

	private final List<MapImporter> importers;

	public DefaultMapIO(final Stream<MapImporter> map) {
		this.importers = Objects.requireNonNull(map).collect(Collectors.toUnmodifiableList());
	}

	@Override
	public final Collection<MapImporter> importers() {
		return this.importers;
	}

	@Override
	public MapSkin openMap(Path path) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MapSkin saveMap(final MapSkin map, final Path path) throws IOException {
		Objects.requireNonNull(map);
		Objects.requireNonNull(path);

		final Path tempPath = Files.createTempFile(getClass().getSimpleName(), ".css");

		final StringBuilder text = new StringBuilder();
		map.renderCss(text);
		
		try {
			try (BufferedWriter out = Files.newBufferedWriter(tempPath, Charset.forName("UTF-8"))) {
				out.append(text);
				out.newLine();
				out.flush();
			}

			Files.copy(tempPath, path, StandardCopyOption.REPLACE_EXISTING);
		} finally {
			Files.deleteIfExists(tempPath);
		}

		return map;
	}

}
