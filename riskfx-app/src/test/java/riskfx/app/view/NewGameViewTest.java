package riskfx.app.view;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.testfx.api.FxAssert;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import dagger.Lazy;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.RadioButton;
import javafx.stage.Stage;
import riskfx.engine.GameConfig.Type;
import riskfx.engine.MutableGameConfig;
import riskfx.engine.games.ClassicRiskGameConfig;
import riskfx.engine.network.ConnectionFactory;
import riskfx.util.ui.UiContext;

@ExtendWith(ApplicationExtension.class)
class NewGameViewTest {

	private UiContext<Node> context = Mockito.mock(UiContext.class);
	private MutableGameConfig config = new ClassicRiskGameConfig();
	private NewGame newGame;

	private ArgumentCaptor<ConnectionFactory.Notifier> notifier;

	@Start
	public void start(final Stage stage) {
		newGame = new NewGame(context, new Lazy<PlayGame>() {

			@Override
			public PlayGame get() {
				return new PlayGame(context);
			}
			
		});
//		newGame.inflateView();
		newGame.setGameConfig(config);

		final Scene scene = new Scene(newGame);
		stage.setScene(scene);
		stage.sizeToScene();
		stage.show();

	}

	@Test
	public final void localMode(final FxRobot robot) {
		// GIVEN
		Assertions.assertThat(newGame.getMode()).isEqualTo(NewGame.Mode.LOCAL);

		// WHEN

		// THEN

	}

	@Test
	public final void autoPlace(final FxRobot robot) {
		// GIVEN

		// WHEN
		robot.clickOn("#autoPlaceBtn").sleep(10);

		// THEN
		Assertions.assertThat(config.autoPlaceProperty().get()).isTrue();

		// WHEN
		robot.clickOn("#autoPlaceBtn").sleep(10);

		// THEN
		Assertions.assertThat(config.autoPlaceProperty().get()).isFalse();

	}

	@Test
	public final void autoAssign(final FxRobot robot) {
		// GIVEN

		// WHEN
		robot.clickOn("#autoAssignBtn").sleep(10);

		// THEN
		Assertions.assertThat(config.autoAssignProperty().get()).isTrue();

		// WHEN
		robot.clickOn("#autoAssignBtn").sleep(10);

		// THEN
		Assertions.assertThat(config.autoAssignProperty().get()).isFalse();

	}

	@Test
	public final void gameType(final FxRobot robot) {
		// GIVEN

		// WHEN
		robot.clickOn("#dominationBtn").sleep(10);

		// THEN
		FxAssert.verifyThat("#dominationBtn", (RadioButton btn) -> btn.isSelected());
		Assertions.assertThat(config.getGameType()).isEqualTo(Type.DOMINATION);
		
		// WHEN
		robot.clickOn("#missionBtn").sleep(10);

		// THEN
		FxAssert.verifyThat("#missionBtn", (RadioButton btn) -> btn.isSelected());
		Assertions.assertThat(config.getGameType()).isEqualTo(Type.MISSION);

		// WHEN
		robot.clickOn("#capitalBtn").sleep(10);

		// THEN
		FxAssert.verifyThat("#capitalBtn", (RadioButton btn) -> btn.isSelected());
		Assertions.assertThat(config.getGameType()).isEqualTo(Type.CAPITAL);

	}
	
	@Test public final void startGame(final FxRobot robot) {
		// GVEN 
		
		// WHEN 
		robot.clickOn("#startBtn").sleep(10);
		
		// THEN 
		Mockito.verify(context).switchView(Mockito.notNull());
	}
}
