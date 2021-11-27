package riskfx.engine.ai;

import java.time.Duration;
import java.util.Objects;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import riskfx.engine.game.GamePlayer;
import riskfx.engine.game.GameState;
import riskfx.engine.model.Moves.Attack;
import riskfx.engine.model.Moves.Claim;
import riskfx.engine.model.Moves.Fortify;
import riskfx.engine.model.Moves.Place;
import riskfx.engine.model.Moves.Reinforce;
import riskfx.engine.model.Player;
import riskfx.engine.model.Territory;

public class PausingGamePlayer implements GamePlayer {

	private final GamePlayer delegate;
	private final Duration pause;

	public PausingGamePlayer(GamePlayer delegate, Duration pause) {
		this.delegate = Objects.requireNonNull(delegate);
		this.pause = Objects.requireNonNull(pause);
	}

	@Override
	public final Flux<Claim> claim(GameState state, Player player, int numTerritories) {
		return delegate.claim(state, player, numTerritories)
				.delayElements(pause);
	}

	@Override
	public final Flux<Place> place(GameState state, Player player, int troopsToDeploy) {
		return delegate.place(state, player, troopsToDeploy)
				.delayElements(pause);
	}

	@Override
	public final Mono<Attack> attack(GameState state, Player player) {
		return delegate.attack(state, player)
				.delayElement(pause);
	}

	@Override
	public final Flux<Fortify> fortify(GameState state, Player player) {
		return delegate.fortify(state, player)
				.delayElements(pause);
	}

	@Override
	public final Mono<Void> beginTurn(GameState state, Player player, int i) {
		return delegate.beginTurn(state, player, i)
				.delayElement(pause);
	}

	@Override
	public final Flux<Reinforce> reinforce(GameState state, Player player, Territory from, Territory to) {
		return delegate.reinforce(state, player, from, to)
				.delayElements(pause);
	}
}
