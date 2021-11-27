package riskfx.app.view;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.Assumptions;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.BeforeEach;
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
import org.testfx.matcher.control.LabeledMatchers;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import riskfx.engine.GameConfig;
import riskfx.engine.display.Display;
import riskfx.engine.game.BigEuropeGameFixture;
import riskfx.engine.game.EngineTestUtil;
import riskfx.engine.game.Game;
import riskfx.engine.model.Moves;
import riskfx.engine.model.Moves.Attack;
import riskfx.engine.model.Moves.Claim;
import riskfx.engine.model.Moves.Fortify;
import riskfx.engine.model.Moves.Place;
import riskfx.engine.model.Moves.Reinforce;
import riskfx.engine.model.Moves.TurnInCards;
import riskfx.engine.model.Player;
import riskfx.engine.model.Territory;
import riskfx.ui.ViewFixture;
import riskfx.util.role.Selectable;
import riskfx.util.ui.UiContext;

@ExtendWith(ApplicationExtension.class)
class PlayGamePlayerTest {
	private Display notifier = Mockito.mock(Display.class);
	private UiContext<Node> context = Mockito.mock(UiContext.class);
	private BigEuropeGameFixture gameFixture;

	private PlayGame playGame;
	private Player PLAYER_BLACK;
	private Player PLAYER_BLUE;
	private GameConfig gameConfig;
	private Game game;

	@Start
	public void start(Stage stage) {
		gameFixture = new BigEuropeGameFixture(75, false, false);
		gameConfig = gameFixture.gameConfig;
		game = gameFixture.game;
		
		
		PLAYER_BLACK = gameFixture.playerBlack;
		PLAYER_BLUE = gameFixture.playerBlue;

		playGame = new PlayGame(context);
		playGame.play(game, ViewFixture.bigeuropeSkinUrl());

		final Scene scene = new Scene(playGame);
		stage.setScene(scene);
		stage.show();
	}

	@Nested
	@DisplayName("when game calls claim territory")
	public class ClaimTerritory {
		@Start
		public void start(final Stage stage) {
			PlayGamePlayerTest.this.start(stage);
//			autoAssignAll(game.map().territories(), game.turnOrder());

		}

		@Test
		public void notifiesPlayer(final FxRobot robot) {
			// GIVEN
			final Flux<Claim> flux = playGame.claim(game.state(), PLAYER_BLACK, 5);

			// WHEN
			robot.interact(() -> {
				flux.subscribe();
			}).sleep(10);

			// THEN
			FxAssert.verifyThat("#messageField", LabeledMatchers.hasText("Claim 5 territories"));
		}

		@Test
		public void selectsTerritoryOnClick(final FxRobot robot) {
			// GIVEN
			final List<Claim> territories = new ArrayList<>();
			final Flux<Claim> flux = playGame.claim(game.state(), PLAYER_BLACK, 5).doOnNext(territories::add);

			// WHEN
			robot.interact(() -> {
				flux.subscribe();
			}).sleep(10);

			// THEN
			final Territory t = game.map().lookupTerritory("si1");
			Assumptions.assumeThat(t.getOwner()).isEqualTo(Player.none());

			robot.moveTo("#si1").sleep(10).clickOn().sleep(100);
			Assertions.assertThat(territories.get(0).territory()).isEqualTo(t);
		}

		@Test
		public void ignoresMultipleClicks(final FxRobot robot) {
			// GIVEN
			final List<Claim> territories = new ArrayList<>();
			final Flux<Claim> flux = playGame.claim(game.state(), PLAYER_BLACK, 5).doOnNext(territories::add);

			// WHEN
			robot.interact(() -> {
				flux.subscribe();
			}).sleep(10);

			// THEN
			final Territory t = game.map().lookupTerritory("si1");
			Assumptions.assumeThat(t.getOwner()).isEqualTo(Player.none());

			robot.moveTo("#si1").sleep(10).clickOn().sleep(10).clickOn().clickOn().clickOn();
			Assertions.assertThat(territories.get(0).territory()).isEqualTo(t);
			Assertions.assertThat(territories).hasSize(1);
		}

		@Test
		public void ignoresClicksOnOwnedTerritories(final FxRobot robot) {
			// GIVEN

			EngineTestUtil.update(game.state(), notifier, Moves.claim(game.map().lookupTerritory("si2"), PLAYER_BLACK));

			EngineTestUtil.update(game.state(), notifier, Moves.claim(game.map().lookupTerritory("f2"), PLAYER_BLUE));
			final List<Claim> territories = new ArrayList<>();
			final AtomicBoolean done = new AtomicBoolean(false);
			final Flux<Claim> flux = playGame.claim(game.state(), PLAYER_BLACK, 5).doOnNext(territories::add)
					.doOnNext(m -> {
						EngineTestUtil.update(game.state(), notifier, m);
					}).doAfterTerminate(() -> done.set(true));

			// WHEN
			robot.interact(() -> {
				flux.subscribe();
			}).sleep(10);

			// WHEN
			robot.moveTo("#si1").sleep(10).clickOn();
			robot.moveTo("#si2").sleep(10).clickOn();
			robot.moveTo("#b4").sleep(10).clickOn();
			robot.moveTo("#f5").sleep(10).clickOn();
			robot.moveTo("#f6").sleep(10).clickOn();
			robot.moveTo("#sp16").sleep(10).clickOn();
			robot.moveTo("#por2").sleep(10).clickOn();

			// THEN
			Assertions.assertThat(done).isTrue();
			Assertions.assertThat(territories).hasSize(5);
		}

		@Test
		public void endsAfter5Clicks(final FxRobot robot) {
			// GIVEN
			final List<Claim> territories = new ArrayList<>();
			final AtomicBoolean done = new AtomicBoolean(false);
			final Flux<Claim> flux = playGame.claim(game.state(), PLAYER_BLACK, 5).doOnNext(territories::add)
					.doAfterTerminate(() -> done.set(true));

			// WHEN
			robot.interact(() -> {
				flux.subscribe();
			}).sleep(10);

			// THEN

			robot.moveTo("#si1").sleep(10).clickOn();
			robot.moveTo("#b4").sleep(10).clickOn();
			robot.moveTo("#f5").sleep(10).clickOn();
			robot.moveTo("#sp16").sleep(10).clickOn();
			robot.moveTo("#por2").sleep(10).clickOn();

			Assertions.assertThat(done).isTrue();
		}
	}

	@Nested
	@DisplayName("when game calls place armies")
	public class PlaceArmies {

		private Flux<Place> flux;
		private AtomicBoolean done = new AtomicBoolean(false);
		private List<Place> moves = new ArrayList<>();
		private List<Territory> ownedByBlack;
		private List<Territory> ownedByBlue;

		@Start
		public void start(final Stage stage) {
			PlayGamePlayerTest.this.start(stage);
			autoAssignAll(game.map().territories(), game.turnOrder());

			ownedByBlack = game.map().territories().stream().filter(t -> t.isOwnedBy(PLAYER_BLACK))
					.collect(Collectors.toList());
			ownedByBlue = game.map().territories().stream().filter(t -> t.isOwnedBy(PLAYER_BLUE))
					.collect(Collectors.toList());

			flux = playGame.place(game.state(), PLAYER_BLACK, 5).doOnNext(moves::add)
					.doOnNext(m -> EngineTestUtil.update(game.state(), notifier, m))
					.doAfterTerminate(() -> done.set(true));
		}

		@Test
		public void notifiesPlayer(final FxRobot robot) {
			// GIVEN
			final Flux<Place> flux = playGame.place(game.state(), PLAYER_BLACK, 5);

			// WHEN
			flux.subscribe();
			robot.sleep(10);

			// THEN
			FxAssert.verifyThat("#messageField", LabeledMatchers.hasText("Place 5 armies"));
		}

		@Test
		public void placesArmyOnClick(final FxRobot robot) {
			// GIVEN

			flux.subscribe();

			// THEN
			robot.moveTo("#" + ownedByBlack.get(0).getId()).clickOn();

			Assertions.assertThat(moves).hasSize(1);
		}

		@Test
		public void pressAndHold(final FxRobot robot) {
			// GIVEN
			flux.subscribe();

			// WHEN
			robot.moveTo("#" + ownedByBlack.get(0).getId());
			robot.press(MouseButton.PRIMARY).sleep(6 * 100).release(MouseButton.PRIMARY).sleep(10);

			// THEN
			Assertions.assertThat(moves).hasSize(5);
			Assertions.assertThat(done).isTrue();

		}

		@Test
		public void assignAllInTerritory(final FxRobot robot) {
			// GIVEN
			flux.subscribe();

			// WHEN
			robot.moveTo("#" + ownedByBlack.get(0).getId());
			for (int i = 0; i < 5; i++) {
				robot.clickOn();
			}
			robot.sleep(10);

			// THEN
			Assertions.assertThat(done).isTrue();
			Assertions.assertThat(moves).hasSize(5);
		}

		@Test
		public void ignoresClicksOnOwnedTerritories(final FxRobot robot) {
			// GIVEN
			flux.subscribe();

			// WHEN
			robot.moveTo("#" + ownedByBlack.get(0).getId()).clickOn();
			robot.moveTo("#" + ownedByBlack.get(1).getId()).clickOn();
			robot.moveTo("#" + ownedByBlue.get(0).getId()).clickOn();
			robot.moveTo("#" + ownedByBlack.get(1).getId()).clickOn();
			robot.moveTo("#" + ownedByBlue.get(0).getId()).clickOn();
			robot.moveTo("#" + ownedByBlack.get(1).getId()).clickOn();
			robot.moveTo("#" + ownedByBlack.get(3).getId()).clickOn();
			robot.sleep(10);

			// THEN
			Assertions.assertThat(moves).hasSize(5);
			Assertions.assertThat(done).isTrue();

		}

		@Test
		public void endsAfter5Clicks(final FxRobot robot) {
			// GIVEN
			flux.subscribe();

			// WHEN
			robot.moveTo("#" + ownedByBlack.get(0).getId());
			for (int i = 0; i < 10; i++) {
				robot.clickOn();
			}
			robot.sleep(10);
			// THEN
			Assertions.assertThat(done).isTrue();
			Assertions.assertThat(moves).hasSize(5);

		}
	}

	@Nested
	@DisplayName("when game calls attack")
	public class DoAttack {

		private Mono<Attack> flux;
		private AtomicBoolean done = new AtomicBoolean(false);
		private List<Attack> moves = new ArrayList<>();
		private Territory territorySi1;
		private Territory territorySi3;
		private Territory territorySi2;
		private Territory territoryAh4;
		private Territory territoryAh3;

		@Start
		public void start(final Stage stage) {
			PlayGamePlayerTest.this.start(stage);

			territorySi1 = game.state().lookupTerritory("si1");
			EngineTestUtil.update(game.state(), notifier, Moves.claim(territorySi1, PLAYER_BLACK));
			EngineTestUtil.update(game.state(), notifier, Moves.place(territorySi1, PLAYER_BLACK, 5));

			territorySi3 = game.state().lookupTerritory("si3");
			EngineTestUtil.update(game.state(), notifier, Moves.claim(territorySi3, PLAYER_BLUE));
			EngineTestUtil.update(game.state(), notifier, Moves.place(territorySi3, PLAYER_BLUE, 2));

			territorySi2 = game.state().lookupTerritory("si2");
			EngineTestUtil.update(game.state(), notifier, Moves.claim(territorySi2, PLAYER_BLACK));
			EngineTestUtil.update(game.state(), notifier, Moves.place(territorySi2, PLAYER_BLACK, 2));

			territoryAh4 = game.state().lookupTerritory("ah4");
			EngineTestUtil.update(game.state(), notifier, Moves.claim(territoryAh4, PLAYER_BLACK));
			EngineTestUtil.update(game.state(), notifier, Moves.place(territoryAh4, PLAYER_BLACK, 2));

			territoryAh3 = game.state().lookupTerritory("ah3");
			EngineTestUtil.update(game.state(), notifier, Moves.claim(territoryAh3, PLAYER_BLACK));
			EngineTestUtil.update(game.state(), notifier, Moves.place(territoryAh3, PLAYER_BLACK, 1));

			flux = playGame.attack(game.state(), PLAYER_BLACK).doOnNext(moves::add)
					.doOnNext(m -> EngineTestUtil.update(game.state(), notifier, m))
					.doAfterTerminate(() -> done.set(true));
		}

		@Test
		public void notifiesPlayer(final FxRobot robot) {
			// GIVEN
			flux.subscribe();

			// WHEN
			robot.sleep(10);

			// THEN
			FxAssert.verifyThat("#messageField", LabeledMatchers.hasText("Make an attack"));
		}

		@Test
		public void completesOnDone(final FxRobot robot) {
			// GIVEN
			flux.subscribe();

			// THEN
			robot.clickOn("Done").sleep(10);

			Assertions.assertThat(moves).hasSize(0);
			Assertions.assertThat(done).isTrue();
		}

		@Test
		public void remembersPreviousCountry(final FxRobot robot) {
			// GIVEN
			flux.subscribe();

			// THEN
			robot.moveTo("#" + territorySi1.getId()).clickOn();
			robot.moveTo("#" + territorySi3.getId()).clickOn();

			Assertions.assertThat(moves).hasSize(1);
			FxAssert.verifyThat("#si1", isSelected());
			
			System.err.println("Going for the second round");
			
			// WHEN 
			playGame.attack(game.state(), PLAYER_BLACK)
				.subscribe(moves::add);
			robot.moveTo("#si3").clickOn().sleep(10);
			
			Assertions.assertThat(moves).hasSize(2);
		}
		
		@Test
		public void clearsSelectionWhenDone(final FxRobot robot) {
			// GIVEN
			flux.subscribe();

			// THEN
			robot.moveTo("#" + territorySi1.getId()).clickOn();
			robot.moveTo("#" + territorySi3.getId()).clickOn();

			Assertions.assertThat(moves).hasSize(1);
			
			
			System.err.println("Going for the second round");
			
			// WHEN 
			playGame.attack(game.state(), PLAYER_BLACK)
				.subscribe(moves::add);
			robot.moveTo("#si3").clickOn().sleep(10);
			
			Assertions.assertThat(moves).hasSize(2);
			
			// WHEN 
			playGame.attack(game.state(), PLAYER_BLACK)
			.subscribe(moves::add);
			robot.clickOn("#actionBtn").sleep(10);
			FxAssert.verifyThat("#si1", isNotSelected());
		}

		@Test
		public void clickOnOwnedCountryChangesSource(final FxRobot robot) {
			// GIVEN
			flux.subscribe();

			// WHEN
			robot.moveTo("#si1").clickOn().sleep(10);
			FxAssert.verifyThat("#si1", isSelected());

			// WHEN
			robot.moveTo("#si2").clickOn().sleep(10);
			FxAssert.verifyThat("#si1", isNotSelected());
			FxAssert.verifyThat("#si2", isSelected());
			Assertions.assertThat(moves).hasSize(0);

			// WHEN
			robot.moveTo("#si3").clickOn().sleep(10);
			FxAssert.verifyThat("#si2", isSelected());
			Assertions.assertThat(moves).hasSize(1);

			final Attack attack = moves.get(0);
			Assertions.assertThat(attack.territory()).isEqualTo(territorySi2);
			Assertions.assertThat(attack.to).isEqualTo(territorySi3);
		}

		@Test
		public void cannotAttackCountryYouOwn(final FxRobot robot) {
			// GIVEN
			flux.subscribe();

			// THEN
			robot.moveTo("#" + territorySi1.getId()).clickOn().sleep(10);
			FxAssert.verifyThat("#si1", isSelected());

			robot.moveTo("#" + territorySi2.getId()).clickOn();
			robot.sleep(10);

			Assertions.assertThat(moves).hasSize(0);
		}

		@Test
		public void cannotAttackCountryThatDoesNotNeighborSource(final FxRobot robot) {
			// GIVEN
			flux.subscribe();

			// THEN
			robot.moveTo("#" + territorySi1.getId()).clickOn();
			robot.moveTo("#" + territoryAh4.getId()).clickOn();
			robot.sleep(10);

			Assertions.assertThat(moves).hasSize(0);
		}

		@Test
		public void canChangeYourMind(final FxRobot robot) {
			// GIVEN
			flux.subscribe();

			// THEN
			robot.moveTo("#" + territoryAh4.getId()).clickOn().sleep(10);
			FxAssert.verifyThat("#ah4", isSelected());
			robot.moveTo("#" + territoryAh4.getId()).clickOn().sleep(10);
			FxAssert.verifyThat("#ah4", isNotSelected());

			robot.moveTo("#" + territorySi1.getId()).clickOn();
			robot.moveTo("#" + territorySi3.getId()).clickOn();

			Assertions.assertThat(moves).hasSize(1);
		}

		@Test
		public void attack(final FxRobot robot) {
			// GIVEN
			flux.subscribe();

			// THEN
			robot.moveTo("#" + territorySi1.getId()).clickOn();
			robot.moveTo("#" + territorySi3.getId()).clickOn();

			Assertions.assertThat(moves).hasSize(1);
		}

		@Test
		public void cannotStartFromTerritoryYouDontOwn(final FxRobot robot) {
			// GIVEN
			flux.subscribe();

			// THEN
			robot.moveTo("#" + territorySi3.getId()).clickOn().sleep(10);
			FxAssert.verifyThat("#si3", isNotSelected());

			Assertions.assertThat(moves).hasSize(0);
		}

		@Test
		public void cannotStartFromTerritoryWith1Army(final FxRobot robot) {
			// GIVEN
			flux.subscribe();

			// THEN
			robot.moveTo("#" + territoryAh3.getId()).clickOn().sleep(10);
			FxAssert.verifyThat("#ah3", isNotSelected());

			Assertions.assertThat(moves).hasSize(0);
		}

//		@Test public void pressAndHold(final FxRobot robot) {
//			// GIVEN 
//			flux.subscribe();
//			
//			// WHEN 
//			robot.moveTo("#" + ownedByBlack.get(0).getId());
//			robot.press(MouseButton.PRIMARY).sleep(6 * 100).release(MouseButton.PRIMARY).sleep(10);
//			
//			// THEN 
//			Assertions.assertThat(moves).hasSize(5);
//			Assertions.assertThat(done).isTrue();
//			
//		}
//		
//		@Test public void assignAllInTerritory(final FxRobot robot) {
//			// GIVEN 
//			flux.subscribe();
//			
//			// WHEN 
//			robot.moveTo("#" + ownedByBlack.get(0).getId());
//			for (int i = 0; i < 5; i++) {
//				robot.clickOn();
//			}
//			robot.sleep(10);
//			
//			// THEN 
//			Assertions.assertThat(done).isTrue();
//			Assertions.assertThat(moves).hasSize(5);
//		}
//
//		@Test public void ignoresClicksOnOwnedTerritories(final FxRobot robot) {
//			// GIVEN 
//			flux.subscribe();
//			
//			// WHEN 
//			robot.moveTo("#" + ownedByBlack.get(0).getId()).clickOn();
//			robot.moveTo("#" + ownedByBlack.get(1).getId()).clickOn();
//			robot.moveTo("#" + ownedByBlue.get(0).getId()).clickOn();
//			robot.moveTo("#" + ownedByBlack.get(1).getId()).clickOn();
//			robot.moveTo("#" + ownedByBlue.get(0).getId()).clickOn();
//			robot.moveTo("#" + ownedByBlack.get(1).getId()).clickOn();
//			robot.moveTo("#" + ownedByBlack.get(3).getId()).clickOn();
//			robot.sleep(10);
//			
//			// THEN 
//			Assertions.assertThat(moves).hasSize(5);
//			Assertions.assertThat(done).isTrue();
//			
//		}
//		
//		@Test public void endsAfter5Clicks(final FxRobot robot) {
//			// GIVEN 
//			flux.subscribe();
//			
//			// WHEN 
//			robot.moveTo("#" + ownedByBlack.get(0).getId());
//			for (int i = 0; i < 10; i++) {
//				robot.clickOn();
//			}
//			robot.sleep(10);
//			// THEN 
//			Assertions.assertThat(done).isTrue();
//			Assertions.assertThat(moves).hasSize(5);
//		
//		}
	}

	@Nested
	@DisplayName("when game calls reinforce")
	public class DoReinforce {

		private Flux<Reinforce> flux;
		private AtomicBoolean done = new AtomicBoolean(false);
		private List<Reinforce> moves = new ArrayList<>();
		private Territory territorySi1;
		private Territory territorySi3;
		private Territory territorySi2;
		private Territory territoryAh4;
		private Territory territoryAh3;

		@Start
		public void start(final Stage stage) {
			PlayGamePlayerTest.this.start(stage);

			territorySi1 = game.state().lookupTerritory("si1");
			EngineTestUtil.update(game.state(), notifier, Moves.claim(territorySi1, PLAYER_BLACK));
			EngineTestUtil.update(game.state(), notifier, Moves.place(territorySi1, PLAYER_BLACK, 5));

			territorySi3 = game.state().lookupTerritory("si3");
			EngineTestUtil.update(game.state(), notifier, Moves.claim(territorySi3, PLAYER_BLUE));
			EngineTestUtil.update(game.state(), notifier, Moves.place(territorySi3, PLAYER_BLUE, 2));

			territorySi2 = game.state().lookupTerritory("si2");
			EngineTestUtil.update(game.state(), notifier, Moves.claim(territorySi2, PLAYER_BLACK));
			EngineTestUtil.update(game.state(), notifier, Moves.place(territorySi2, PLAYER_BLACK, 1));

			territoryAh4 = game.state().lookupTerritory("ah4");
			EngineTestUtil.update(game.state(), notifier, Moves.claim(territoryAh4, PLAYER_BLACK));
			EngineTestUtil.update(game.state(), notifier, Moves.place(territoryAh4, PLAYER_BLACK, 2));

			territoryAh3 = game.state().lookupTerritory("ah3");
			EngineTestUtil.update(game.state(), notifier, Moves.claim(territoryAh3, PLAYER_BLACK));
			EngineTestUtil.update(game.state(), notifier, Moves.place(territoryAh3, PLAYER_BLACK, 1));

			flux = playGame.reinforce(game.state(), PLAYER_BLACK, territorySi1, territorySi2).doOnNext(moves::add)
					.doOnNext(m -> EngineTestUtil.update(game.state(), notifier, m))
					.doAfterTerminate(() -> done.set(true));
		}

		@Test
		public void notifiesPlayer(final FxRobot robot) {
			// GIVEN
			flux.subscribe();

			// WHEN
			robot.sleep(10);

			// THEN
			FxAssert.verifyThat("#messageField", LabeledMatchers.hasText("Reinforce si2 from si1"));
		}

		@Test
		public void completesOnDone(final FxRobot robot) {
			// GIVEN
			flux.subscribe();

			// THEN
			robot.clickOn("Done").sleep(10);

			Assertions.assertThat(moves).hasSize(0);
			Assertions.assertThat(done).isTrue();
		}
	}

	@Nested
	@DisplayName("when game calls fortify")
	public class DoFortify {

		private Flux<Fortify> flux;
		private AtomicBoolean done = new AtomicBoolean(false);
		private List<Fortify> moves = new ArrayList<>();
		private Territory territorySi1;
		private Territory territorySi3;
		private Territory territorySi2;
		private Territory territoryAh4;
		private Territory territoryAh3;

		@Start
		public void start(final Stage stage) {
			PlayGamePlayerTest.this.start(stage);

			territorySi1 = game.state().lookupTerritory("si1");
			EngineTestUtil.update(game.state(), notifier, Moves.claim(territorySi1, PLAYER_BLACK));
			EngineTestUtil.update(game.state(), notifier, Moves.place(territorySi1, PLAYER_BLACK, 5));

			territorySi3 = game.state().lookupTerritory("si3");
			EngineTestUtil.update(game.state(), notifier, Moves.claim(territorySi3, PLAYER_BLUE));
			EngineTestUtil.update(game.state(), notifier, Moves.place(territorySi3, PLAYER_BLUE, 2));

			territorySi2 = game.state().lookupTerritory("si2");
			EngineTestUtil.update(game.state(), notifier, Moves.claim(territorySi2, PLAYER_BLACK));
			EngineTestUtil.update(game.state(), notifier, Moves.place(territorySi2, PLAYER_BLACK, 2));

			territoryAh4 = game.state().lookupTerritory("ah4");
			EngineTestUtil.update(game.state(), notifier, Moves.claim(territoryAh4, PLAYER_BLACK));
			EngineTestUtil.update(game.state(), notifier, Moves.place(territoryAh4, PLAYER_BLACK, 2));

			territoryAh3 = game.state().lookupTerritory("ah3");
			EngineTestUtil.update(game.state(), notifier, Moves.claim(territoryAh3, PLAYER_BLACK));
			EngineTestUtil.update(game.state(), notifier, Moves.place(territoryAh3, PLAYER_BLACK, 1));

			flux = playGame.fortify(game.state(), PLAYER_BLACK).doOnNext(moves::add)
					.doOnNext(m -> EngineTestUtil.update(game.state(), notifier, m))
					.doAfterTerminate(() -> done.set(true));
		}

		@Test
		public void notifiesPlayer(final FxRobot robot) {
			// GIVEN
			flux.subscribe();

			// WHEN
			robot.sleep(10);

			// THEN
			FxAssert.verifyThat("#messageField", LabeledMatchers.hasText("Fortify your position"));
		}

		@Test
		public void completesOnDone(final FxRobot robot) {
			// GIVEN
			flux.subscribe();

			// THEN
			robot.clickOn("Done").sleep(10);

			Assertions.assertThat(moves).hasSize(0);
			Assertions.assertThat(done).isTrue();
		}

//		@Test
//		public void cannotAttackCountryYouOwn(final FxRobot robot) {
//			// GIVEN
//			flux.subscribe();
//
//			// THEN
//			robot.moveTo("#" + territorySi1.getId()).clickOn().sleep(10);
//			FxAssert.verifyThat("#si1", isSelected());
//
//			robot.moveTo("#" + territorySi2.getId()).clickOn();
//			robot.sleep(10);
//
//			Assertions.assertThat(moves).hasSize(0);
//		}
//
//		@Test
//		public void cannotAttackCountryThatDoesNotNeighborSource(final FxRobot robot) {
//			// GIVEN
//			flux.subscribe();
//
//			// THEN
//			robot.moveTo("#" + territorySi1.getId()).clickOn();
//			robot.moveTo("#" + territoryAh4.getId()).clickOn();
//			robot.sleep(10);
//
//			Assertions.assertThat(moves).hasSize(0);
//		}
//
		@Test
		public void foritfy(final FxRobot robot) {
			// GIVEN
			flux.subscribe();

			Assumptions.assumeThat(territorySi1.getArmies()).isEqualTo(5);
			Assumptions.assumeThat(territorySi2.getArmies()).isEqualTo(2);

			// THEN
			robot.moveTo("#" + territorySi1.getId()).clickOn().sleep(10);
			FxAssert.verifyThat("#si1", isSelected());

			robot.moveTo("#" + territorySi2.getId()).clickOn().clickOn().clickOn().clickOn().clickOn().clickOn();

			robot.moveTo("#" + territorySi1.getId()).clickOn().sleep(10);
			FxAssert.verifyThat("#si1", isNotSelected());

			robot.moveTo("#" + territorySi2.getId()).clickOn().sleep(10);
			FxAssert.verifyThat("#si2", isSelected());
			robot.moveTo("#" + territorySi1.getId()).clickOn().clickOn();

			robot.clickOn("Done");

			robot.sleep(10);

			Assertions.assertThat(moves).hasSize(6);
			Assertions.assertThat(territorySi2.getArmies()).isEqualTo(4);

			Assertions.assertThat(territorySi1.getArmies()).isEqualTo(3);

		}

		@Test
		public void canChangeYourMind(final FxRobot robot) {
			// GIVEN
			flux.subscribe();

			// THEN
			robot.moveTo("#" + territorySi1.getId()).clickOn().sleep(10);
			FxAssert.verifyThat("#si1", isSelected());
			robot.moveTo("#" + territorySi1.getId()).clickOn().sleep(10);
			FxAssert.verifyThat("#si1", isNotSelected());

			robot.moveTo("#" + territorySi1.getId()).clickOn().sleep(10);
			robot.moveTo("#" + territorySi2.getId()).clickOn().clickOn().clickOn();

			Assertions.assertThat(moves).hasSize(3);
		}

		@Test
		public void cannotStartFromTerritoryYouDontOwn(final FxRobot robot) {
			// GIVEN
			flux.subscribe();

			// THEN
			robot.moveTo("#" + territorySi3.getId()).clickOn().sleep(10);
			FxAssert.verifyThat("#si3", isNotSelected());

			Assertions.assertThat(moves).hasSize(0);
		}

		@Test
		public void cannotStartFromTerritoryWith1Army(final FxRobot robot) {
			// GIVEN
			flux.subscribe();

			// THEN
			robot.moveTo("#" + territoryAh3.getId()).clickOn().sleep(10);
			FxAssert.verifyThat("#ah3", isNotSelected());

			Assertions.assertThat(moves).hasSize(0);
		}

	}

	@Nested
	@DisplayName("when game calls beginTurn")
	public class OnBeginTurn {

		private Mono<Void> mono;

		@Start
		public void start(final Stage stage) {
			PlayGamePlayerTest.this.start(stage);

		}

		@BeforeEach
		public void setup() {
			mono = playGame.beginTurn(game.state(), PLAYER_BLACK, 0);
		}

		@Test
		public void notifiesUser(final FxRobot robot) {
			// GIVEN

			// WHEN
			robot.interact(() -> {
				mono.subscribe();
			});

			// THEN
			FxAssert.verifyThat("#actionBtn", NodeMatchers.isEnabled());
			FxAssert.verifyThat("#messageField", LabeledMatchers.hasText("Begin turn 0 for Black"));
		}

		@Test
		public void completesOnClick(final FxRobot robot) {
			// GIVEN
			final CompletableFuture<Void> future = new CompletableFuture<>();

			mono.doOnError(future::completeExceptionally);

			// WHEN
			robot.interact(() -> {
				mono.doAfterTerminate(() -> future.complete(null)).

						subscribe();
			});

			// THEN
			Assertions.assertThat(future).isNotCompleted();

			robot.clickOn("#actionBtn").sleep(100);

			Assertions.assertThat(future).isCompleted();
		}
	}

	@Nested @DisplayName("when requested to play cards")
	public class ShowTurnIn {
		private final List<TurnInCards> moves = new ArrayList<>();
		private final AtomicBoolean done = new AtomicBoolean(false);
		
		@Start public void start(final Stage stage) {
			PlayGamePlayerTest.this.start(stage);

		}
		
		@Test public void turnInCardsWithoutMatchingSet(final FxRobot robot) {
			// GIVEN 
			
			// WHEN
			playGame.turnInCards(game.state(), PLAYER_BLACK, false)
				.doOnNext(moves::add)
				.doAfterTerminate(() -> done.set(true))
				.subscribe();
			robot.sleep(500);
			
			// THEN
			FxAssert.verifyThat(".hand-view", NodeMatchers.isVisible());
			
			// WHEN 
			robot.sleep(100);
			robot.clickOn("#hideButton").sleep(500);
			
			// THEN 
			Assertions.assertThat(done).isTrue();
			Assertions.assertThat(moves).hasSize(0);
		}
		
		@Test public void turnInCardsWhenPossible(final FxRobot robot) {
			// GIVEN 
			EngineTestUtil.update(game.state(), notifier, Moves.dealCard(gameFixture.playerBlack, gameFixture.wildcard1));
			EngineTestUtil.update(game.state(), notifier, Moves.dealCard(gameFixture.playerBlack, gameFixture.infantrySi1));
			EngineTestUtil.update(game.state(), notifier, Moves.dealCard(gameFixture.playerBlack, gameFixture.infantrySi2));
			EngineTestUtil.update(game.state(), notifier, Moves.dealCard(gameFixture.playerBlack, gameFixture.artilleryS1));
			
			// WHEN
			playGame.turnInCards(game.state(), PLAYER_BLACK, false)
				.doOnNext(moves::add)
				.doAfterTerminate(() -> done.set(true))
				.subscribe();
			robot.sleep(500);
			
			// THEN
			FxAssert.verifyThat(".hand-view", NodeMatchers.isVisible());
			
			
			// WHEN 
			robot.clickOn("#wildcard1").clickOn("#infantrysi1").clickOn("#infantrysi2");
			robot.sleep(1000);
			robot.clickOn("Turn In").sleep(500);
			
			// THEN 
			Assertions.assertThat(done).isTrue();
			Assertions.assertThat(moves).hasSize(1);
		}
		
		@Test public void turnInCardsWhenRequired(final FxRobot robot) {
			// GIVEN 
			EngineTestUtil.update(game.state(), notifier, Moves.dealCard(gameFixture.playerBlack, gameFixture.wildcard1));
			EngineTestUtil.update(game.state(), notifier, Moves.dealCard(gameFixture.playerBlack, gameFixture.infantrySi1));
			EngineTestUtil.update(game.state(), notifier, Moves.dealCard(gameFixture.playerBlack, gameFixture.infantrySi2));
			EngineTestUtil.update(game.state(), notifier, Moves.dealCard(gameFixture.playerBlack, gameFixture.artilleryS1));
			
			// WHEN
			playGame.turnInCards(game.state(), PLAYER_BLACK, true)
				.doOnNext(moves::add)
				.doAfterTerminate(() -> done.set(true))
				.subscribe();
			robot.sleep(500);
			
			// THEN
			FxAssert.verifyThat(".hand-view", NodeMatchers.isVisible());
			FxAssert.verifyThat("#hideButton", NodeMatchers.isDisabled());
			
			// WHEN 
			robot.clickOn("#wildcard1").clickOn("#infantrysi1").clickOn("#infantrysi2");
			robot.sleep(1000);
			robot.clickOn("Turn In").sleep(500);
			
			// THEN 
			Assertions.assertThat(done).isTrue();
			Assertions.assertThat(moves).hasSize(1);
		}
	}
	
	
	public void autoAssignAll(Collection<Territory> territories, List<Player> allPlayers) {
		int i = 0;
		for (Territory t : territories) {
			EngineTestUtil.update(game.state(), notifier, Moves.claim(t, allPlayers.get(i)));
			i = (i + 1) % allPlayers.size();
		}
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
