package riskfx.engine.game;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;

import reactor.core.publisher.Flux;
import riskfx.engine.MutableGameConfig;
import riskfx.engine.display.Display;
import riskfx.engine.game.GameState.Lookup;
import riskfx.engine.model.Map;
import riskfx.engine.model.Player;
import riskfx.util.role.As;
import riskfx.util.role.Displayable;
import riskfx.util.role.Identifiable;

/**
 * 
 * @author christopher
 *
 */
public interface Game extends Identifiable, Displayable, As {

	
	
	public static Game from(MutableGameConfig gameConfig) {
		return GameImpl.from(gameConfig);
	}


	
	/**
	 * 
	 * @return an unmodifiable set of all {@code Player}s in the game.
	 */
	default public Set<Player> allPlayers() {
		return state().allPlayers();
	}
	
	default public List<Player> turnOrder() {
		return state().turnOrder();
	}

	default public Lookup lookup(final String id) {
		return state().lookup(id);
	};
	
	@Deprecated default public Player lookupPlayer(final String id) {
		return lookup(id).player();
	}

	void setNotifier(final Display notifier, final Executor executor);
	
	void assignGamePlayer(Player player, GamePlayer gamePlayer);

	void start(Duration stepDelay);

	default public Map map() {
		return state().map();
	};
	GameState state();

	Flux<GameEvent> events();
}