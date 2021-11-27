package riskfx.app;

import appfx.window.JavaFxFileDialogs;
import appfx.window.MainWindow;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import riskfx.util.ui.ExitController;
import riskfx.util.ui.FileDialogs;
import riskfx.util.ui.UiContext;
import riskfx.util.ui.UserNotification;
import riskfx.util.ui.UserNotificationWithAction;

public class RiskFxApplication extends Application {

	public static void main(String[] args) {
		launch(args);
	}
	
	private MainWindow mainWindow;
	private UiContext<Node> uiContext;

	@Override public void start(final Stage stage) {
		mainWindow = new MainWindow();
		uiContext = uiContext(mainWindow, new JavaFxFileDialogs(), this::requestExit);
		
		final Scene scene = new Scene(mainWindow);
		scene.getStylesheets().add(getClass().getResource("riskfx.css").toExternalForm());
		stage.setScene(scene);
		
		postInit(uiContext, scene, stage);
		stage.show();
	}
	
	public static UiContext<Node> uiContext(MainWindow mainWindow, FileDialogs<Node, FileChooser> fileDialogs, ExitController exit) {
		return new UiContext<Node>() {

			@Override
			public void lockView(UserNotification n) {
				mainWindow.lockView(n);
			}

			@Override
			public void unlockView() {
				mainWindow.unlockView();
			}

			@Override
			public void notify(UserNotification n) {
				mainWindow.notify(n);
			}

			@Override
			public void notify(UserNotificationWithAction n) {
				mainWindow.notify(n);
			}

			@Override
			public void requestExit() {
				exit.requestExit();
			}

			@Override
			public void switchView(final Node node) {
				System.err.println("Switch view: " + node);
				mainWindow.setContent(node);
			}

			@Override
			public void notifyError(UserNotification of, Throwable ex) {
				final UserNotificationWithAction n = of.withAction("DETAIL", () -> {
					// TODO Show dialog
					ex.printStackTrace();
				});
				mainWindow.notify(n);
			}

			@Override
			public void goBack() {
				// TODO Auto-generated method stub
				
			}
			
		};
	}

	protected void postInit(UiContext<Node> uiContext, Scene scene, Stage primaryStage) {
		new RiskFx().start(uiContext, scene, primaryStage);
	}

	private void requestExit() {
		Platform.exit();
	}

	
}
