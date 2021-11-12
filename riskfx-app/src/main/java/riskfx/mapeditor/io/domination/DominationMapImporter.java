package riskfx.mapeditor.io.domination;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import riskfx.mapeditor.io.MapImporter;
import riskfx.mapeditor.model.MapSkin;

public final class DominationMapImporter implements MapImporter {

	@Override
	public boolean canImport(final Path path) throws IOException {
//		System.err.println(path.getFileName().toString())
		return path.getFileName().toString().endsWith(".zip") && Files.exists(path);
	}

	@Override
	public MapSkin importMap(Path path) throws IOException {
		return new Worker(path).importMap();
	}

	@Override
	public String name() {
		return "Domination";
	}

	@Override
	public ExtensionFilter fileFilter() {
		return new FileChooser.ExtensionFilter("Domination Map", "*.zip");
	}

}
