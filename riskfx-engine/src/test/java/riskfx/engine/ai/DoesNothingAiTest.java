package riskfx.engine.ai;

import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import riskfx.engine.display.Display;
import riskfx.engine.game.EngineTestUtil;
import riskfx.engine.game.Game;
import riskfx.engine.game.GamePlayer;
import riskfx.engine.games.BigEuropeGameConfig;
import riskfx.engine.model.Player;

class DoesNothingAiTest {
	private Display notifier = new Display() {
	};
	private BigEuropeGameConfig gameConfig;
	private Game game;
	private Player player;
	private GamePlayer ai;

	@BeforeEach
	public void setup() {
		gameConfig = new BigEuropeGameConfig();
		game = Game.from(gameConfig);

		player = game.turnOrder().get(0);
		ai = new PausingGamePlayer(new LoggingGamePlayer(new DoesNothingAi(), "Do Nothing AI"), Duration.ofMillis(200));
	}

	@Test
	void claimTerritories() {
		// GIVEN

		// WHEN
		ai.beginTurn(game.state(), player, 0).block();

		
		
		ai.claim(game.state(), player, 5).timed().doOnNext(t -> EngineTestUtil.update(game.state(), notifier, t.get())).blockLast();

		ai.place(game.state(), player, 15)

				.timed().doOnNext(t -> EngineTestUtil.update(game.state(), notifier, t.get())).blockLast();

		ai.attack(game.state(), player).block();
	}

}
