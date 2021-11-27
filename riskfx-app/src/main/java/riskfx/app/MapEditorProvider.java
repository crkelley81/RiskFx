package riskfx.app;

import java.util.Optional;
import java.util.ServiceLoader;

import appfx.util.FxmlView;
import javafx.scene.Node;
import riskfx.util.ui.UiContext;

public interface MapEditorProvider {

	public static Optional<MapEditorProvider> findProvider() {
		final ServiceLoader<MapEditorProvider> locator = ServiceLoader.load(MapEditorProvider.class);
		return locator.findFirst();
	}
	
	public FxmlView load(final UiContext<Node> context);
}
