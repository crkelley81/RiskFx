package riskfx.app;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.testfx.api.FxAssert;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.matcher.base.NodeMatchers;

import appfx.window.MainWindow;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import riskfx.app.view.NewGame;
import riskfx.engine.MutableGameConfig;
import riskfx.engine.StandardPlayerTypes;
import riskfx.ui.BoardView;
import riskfx.ui.ViewFixture;
import riskfx.util.ui.ExitController;
import riskfx.util.ui.FileDialogs;
import riskfx.util.ui.UiContext;

@ExtendWith(ApplicationExtension.class)
class NewGameFlowIntegrationTest {

	private MainWindow mainWindow;
	private ExitController exitController = Mockito.mock(ExitController.class);
	private FileDialogs<Node,FileChooser> fileDialogs = Mockito.mock(FileDialogs.class);
	private UiContext<Node> uiContext;
	private Node playGame;

	@Start public void start(final Stage stage) {
		mainWindow = new MainWindow();
		mainWindow.setPrefSize(1200, 800);
		uiContext = RiskFxApplication.uiContext(mainWindow, fileDialogs, exitController);
		
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
		robot.clickOn("Auto Assign");
		robot.clickOn("Auto Place");
		
		NewGame newGame = (NewGame) mainWindow.getContent();
		MutableGameConfig config = newGame.getGameConfig();
		robot.interact(() -> {
			config.playerAssociations().get(3).typeProperty().set(StandardPlayerTypes.NONE);
			config.playerAssociations().get(4).typeProperty().set(StandardPlayerTypes.NONE);
			config.playerAssociations().get(5).typeProperty().set(StandardPlayerTypes.NONE);
		});
		
		FxAssert.verifyThat("Start", NodeMatchers.isEnabled());
		
		robot.clickOn("Start").sleep(500);
	
		final BoardView boardView = robot.lookup(".board").query();
		robot.interact(() -> {
			boardView.setBackgroundImageUrl(ViewFixture.bigeuropeImageUrl().toExternalForm());
		});
		robot.sleep(3000);
		
		FxAssert.verifyThat("#playGame", NodeMatchers.isVisible());
		
		
	}

}
