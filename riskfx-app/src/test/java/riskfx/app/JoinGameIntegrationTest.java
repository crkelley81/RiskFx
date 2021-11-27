package riskfx.app;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.testfx.api.FxAssert;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.matcher.control.LabeledMatchers;

import akka.actor.ActorSystem;
import appfx.window.MainWindow;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import riskfx.util.ui.ExitController;
import riskfx.util.ui.FileDialogs;
import riskfx.util.ui.UiContext;

@ExtendWith(ApplicationExtension.class)
class JoinGameIntegrationTest {

	private MainWindow mainWindow;
	private ExitController exitController = Mockito.mock(ExitController.class);
	private FileDialogs<Node, FileChooser> fileDialogs = Mockito.mock(FileDialogs.class);
	private UiContext<Node> uiContext;

	@Start
	public void start(final Stage stage) {
		mainWindow = new MainWindow();
		mainWindow.setPrefSize(1200, 800);
		uiContext = RiskFxApplication.uiContext(mainWindow, fileDialogs, exitController);

		final Scene scene = new Scene(mainWindow);
		stage.setScene(scene);

		stage.show();

		new RiskFx().start(uiContext, scene, stage);

	}

	@Test
	public void joinGameFails(final FxRobot robot) {
		// GIVEN

		// WHEN
		robot.clickOn("Join Game").sleep(300);

		robot.clickOn("#nameField").write("Christopher").press(KeyCode.TAB);
		robot.write("127.0.0.1").press(KeyCode.TAB).sleep(100);
		robot.clickOn("#portField").doubleClickOn("#portField");
		robot.write("1024").press(KeyCode.TAB).sleep(100);
		robot.clickOn("Connect");
		robot.sleep(2000);

		// THEN
		FxAssert.verifyThat("#messageField", LabeledMatchers.hasText("Could not find server"));
	}

	@Test
	public void joinGameSucceeds(final FxRobot robot) throws InterruptedException {
		// GIVEN
		ActorSystem system = new ServerFixture().startup("localhost", 25520, "mypassword");
		Thread.sleep(1000);
		
		try {
			// WHEN
		/*	robot.clickOn("Join Game").sleep(300);

			robot.clickOn("#nameField").write("Christopher").press(KeyCode.TAB);
			robot.write("127.0.0.1").press(KeyCode.TAB).sleep(100);
			robot.clickOn("#portField").doubleClickOn("#portField");
			robot.write("25520").press(KeyCode.TAB).sleep(100);
			robot.clickOn("Connect");
			robot.sleep(2000);

			// THEN
			FxAssert.verifyThat("#messageField", LabeledMatchers.hasText("Could not find server"));*/
		} finally {
			system.terminate();
		}
	}

}
