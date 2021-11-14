package appfx.ui;

import appfx.window.MainWindow;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;

public abstract class AppFxApplication extends Application {
	private MainWindow mainWindow;
	private UiContext uiContext;
	
	@Override
	public final void start(final Stage primaryStage) throws Exception {
		mainWindow = new MainWindow();
		uiContext = UiContext.of(mainWindow, new FileDialogs() {}, Platform::exit);
		
		final Scene scene = new Scene(mainWindow);
		primaryStage.setScene(scene);
		
		postInit(uiContext, scene, primaryStage);	
		
		primaryStage.sizeToScene();
		primaryStage.show();
	}

	protected abstract void postInit(final UiContext uiContext, final Scene scene, final Stage primaryStage);
	
}
