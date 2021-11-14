package riskfx.app;

import java.util.Objects;

import appfx.ui.AppFxApplication;
import appfx.ui.UiContext;
import javafx.scene.Scene;
import javafx.stage.Stage;
import riskfx.app.view.MainMenu;
import riskfx.mapeditor.MapEditorModule;

public class RiskFxApplication extends AppFxApplication {

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	protected void postInit(UiContext uiContext, Scene scene, Stage primaryStage) {
		scene.getStylesheets().add(stylesheet());
		
		final MainMenu mainMenu = DaggerRiskFxApp.builder()
				.riskFxModule(new RiskFxModule(uiContext))
				.mapEditorModule(new MapEditorModule())
				.build()
				.mainMenu();
		mainMenu.inflateView();
		uiContext.switchView(mainMenu);
	}

	private String stylesheet() {
		return Objects.requireNonNull(getClass().getResource("riskfx.css")).toExternalForm();
	}

}
