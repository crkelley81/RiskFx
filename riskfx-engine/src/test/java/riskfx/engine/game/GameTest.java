package riskfx.engine.game;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import riskfx.engine.display.Display;
import riskfx.engine.game.GameState.Move;
import riskfx.engine.model.BattleResult;
import riskfx.engine.model.Moves;
import riskfx.engine.model.Moves.Attack;
import riskfx.engine.model.Player;

class GameTest {
	
	@Nested
	@DisplayName("classic risk turn phase")
	public class ClassicRiskTurn {
		private BigEuropeGameFixture fixture;
		private GameImpl game;
		private GameState state;
		private GamePlayer gamePlayer;
		private Display notifier = Mockito.mock(Display.class);
		private Player player1;
		private GameStep nextStep;
		private Player player2;
		private BattleCalculator calculator;

		private static final Duration TIMEOUT = Duration.ofMillis(100);

		@BeforeEach
		public void setup() {
			calculator = Mockito.mock(BattleCalculator.class);
			Objects.requireNonNull(calculator);

			fixture = new BigEuropeGameFixture(75, true, true, calculator);

			game = (GameImpl) fixture.game;
			state = fixture.game.state();
			player1 = game.state().turnOrder().get(0);
			player2 = game.state().turnOrder().get(1);

			fixture.autoAssignAll();
			fixture.autoPlaceAll();

			gamePlayer = Mockito.mock(GamePlayer.class);
			game.assignGamePlayer(player1, gamePlayer);
			nextStep = new GameStep(Phase.BEGIN_TURN, player1);

		}

		public <T extends Move> Mono<T> wrap(final Mono<T> mono) {
			return mono.doOnNext(move -> game.state().update(move, notifier));
		}

		@Test
		@Order(1)
		public void classicRiskTurnNoAttack() {
			// GIVEN
			Mockito.when(gamePlayer.beginTurn(state, player1, 0)).thenReturn(Mono.empty());
			Mockito.when(gamePlayer.place(Mockito.eq(state), Mockito.eq(player1), Mockito.anyInt()))
					.thenReturn(Flux.empty());
			Mockito.when(gamePlayer.attack(state, player1)).thenReturn(Mono.empty());
			Mockito.when(gamePlayer.fortify(state, player1)).thenReturn(Flux.empty());

			// WHEN
			nextStep = game.runStep(nextStep).block(TIMEOUT);
			nextStep = game.runStep(nextStep).block(TIMEOUT);
			nextStep = game.runStep(nextStep).block(TIMEOUT);
			nextStep = game.runStep(nextStep).block(TIMEOUT);

			// THEN
			Mockito.verify(gamePlayer).beginTurn(state, player1, 0);
			Mockito.verify(gamePlayer).place(Mockito.eq(state), Mockito.eq(player1), Mockito.anyInt());
			Mockito.verify(gamePlayer).attack(state, player1);
			Mockito.verify(gamePlayer).fortify(state, player1);

			Assertions.assertEquals(Phase.END_TURN, nextStep.phase());
			Assertions.assertEquals(player1, nextStep.player());
		}

		@Test
		@Order(2)
		public void classicRiskTurnWithAttacks() {
			// GIVEN
			Mockito.when(gamePlayer.beginTurn(state, player1, 0)).thenReturn(Mono.empty());
			Mockito.when(gamePlayer.place(Mockito.eq(state), Mockito.eq(player1), Mockito.anyInt()))
					.thenReturn(Flux.empty());

			// WHEN
			nextStep = game.runStep(nextStep).block();
			nextStep = game.runStep(nextStep).block();

			// WHEN
			EngineTestUtil.territory(state, "si1", player1, 10);
			EngineTestUtil.territory(state, "si2", player2, 3);

			Attack attack = Moves.attack(player1, fixture.territorySi1, fixture.territorySi2);
			BattleResult result = new BattleResult(attack, false, List.of(), List.of(),2, 0);
			Mockito.when(gamePlayer.attack(state, player1)).thenReturn(Mono.just(attack));
			Mockito.when(calculator.battle(state, attack)).thenReturn(result);
			nextStep = game.runStep(nextStep).block();

			// THEN
			Mockito.verify(gamePlayer).attack(state, player1);
			Mockito.verify(calculator).battle(state, attack);

			Assertions.assertEquals(Phase.ATTACK, nextStep.phase());
			Assertions.assertEquals(player1, nextStep.player());
			Assertions.assertEquals(8, fixture.territorySi1.getArmies());

			attack = Moves.attack(player1, fixture.territorySi1, fixture.territorySi2);
			result = new BattleResult(attack, false, List.of(), List.of(),0, 2);
			Mockito.when(gamePlayer.attack(state, player1)).thenReturn(Mono.just(attack));
			Mockito.when(calculator.battle(state, attack)).thenReturn(result);
			nextStep = game.runStep(nextStep).block();

			Assertions.assertEquals(Phase.ATTACK, nextStep.phase());
			Assertions.assertEquals(player1, nextStep.player());
			Assertions.assertEquals(1, fixture.territorySi2.getArmies());

			attack = Moves.attack(player1, fixture.territorySi1, fixture.territorySi2);
			result = new BattleResult(attack, true,List.of(), List.of(), 0, 1);
			Mockito.when(gamePlayer.attack(state, player1)).thenReturn(Mono.just(attack));
			Mockito.when(calculator.battle(state, attack)).thenReturn(result);
			nextStep = game.runStep(nextStep).block(TIMEOUT);

			Assertions.assertEquals(1, fixture.territorySi2.getArmies());
			Assertions.assertEquals(7, fixture.territorySi1.getArmies());
			Assertions.assertEquals(player1, fixture.territorySi2.getOwner());
			Assertions.assertEquals(Phase.REINFORCE, nextStep.phase());
			Assertions.assertEquals(player1, nextStep.player());

			// WHEN 
			Mockito.when(gamePlayer.reinforce(state, player1, fixture.territorySi1, fixture.territorySi2)).thenReturn(Flux.empty());
			nextStep = game.runStep(nextStep).block(TIMEOUT);
			Mockito.verify(gamePlayer).reinforce(state, player1, fixture.territorySi1, fixture.territorySi2);
			
			// WHEN
			Mockito.when(gamePlayer.attack(state, player1)).thenReturn(Mono.empty());
			nextStep = game.runStep(nextStep).block();
			
//			Mockito.verify(gamePlayer).attack(state, player1);
			Assertions.assertEquals(Phase.FORTIFY, nextStep.phase());
			Assertions.assertEquals(player1, nextStep.player());

			// WHEN
			Mockito.when(gamePlayer.fortify(state, player1)).thenReturn(Flux.empty());
			nextStep = game.runStep(nextStep).block();

			Assertions.assertEquals(Phase.END_TURN, nextStep.phase());
			Assertions.assertEquals(player1, nextStep.player());

		}
		
		@Test
		@Order(3)
		public void classicRiskTurnWithAttacksFull() {
			// GIVEN
			EngineTestUtil.territory(state, "si1", player1, 10);
			EngineTestUtil.territory(state, "si2", player2, 3);
			
			final Attack[] attacks = {
					Moves.attack(player1, fixture.territorySi1, fixture.territorySi2),
					Moves.attack(player1, fixture.territorySi1, fixture.territorySi2),
					Moves.attack(player1, fixture.territorySi1, fixture.territorySi2)
			};
			
			final BattleResult[] results = {
					new BattleResult(attacks[0], false, List.of(), List.of(),2, 0),
					new BattleResult(attacks[1], false, List.of(), List.of(),0, 2),
					new BattleResult(attacks[2], true, List.of(), List.of(), 0, 1)
			};
			
			
			Mockito.when(gamePlayer.beginTurn(state, player1, 0)).thenReturn(Mono.empty());
			Mockito.when(gamePlayer.place(Mockito.eq(state), Mockito.eq(player1), Mockito.anyInt()))
					.thenReturn(Flux.empty());
			Mockito.when(gamePlayer.attack(state, player1)).thenReturn(
					Mono.just(attacks[0]).doOnSubscribe(s -> System.err.println("Attack 0")),
					Mono.just(attacks[1]).doOnSubscribe(s -> System.err.println("Attack 1")),
					Mono.just(attacks[2]).doOnSubscribe(s -> System.err.println("Attack 2")),
					Mono.empty()
					
					);
//			Mockito.when(gamePlayer.attack(state, player1)).thenReturn(Mono.just(attacks[1]));
//			Mockito.when(gamePlayer.attack(state, player1)).thenReturn(Mono.just(attacks[2]));
//			Mockito.when(gamePlayer.attack(state, player1)).thenThrow(new IllegalStateException());
			Mockito.when(gamePlayer.reinforce(state, player1, fixture.territorySi1, fixture.territorySi2)).thenReturn(Flux.empty());
			Mockito.when(gamePlayer.fortify(state, player1)).thenReturn(Flux.empty());
			
			Mockito.when(calculator.battle(state, attacks[0])).thenReturn(results[0]);
			Mockito.when(calculator.battle(state, attacks[1])).thenReturn(results[1]);
			Mockito.when(calculator.battle(state, attacks[2])).thenReturn(results[2]);
			
			// WHEN
			game.start(Duration.ofMillis(200));

			// THEN
			Mockito.verify(gamePlayer).beginTurn(state, player1, 0);
			Mockito.verify(gamePlayer).place(Mockito.eq(state), Mockito.eq(player1), Mockito.intThat(m -> m > 0));
			Mockito.verify(gamePlayer, Mockito.times(4)).attack(state, player1);
			Mockito.verify(gamePlayer).reinforce(state, player1, fixture.territorySi1, fixture.territorySi2);
			Mockito.verify(gamePlayer).fortify(state, player1);
			
			Assertions.assertEquals(1, fixture.territorySi2.getArmies());
			Assertions.assertEquals(7, fixture.territorySi1.getArmies());
			Assertions.assertEquals(player1, fixture.territorySi2.getOwner());
			

		}
	}

	@Nested
	@DisplayName("claim phase")
	public class ClaimPhase {
		private BigEuropeGameFixture fixture; 
		private GameImpl game;
		private Player player1;
		private Player player2;
		private GamePlayer gamePlayer = Mockito.mock(GamePlayer.class);

		@BeforeEach
		public void setup() {
			fixture = new BigEuropeGameFixture(75, false, false);
			game = (GameImpl) fixture.game;
			player1 = fixture.game.turnOrder().get(0);
			player2 = fixture.game.turnOrder().get(1);
		}

		@Test
		public void requestsClaim() {
			// GIVEN
			Mockito.when(gamePlayer.claim(game.state(), player1, 5)).thenReturn(Flux.empty());

			// WHEN
			Phase.CLAIM.execute(game, game.state(), player1, gamePlayer).subscribe();

			// THEN
			Mockito.verify(gamePlayer).claim(game.state(), player1, 5);
		}

		@Test
		public void nextStepWhenUnclaimedTerritoriesRemain() {
			// GIVEN
			Assumptions.assumeTrue(game.state().numUnclaimedTerritories() > 0);

			// WHEN
			final GameStep nextStep = Phase.CLAIM.nextPhase(game, game.state(), player1);

			// THEN
			Assertions.assertEquals(Phase.CLAIM, nextStep.phase());
			Assertions.assertEquals(player2, nextStep.player());
		}

		@Test
		public void nextStepWhenAllTerritoriesClaimed() {
			// GIVEN
			EngineTestUtil.randomlyAssignAll(game.state());
			Assumptions.assumeTrue(game.state().numUnclaimedTerritories() == 0);

			// WHEN
			final GameStep nextStep = Phase.CLAIM.nextPhase(game, game.state(), player1);

			// THEN
			Assertions.assertEquals(Phase.PLACE_SETUP, nextStep.phase());
			Assertions.assertEquals(player1, nextStep.player());
		}
	}

	@Nested
	@DisplayName("place during setup phase")
	public class PlaceSetupPhase {
		private BigEuropeGameFixture fixture; 
		private GameImpl game;
		private Player player1;
		private Player player2;
		private GamePlayer gamePlayer = Mockito.mock(GamePlayer.class);

		@BeforeEach
		public void setup() {
			fixture = new BigEuropeGameFixture(75, false, false);
			fixture.autoAssignAll();
			game = (GameImpl) fixture.game;
			player1 = fixture.game.turnOrder().get(0);
			player2 = fixture.game.turnOrder().get(1);

			game.assignGamePlayer(player1, gamePlayer);
		}

		@Test
		public void requestsPlace() {
			Assertions.assertNotNull(gamePlayer);

			// GIVEN
			Mockito.when(gamePlayer.place(Mockito.any(), Mockito.eq(player1), Mockito.anyInt()))
					.thenReturn(Flux.empty());

			// WHEN
			Phase.PLACE_SETUP.execute(game, game.state(), player1, gamePlayer).subscribe();

			// THEN
			Mockito.verify(gamePlayer).place(Mockito.eq(game.state()), Mockito.eq(player1), Mockito.anyInt());
		}

		@Test
		public void nextStepWhenUnclaimedTerritoriesRemain() {
			// GIVEN
			Assumptions.assumeTrue(player2.getTroopsToDeploy() > 0);

			// WHEN
			final GameStep nextStep = Phase.PLACE_SETUP.nextPhase(game, game.state(), player1);

			// THEN
			Assertions.assertEquals(player2, game.state().nextPlayer(player1));
			Assertions.assertEquals(Phase.PLACE_SETUP, nextStep.phase());
			Assertions.assertEquals(player2, nextStep.player());
		}

		@Test
		public void nextStepWhenNoCapitalsOrMissions() {
			// GIVEN
			player2.setTroopsToDeploy(0);
			Assumptions.assumeFalse(game.state().useCapitals());
			Assumptions.assumeFalse(game.state().useMissions());

			// WHEN
			final GameStep nextStep = Phase.PLACE_SETUP.nextPhase(game, game.state(), player1);

			// THEN
			Assertions.assertEquals(Phase.BEGIN_TURN, nextStep.phase());
			Assertions.assertEquals(player1, nextStep.player());
		}

		@Test
		public void nextStepWithCapitals() {
			// GIVEN
			player2.setTroopsToDeploy(0);
			Assumptions.assumeTrue(game.state().useCapitals());

			// WHEN
			final GameStep nextStep = Phase.PLACE_SETUP.nextPhase(game, game.state(), player1);

			// THEN
			Assertions.assertEquals(Phase.SELECT_CAPITAL, nextStep.phase());
			Assertions.assertEquals(player1, nextStep.player());
		}

		@Test
		public void nextStepWithoutCapitalsWithMissions() {
			// GIVEN
			player2.setTroopsToDeploy(0);
			Assumptions.assumeFalse(game.state().useCapitals());
			Assumptions.assumeTrue(game.state().useMissions());

			// WHEN
			final GameStep nextStep = Phase.PLACE_SETUP.nextPhase(game, game.state(), player1);

			// THEN
			Assertions.assertEquals(Phase.RECEIVE_MISSION, nextStep.phase());
			Assertions.assertEquals(player1, nextStep.player());
		}
	}

	public Attack findAttack(GameState state, Player player1) {
		final List<Attack> attacks = state.territoriesOwnedBy(player1)
				.filter(t -> t.getArmies() > 1)
				.flatMap(from -> from.neighborsNotOwnedBy(player1).map(to -> Moves.attack(player1, from, to)))
				.collect(Collectors.toList());
		Collections.sort(attacks, (a, b) -> Long.compare( b.territory().getArmies(), a.territory().getArmies()));
		if (attacks.isEmpty()) throw new IllegalStateException("Could not find valid attacks for " + player1.getId());
		return attacks.get(0);		
	}
}
