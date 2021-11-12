package riskfx.mapeditor;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

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
	

}
