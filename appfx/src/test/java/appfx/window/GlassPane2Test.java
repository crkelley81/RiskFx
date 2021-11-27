package appfx.window;

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
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

@ExtendWith(ApplicationExtension.class)
class GlassPane2Test {

	private GlassPane2 glassPane;
	private Label content;

	@Start public void start(final Stage stage) {
		glassPane = new GlassPane2();

		content  = new Label("My Content");
		final StackPane stackPane = new StackPane(content, glassPane);
		
		final Scene scene = new Scene(stackPane, 600, 400);
		stage.setScene(scene);
		stage.show();
	}
	
	@Test void becomesVisibleOnShow(final FxRobot robot) {
		// GIVEN 
		// WHEN 
		robot.interact(() -> {
			glassPane.setMessage("Testing ...");
			glassPane.show();
		}).sleep(500);

		// THEN 
		FxAssert.verifyThat(".glasspane", NodeMatchers.isVisible());
		FxAssert.verifyThat(".glasspane-message", LabeledMatchers.hasText("Testing ..."));
		
		// WHEN 
		robot.interact(() -> {
			try {
				glassPane.hide();
			}
			catch (RuntimeException re) {
				System.err.println("Error hiding: " + re);
			}
		}).sleep(500);
		
		// THEN 
		FxAssert.verifyThat(".glasspane", NodeMatchers.isInvisible());
	}
	
	@Test
	void invisibleByDefault(final FxRobot robot) {
		// GIVEN 
		// WHEN
		// THEN
		FxAssert.verifyThat(".glasspane", NodeMatchers.isInvisible());
	}

}
