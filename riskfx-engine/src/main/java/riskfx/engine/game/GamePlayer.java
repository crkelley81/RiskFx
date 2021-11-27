package riskfx.engine.game;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import riskfx.engine.model.Moves;
import riskfx.engine.model.Moves.Attack;
import riskfx.engine.model.Moves.Claim;
import riskfx.engine.model.Moves.Fortify;
import riskfx.engine.model.Moves.Place;
import riskfx.engine.model.Moves.Reinforce;
import riskfx.engine.model.Moves.SelectCapital;
import riskfx.engine.model.Moves.TurnInCards;
import riskfx.engine.model.Player;
import riskfx.engine.model.Territory;
import riskfx.util.Try;

public interface GamePlayer {

	/**
	 * 
	 * 
	 * Default implementation claims territories randomly.
	 * 
	 * @param state
	 * @param player
	 * @param numTerritories
	 * @return
	 */
	default public Flux<Claim> claim(final GameState state, final Player player, final int numTerritories) {
		return Flux.fromStream(state.unclaimedTerritories())
				.take(numTerritories)
				.map(t -> Moves.claim(t, player));
	}

	default public Flux<Place> place(GameState state, Player player, int troopsToDeploy) {
		final Random random = new Random();
		final List<Territory> territories = state.map().territories().stream().filter(t -> t.isOwnedBy(player)).collect(Collectors.toList());
		return Flux.range(0, troopsToDeploy)
				.map(i -> random.nextInt(0, territories.size()))
				.map(idx -> territories.get(idx))
				.map(t -> Moves.place(t, player, 1))
				.doOnNext(m -> {
					System.err.println("Describe: " + m.describe());
				});
	}

	/**
	 * 
	 * Default implementation does not attack (i.e. returns {@code Mono.empty()}).
	 * 
	 * @param state
	 * @param player
	 * @return
	 */
	default public Mono<Attack> attack(GameState state, Player player) {
		return Mono.empty();
	}

	default Flux<Fortify> fortify(GameState state, Player player) {
		return Flux.empty();
	}

	default public Mono<Void> beginTurn(GameState state, Player player, int i) {
		return Mono.empty();
	}

	default public Flux<Reinforce> reinforce(GameState state, Player player, Territory from, Territory to) {
		return Flux.just(Moves.reinforce(player, from, to, from.getArmies() - 1)).filter(Try::isSuccess).map(Try::get);
	}

	default public Mono<TurnInCards> turnInCards(GameState state, Player player, boolean required) {
		return Mono.empty();
	}

	default public Mono<SelectCapital> selectCapital(GameState state, Player player) {
		return Flux.fromStream(state.territoriesOwnedBy(player)).next().map(t -> Moves.selectCapital(t, player));
	}
	
}
