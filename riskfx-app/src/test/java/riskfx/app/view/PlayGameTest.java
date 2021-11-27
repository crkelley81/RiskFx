package riskfx.app.view;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;

import org.assertj.core.api.Assertions;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.testfx.api.FxAssert;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.matcher.base.GeneralMatchers;
import org.testfx.matcher.base.NodeMatchers;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import riskfx.engine.MutableGameConfig;
import riskfx.engine.StandardPlayerTypes;
import riskfx.engine.display.Display;
import riskfx.engine.game.Game;
import riskfx.engine.games.BigEuropeGameConfig;
import riskfx.engine.model.Player;
import riskfx.ui.ViewFixture;
import riskfx.util.role.Selectable;
import riskfx.util.ui.UiContext;

@ExtendWith(ApplicationExtension.class)
class PlayGameTest {
	private Display notifier = Mockito.mock(Display.class);
	private UiContext<Node> context = Mockito.mock(UiContext.class);
	private MutableGameConfig config = new BigEuropeGameConfig(); // new ClassicRiskGameConfig();
	private Game game;

	private PlayGame playGame;
	private Player PLAYER_BLACK;
	private Player PLAYER_BLUE;
	private Player PLAYER_RED;

	@Start
	public void start(Stage stage) {

		config.setAutoAssign(true);
		config.setAutoPlace(true);

		config.playerAssociations().forEach(pa -> {
			if (Arrays.asList("blue", "black", "red").contains(pa.getPlayer().getId())) {
				
			}
			else {
				pa.typeProperty().set(StandardPlayerTypes.NONE);
			}
		});
		
		
		game = Game.from(config);

		PLAYER_BLACK = game.lookup("black").player();
		PLAYER_BLUE = game.lookup("blue").player();
		PLAYER_RED = game.lookup("red").player();
		
		playGame = new PlayGame(context);
		playGame.play(game, ViewFixture.bigeuropeSkinUrl());

		final Scene scene = new Scene(playGame);
		stage.setScene(scene);
		stage.show();
	}

	@Nested
	@DisplayName("show cards")
	public class ShowCards {
		@Start
		public void start(final Stage stage) {
			PlayGameTest.this.start(stage);
		}
		
		@Test public void showCards(final FxRobot robot) {
			// GIVEN 
			
			// WHEN 
			robot.clickOn("#cardsBtn").sleep(500);
			
			FxAssert.verifyThat(".hand-view", NodeMatchers.isVisible());
		}
		
		@Test public void closeButtonClosesWindow(final FxRobot robot) {
			// GIVEN 
			
			// WHEN 
			robot.clickOn("#cardsBtn").sleep(500);
			
			// THEN 
			FxAssert.verifyThat("#hideButton", NodeMatchers.isVisible());
			
			// WHEN 
			robot.clickOn("#hideButton").sleep(600);
			
			FxAssert.verifyThat(".hand-view", NodeMatchers.isInvisible());
		}
	}
	
	@Nested
	@DisplayName("current player indicator")
	public class CurrentPlayer {
		@Start
		public void start(final Stage stage) {
			PlayGameTest.this.start(stage);
		}

		@Test
		public void showsCurrentPlayer(final FxRobot robot) {
			// GIVEN
			final Set<Label> player = robot.lookup(".player-indicator-label").queryAllAs(Label.class);
			
			final Label black = player.stream().filter(l -> Objects.equals(PLAYER_BLACK.getDisplayName(), l.getText()))
					.findAny().orElseThrow();
			final Label blue = player.stream().filter(l -> Objects.equals(PLAYER_BLUE.getDisplayName(), l.getText()))
					.findAny().orElseThrow();
			final Label red = player.stream().filter(l -> Objects.equals(PLAYER_RED.getDisplayName(), l.getText()))
					.findAny().orElseThrow();
			// WHEN

			FxAssert.verifyThat(black, NodeMatchers.isVisible());
			FxAssert.verifyThat(blue, NodeMatchers.isVisible());
			FxAssert.verifyThat(red, NodeMatchers.isVisible());
			
			
			robot.interact(() -> {
				playGame.notifier.onBeginTurn(PLAYER_BLACK, game.turnOrder().indexOf(PLAYER_BLACK), 1);
			}).sleep(10);

//			Assertions.assertThat(robot.lookup("#playerIndicator").query().getStyleClass()).contains("black");
//			FxAssert.verifyThat("#playerIndicator", hasBackgroundColor(Color.BLACK));

			Assertions.assertThat(black.getParent().getStyleClass()).contains("player-indicator-current");
			
			// WHEN
			robot.interact(() -> {
				playGame.notifier.onBeginTurn(PLAYER_BLUE, game.turnOrder().indexOf(PLAYER_BLUE), 1);
			}).sleep(10);
			Assertions.assertThat(blue.getParent().getStyleClass()).contains("player-indicator-current");
//						Assertions.assertThat(robot.lookup("#playerIndicator").query().getStyleClass()).contains("black");
//			FxAssert.verifyThat("#playerIndicator", hasBackgroundColor(Color.BLUE));

		}
	}

	public static <T extends Region> Matcher<Region> hasBackgroundColor(final Color c) {
		return GeneralMatchers.typeSafeMatcher(Region.class, "has background color " + c,
				s -> String.valueOf(s.getBackground().getFills().get(0).getFill()),
				r -> r.getBackground().getFills().get(0).getFill().equals(c));
	}

	public static <T extends Selectable> Matcher<T> isNotSelected() {
		return (Matcher<T>) GeneralMatchers.typeSafeMatcher(Selectable.class, "is not selected",
				s -> s.isSelected() ? "is selected" : "is not selected", s -> !s.isSelected());
	}

	public static <T extends Selectable> Matcher<T> isSelected() {
		return (Matcher<T>) GeneralMatchers.typeSafeMatcher(Selectable.class, "is selected",
				s -> s.isSelected() ? "is selected" : "is not selected", s -> s.isSelected());
	}

}
