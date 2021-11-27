package riskfx.engine.game;

import reactor.core.publisher.Flux;
import riskfx.engine.model.Player;

public interface GamePhase {

	public  Flux<GameEvent> execute(final GameImpl game, final GameState state, final Player player, final GamePlayer gamePlayer);

	public GameStep nextPhase(final GameImpl game, final GameState state, final Player player);
}
