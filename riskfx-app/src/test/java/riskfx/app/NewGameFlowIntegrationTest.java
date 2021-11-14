package riskfx.app;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.testfx.api.FxAssert;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.matcher.base.NodeMatchers;

import appfx.ui.ExitController;
import appfx.ui.FileDialogs;
import appfx.ui.UiContext;
import appfx.window.MainWindow;
import javafx.scene.Scene;
import javafx.stage.Stage;

@ExtendWith(ApplicationExtension.class)
class NewGameFlowIntegrationTest {

	private MainWindow mainWindow;
	private ExitController exitController = Mockito.mock(ExitController.class);
	private FileDialogs fileDialogs = Mockito.mock(FileDialogs.class);
	private UiContext uiContext;

	@Start public void start(final Stage stage) {
		mainWindow = new MainWindow();
		mainWindow.setPrefSize(1200, 800);
		uiContext = UiContext.of(mainWindow, fileDialogs, exitController);
		
		final Scene scene = new Scene(mainWindow);
		stage.setScene(scene);
		
		stage.show();
		
		new RiskFx().start(uiContext, scene, stage);
		
	}
	
	@Test
	void test(final FxRobot robot) {
		// GIVEN 
		robot.sleep(1000);
		
		// WHEN 
		robot.clickOn("New Game").sleep(300);
		robot.clickOn("Domination").sleep(10);
		
		FxAssert.verifyThat("Start", NodeMatchers.isEnabled());
		
		robot.clickOn("Start");
		
	}

}
