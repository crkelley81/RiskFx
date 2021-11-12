package riskfx.mapeditor.io;

import java.io.IOException;
import java.nio.file.Path;

import javafx.stage.FileChooser.ExtensionFilter;
import riskfx.mapeditor.model.MapSkin;

/**
 * 
 * @author christopher
 *
 */
public interface MapImporter {

	/**
	 * Check if this importer can import map at the provided path. Importer
	 * should check quickly if likely can import the map.
	 * 
	 * @param path
	 * @return
	 * @throws IOException
	 */
	public boolean canImport(final Path path) throws IOException;
	
	/**
	 * Import map from the specified {@code path}.
	 * @param path
	 * @return
	 * @throws IOException
	 */
	public MapSkin importMap(final Path path) throws IOException;
	
	public String name();
	
	public ExtensionFilter fileFilter(); 
}
