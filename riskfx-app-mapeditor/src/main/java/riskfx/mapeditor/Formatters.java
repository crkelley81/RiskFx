package riskfx.mapeditor;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;

import javafx.util.StringConverter;

public class Formatters {

	public static StringConverter<URL> url() {
		return new StringConverter<URL>() {

			@Override
			public URL fromString(String arg0) {
				try {
					return new URL(arg0);
				} catch (MalformedURLException e) {
					return null;
				}
			}

			@Override
			public String toString(URL url) {
				return Optional.ofNullable(url).map(URL::toExternalForm).orElse("");
			}};
	}

}
