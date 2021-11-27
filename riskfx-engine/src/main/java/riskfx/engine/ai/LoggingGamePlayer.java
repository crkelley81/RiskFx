package riskfx.engine.ai;

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

public class LoggingGamePlayer implements GamePlayer {
	private final GamePlayer delegate;
	private final String name;
	

	public LoggingGamePlayer(GamePlayer delegate, String name) {
		super();
		this.delegate = delegate;
		this.name = name;
	}
	
	@Override
	public Flux<Claim> claim(GameState state, Player player, int numTerritories) {
		return delegate.claim(state, player, numTerritories)
				.doOnSubscribe(s -> System.err.println(name + ": claim " + numTerritories + " territories"))
				.doOnNext(m -> System.err.println("  " + name + ": " + m.describe()));
	}


	@Override
	public Flux<Place> place(GameState state, Player player, int troopsToDeploy) {
		return delegate.place(state, player, troopsToDeploy)
				.doOnSubscribe(s -> System.err.println(name + ": place " + troopsToDeploy + " armies"))
				.doOnNext(m -> System.err.println("  " + name + ": " + m.describe()));
	}

	@Override
	public Mono<Attack> attack(GameState state, Player player) {
		return delegate.attack(state, player)
				.doOnSubscribe(s -> System.err.println(name + ": Make an attack"))
				.doOnSuccess(attack -> {
					if (attack != null) {
						System.err.println("  " + name + ": " + attack.describe());
					}
					else {
						System.err.println("  " + name + ": Does not attack");
					}
				});
	}

	@Override
	public Flux<Fortify> fortify(GameState state, Player player) {
		// TODO Auto-generated method stub
		return GamePlayer.super.fortify(state, player);
	}

	@Override
	public Mono<Void> beginTurn(GameState state, Player player, int i) {
		return delegate.beginTurn(state, player, i)
				.doOnSubscribe(s -> System.err.println(name + ": Begin turn " + i + " for " + player.getDisplayName()));
	}

	@Override
	public Flux<Reinforce> reinforce(GameState state, Player player, Territory from, Territory to) {
		// TODO Auto-generated method stub
		return GamePlayer.super.reinforce(state, player, from, to);
	}

}
