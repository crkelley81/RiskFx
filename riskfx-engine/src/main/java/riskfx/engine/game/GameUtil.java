package riskfx.engine.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import riskfx.engine.game.GameState.Move;
import riskfx.engine.model.Player;

 class GameUtil {
	 
	 
	public static List<Player> randomize(final Set<Player> allPlayers) {
		final List<Player> players = new ArrayList<>(allPlayers);
		Collections.shuffle(players);
		return Collections.unmodifiableList(players);
	}

	static <T extends Move> Flux<T> flux(final Mono<T> mono) {
		return Flux.from(mono);
	}
	
}
