package riskfx.engine.ai;

import java.time.Duration;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import riskfx.engine.game.GamePlayer;
import riskfx.engine.game.GameState;
import riskfx.engine.model.Moves;
import riskfx.engine.model.Moves.Attack;
import riskfx.engine.model.Moves.Claim;
import riskfx.engine.model.Moves.Fortify;
import riskfx.engine.model.Moves.Place;
import riskfx.engine.model.Moves.Reinforce;
import riskfx.engine.model.Player;
import riskfx.engine.model.Territory;

public class DoesNothingAi implements GamePlayer {
	private final Duration pause = Duration.ofMillis(200);

	@Override
	public Flux<Claim> claim(GameState state, Player player, int numTerritories) {
		return Flux.fromStream(state.unclaimedTerritories())
				.take(numTerritories)
//				.delayElements(pause)
				.map(t -> Moves.claim(t, player))
//				.doOnSubscribe(s -> {
//					System.err.println("Do Nothing AI: claim %s territories".formatted(numTerritories));
//				})
//				.doOnNext(m -> {
//					System.err.println("Do Nothing AI: " + m.describe());
//				})
				;
	}

	@Override
	public Flux<Place> place(GameState state, Player player, int troopsToDeploy) {
		final Territory terr = state.map().territories().stream()
		.filter(t -> t.isOwnedBy(player))
		.sorted((t1, t2) -> (int) (t1.getArmies() - t2.getArmies()) )
		.findFirst()
		.get();
		return Flux.just(terr).map(t -> Moves.place(t, player, troopsToDeploy))
				;
	}

	@Override
	public Mono<Attack> attack(GameState state, Player player) {
		return Mono.empty();
	}

	@Override
	public Flux<Fortify> fortify(GameState state, Player player) {
		return Flux.empty();
	}

	@Override
	public Mono<Void> beginTurn(GameState state, Player player, int i) {
		return Mono.empty()
				.doOnSubscribe(s -> {
					
				})
				.then();
	}

	@Override
	public Flux<Reinforce> reinforce(GameState state, Player player, Territory from, Territory to) {
		return Flux.empty();
	}
	
}
