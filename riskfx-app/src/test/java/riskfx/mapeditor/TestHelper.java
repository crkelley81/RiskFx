package riskfx.mapeditor;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import org.mockito.ArgumentMatcher;

import javafx.scene.Node;
import javafx.scene.text.Text;
import riskfx.mapeditor.io.domination.DominationMapImporter;
import riskfx.mapeditor.model.MapSkin;

public class TestHelper {
	public static URL bigeuropeUrl() {
		final URL url =  TestHelper.class.getResource("io/domination/bigeurope.zip");
		assert url != null;
		return url;
	}
	
	public static Path bigeuropePath() {
		try {
			return Paths.get(bigeuropeUrl().toURI());
		} catch (URISyntaxException e) {
			throw new IllegalStateException(e);
		}
	}

	public static ArgumentMatcher<Text> textHasText(String string) {
		return new ArgumentMatcher<Text>() {

			@Override
			public boolean matches(final Text argument) {
				return Objects.equals(argument.getText(), string);
			}};
	}

	public static URL bigeuropeSkinUrl() {
		return Objects.requireNonNull(null);
	}

	public static <N extends Node> ArgumentMatcher<N> isHovered() {
		return new ArgumentMatcher<N>() {

			@Override
			public boolean matches(N argument) {
				return argument.isHover();
			}
			
		};
	}

	public static URL bigeuropeImageUrl() {
		return Objects.requireNonNull(TestHelper.class.getResource("bigeurope_pic.jpg"));
	}

	public static MapSkin bigeuropeMapSkin() {
		final DominationMapImporter importer = new DominationMapImporter();
		try {
			return importer.importMap(bigeuropePath());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	

}
