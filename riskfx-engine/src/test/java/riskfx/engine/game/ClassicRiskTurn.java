package riskfx.engine.game;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

import org.junit.jupiter.api.Assertions;
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
		
//		Mockito.verify(gamePlayer).attack(state, player1);
		Assertions.assertEquals(Phase.FORTIFY, nextStep.phase());
		Assertions.assertEquals(player1, nextStep.player());

		// WHEN
		Mockito.when(gamePlayer.fortify(state, player1)).thenReturn(Flux.empty());
		nextStep = game.runStep(nextStep).block();

		Assertions.assertEquals(Phase.END_TURN, nextStep.phase());
		Assertions.assertEquals(player1, nextStep.player());

	}
}