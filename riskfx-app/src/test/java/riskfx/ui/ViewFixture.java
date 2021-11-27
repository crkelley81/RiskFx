package riskfx.ui;

import java.net.URL;
import java.util.Objects;

public class ViewFixture {

	public static String bigeuropeSkinUrl() {
		return Objects.requireNonNull(ViewFixture.class.getResource("bigeurope.css")).toExternalForm();
	}

	public static URL bigeuropeImageUrl() {
		return Objects.requireNonNull(ViewFixture.class.getResource("bigeurope_pic.jpg"));
	}

}
