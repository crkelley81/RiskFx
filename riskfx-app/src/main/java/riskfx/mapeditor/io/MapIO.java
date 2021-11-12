package riskfx.mapeditor.io;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.ServiceLoader;
import java.util.ServiceLoader.Provider;

import riskfx.mapeditor.model.MapSkin;

public interface MapIO {

	public Collection<MapImporter> importers();
	
	public MapSkin openMap(final Path path) throws IOException;

	public MapSkin saveMap(final MapSkin map, final Path path) throws IOException;
	
	static MapIO create() {
		final ServiceLoader<MapImporter> loader = ServiceLoader.load(MapImporter.class);
		return new DefaultMapIO(loader.stream().map(Provider::get));
	}
}
