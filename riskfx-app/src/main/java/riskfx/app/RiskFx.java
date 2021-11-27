package riskfx.app;

import java.util.Objects;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Stage;
import riskfx.app.view.MainMenu;
import riskfx.app.view.Navigation;
import riskfx.util.ui.UiContext;

public class RiskFx {

	public void start(final UiContext<Node> uiContext, final Scene scene, final Stage stage) {
		scene.getStylesheets().add(stylesheet());

		final Navigation nav = DaggerRiskFxApp.builder()
				.riskFxModule(new RiskFxModule(uiContext))
				.build()
				.navigation();
		nav.goToMainMenu();
	}

	private String stylesheet() {
		return Objects.requireNonNull(getClass().getResource("riskfx.css")).toExternalForm();
	}

}
