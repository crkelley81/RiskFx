package appfx.window;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxAssert;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.matcher.base.NodeMatchers;
import org.testfx.matcher.control.LabeledMatchers;

import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import riskfx.util.ui.UserNotification;

@ExtendWith(ApplicationExtension.class)
class MainWindowTest {

	private MainWindow mainWindow;
	private Label content;
	
	@Start public void start(final Stage stage) {
		content = new Label("This is my content ...");
		content.setId("my-content");
		
		mainWindow = new MainWindow();
		mainWindow.getAppBar().titleTextProperty().set("My Application");
		mainWindow.setContent(content);

		final Scene scene = new Scene(mainWindow, 600, 600);
		stage.setScene(scene);
		stage.show();
	}
	
	
	@Test void showNotification(final FxRobot robot) {
		// GIVEN 
		// WHEN 
		robot.interact(() -> {
			final UserNotification message = UserNotification.of("Hello world!");
			mainWindow.notify(message);
		}).sleep(1000);
		
		// THEN
		FxAssert.verifyThat("Hello world!", NodeMatchers.isVisible());
	}
	
	@Test
	void test(final FxRobot robot) throws InterruptedException {
		// GIVEN 
		FxAssert.verifyThat("#main-window-container", NodeMatchers.isVisible());
		Assertions.assertThat(mainWindow.getContent()).isEqualTo(content);

		// THEN 
		
		// WHEN 
		
		FxAssert.verifyThat(".mainwindow", NodeMatchers.isVisible());
		FxAssert.verifyThat(".appbar", NodeMatchers.isVisible());
		FxAssert.verifyThat(".appbar-title", LabeledMatchers.hasText("My Application"));
		
		FxAssert.verifyThat("#my-content", NodeMatchers.isVisible());
		
		Thread.sleep(2000);
	}

}
