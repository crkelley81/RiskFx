package riskfx.ui;

import java.util.Objects;

import org.assertj.core.api.Assertions;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxAssert;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.matcher.base.GeneralMatchers;
import org.testfx.matcher.base.NodeMatchers;

import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import riskfx.engine.model.Player;
import riskfx.engine.model.Territory;

@ExtendWith(ApplicationExtension.class)
class TerritorySkinUiTest {

	private TerritorySkin<?> view;
	private Scene scene;
	private Territory territorySi1;
	private Player playerRed;

	@Start public final void start(final Stage stage) {
		playerRed = Player.of("red", "Red");
		territorySi1 = Territory.of("si1", "Si1");
		view = TerritorySkin.forTerritory(territorySi1);

		Pane pane = new Pane();
		pane.setMinHeight(600);
		pane.setMinWidth(800);
		pane.setStyle(
				"-fx-background-color: white; -fx-border-color: gray; -fx-border-width: .25px; -fx-padding: 20px;");
		pane.getChildren().add(view);

		scene = new Scene(pane);
		stage.setScene(scene);
		stage.show();
		stage.getScene().getStylesheets().add(ViewFixture.bigeuropeSkinUrl());
		
		//Thread.currentThread().setUncaughtExceptionHandler(this::uncaughtException);
	}

	@Test public void indicator(final FxRobot robot) {
		// GIVEN 
		// WHEN 
		robot.interact(() -> {
			territorySi1.setOwner(playerRed);
			territorySi1.setArmies(3);
		});

		// THEN 
		
		Assertions.assertThat(view.getIndicatorX()).isEqualTo(447);
		Assertions.assertThat(view.getIndicatorY()).isEqualTo(344);
		
		FxAssert.verifyThat(".territory-indicator", NodeMatchers.isVisible());
		FxAssert.verifyThat(".territory-indicator-text", NodeMatchers.isVisible());
		FxAssert.verifyThat(".territory-indicator-text", text("3"));
	}

	private <T extends Text> Matcher<Text> text(String string) {
		return GeneralMatchers.typeSafeMatcher(Text.class, 
				"Text has text \"" + string + "\"",
				s -> "was " + s.getText() + "",
				s -> Objects.equals(s.getText(), string));
	}
}
