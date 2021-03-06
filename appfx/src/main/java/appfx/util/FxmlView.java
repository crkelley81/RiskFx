package appfx.util;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import appfx.window.AppBar;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.StackPane;

public class FxmlView extends StackPane {
	private final CompletableFuture<FxmlView> loadFuture = new CompletableFuture<>();

	public final void inflateView() {
		assert !loadFuture.isDone();

		final FXMLLoader loader = new FXMLLoader();

		final String fxmlName = getFxmlName();
		final URL fxmlUrl = getClass().getResource(fxmlName);
		assert fxmlUrl != null;
		
		try {
			loader.setLocation(fxmlUrl);
			loader.setController(this);
			loader.setRoot(this);

			loader.load();
			addCssIfExists(getCssName());

			loadFuture.complete(this);
		} catch (IOException ioe) {
			System.err.println("Failed" + ioe);
			loadFuture.completeExceptionally(ioe);
		}
	}

	protected final CompletionStage<FxmlView> loadFuture() {
		return loadFuture;
	}

	private void addCssIfExists(String cssName) {
		final URL url = getClass().getResource(cssName);
		if (url != null) {
			getStylesheets().add(url.toExternalForm());
		}
	}

	protected String getBaseName() {
		return getClass().getSimpleName().toLowerCase().replace("presenter", "");
	}

	protected String getCssName() {
		return getBaseName() + ".css";
	}

	protected String getFxmlName() {
		return getBaseName() + ".fxml";
	}

	public void updateAppBar(final AppBar appBar) {
		// TODO Auto-generated method stub

	}
}
