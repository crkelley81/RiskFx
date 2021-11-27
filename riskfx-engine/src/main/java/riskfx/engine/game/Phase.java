package riskfx.engine.game;

import java.util.logging.Logger;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import riskfx.engine.game.GameState.Move;
import riskfx.engine.model.Player;

public enum Phase implements GamePhase {
	
	CLAIM() {

		@Override
		public Flux<GameEvent> execute(GameImpl game, GameState state, Player player, GamePlayer gamePlayer) {
			return gamePlayer.claim(state, player, 5).map(Events::move);
		}
		
		public GameStep nextPhase(final GameImpl game, final GameState state, final Player player) {
			int count = state.numUnclaimedTerritories();
			if (count > 0) {
				int index = (state.turnOrder().indexOf(player) + 1) % state.turnOrder().size();
				int num = Math.min(count, 5);
				return new GameStep(CLAIM, state.turnOrder().get(index));
			} else {
				state.turnOrder().forEach(p -> p.setTroopsToDeploy(35));

				return new GameStep(PLACE_SETUP, state.turnOrder().get(0));
			}
		}
		
	},
	PLACE_SETUP() {

		@Override
		public Flux<GameEvent> execute(GameImpl game, GameState state, Player player, GamePlayer gamePlayer) {
			LOG.info("Request %s to place %s armies".formatted(player.getId(), player.getTroopsToDeploy()));
			return gamePlayer.place(state, player, (int) player.getTroopsToDeploy()).map(Events::move);
		}

		@Override
		public GameStep nextPhase(GameImpl game, GameState state, Player player) {
			final Player nextPlayer = state.nextPlayer(player);
			if (nextPlayer.getTroopsToDeploy() > 0) {
				LOG.info("Player: %s, Next: %s".formatted(player.getId(), nextPlayer.getId()));
				return new GameStep(PLACE_SETUP, nextPlayer);
			}
			else {
				return new GameStep(BEGIN_TURN, state.turnOrder().get(0));
			}
		}
		
	},
	BEGIN_TURN() {

		@Override
		public Flux<GameEvent> execute(GameImpl game, GameState state, Player player, GamePlayer gamePlayer) {
			final Mono<Void> command = gamePlayer.beginTurn(state, player, 0);
			final Mono<GameEvent> mono = command.cast(GameEvent.class);
			
			
			return Flux.just((GameEvent) Events.beginTurn(player, 0))
					.concatWith(mono)
					.doOnSubscribe(s -> {
						System.err.println("WTF?");
					})
					.doOnNext(ge -> {
						System.err.println("Event: " + ge);
					});
					
					
					
//					Flux.just(Events.beginTurn(player, 0))
//					.thenMany(GameUtil.flux(gamePlayer.beginTurn(game.state(), player, 0).map(v -> (Move) null))).map(Events::move);
					
//			return GameUtil.flux(gamePlayer.beginTurn(state, player, (int) state.turnNumber).map(v -> (Move) null));
		}

		@Override
		public GameStep nextPhase(GameImpl game, GameState state, Player player) {
			return new GameStep(PLACE, player);
		}
		
	},
	PLACE() {
		@Override
		public Flux<GameEvent> execute(GameImpl game, GameState state, Player player, GamePlayer gamePlayer) {
			LOG.info("Request %s to place %s armies".formatted(player.getId(), player.getTroopsToDeploy()));
			return gamePlayer.place(state, player, (int) player.getTroopsToDeploy()).map(Events::move);
		}

		@Override
		public GameStep nextPhase(GameImpl game, GameState state, Player player) {
			return new GameStep(ATTACK, player);
		}
	}
	
	,
	ATTACK() {

		@Override
		public  Flux<GameEvent> execute(GameImpl game, GameState state, Player player, final GamePlayer gamePlayer) {
			game.playerDidAttack = false;
			game.playerDidConquer = false;
			return GameUtil.flux(gamePlayer.attack(state, player)
					.doOnSubscribe(s -> {
						System.err.println("Requesting %s to make an attack".formatted(player.getId()));
					})
					.doOnSuccess(attack -> {
						game.playerDidAttack = (attack != null);
						System.err.println("Player " + ((attack != null) ? attack.describe() : " did not attack"));
					}))
					.map(Events::move);
		} 
		
		@Override
		public GameStep nextPhase(GameImpl game, GameState state, Player player) {
			if (game.playerDidConquer) { 
				return new GameStep(REINFORCE, player);
			}
			if (game.playerDidAttack) {
				return new GameStep(ATTACK, player);
			}
			else {
				return new GameStep(FORTIFY, player);
			}
		}
		
	},
	FORTIFY() {

		@Override
		public Flux<GameEvent> execute(GameImpl game, GameState state, Player player, GamePlayer gamePlayer) {
			return gamePlayer.fortify(state, player).map(Events::move);
		}

		@Override
		public GameStep nextPhase(GameImpl game, GameState state, Player player) {
			return new GameStep(END_TURN, player);
		}
		
	}, END_TURN() {

		@Override
		public Flux<GameEvent> execute(GameImpl game, GameState state, Player player, GamePlayer gamePlayer) {
			return Flux.empty();
		}

		@Override
		public GameStep nextPhase(GameImpl game, GameState state, Player player) {
			return new GameStep(BEGIN_TURN, state.nextPlayer(player));
		}}, 
	
	SELECT_CAPITAL {

		@Override
		public Flux<GameEvent> execute(GameImpl game, GameState state, Player player, GamePlayer gamePlayer) {
			return GameUtil.flux(gamePlayer.selectCapital(state, player)).map(Events::move);
		}

		@Override
		public GameStep nextPhase(GameImpl game, GameState state, Player player) {
			final Player nextPlayer = state.nextPlayer(player);
			if (nextPlayer.getCapital().isEmpty()) {
				return new GameStep(SELECT_CAPITAL, player);
			}
			else if (state.useMissions()){
				return new GameStep(RECEIVE_MISSION, nextPlayer);
			}
			else {
				return new GameStep(BEGIN_TURN, nextPlayer);
			}
		}
			
		}, RECEIVE_MISSION {

			@Override
			public 	Flux<GameEvent> execute(GameImpl game, GameState state, Player player, GamePlayer gamePlayer) {
				return Flux.empty();
			}

			@Override
			public 	GameStep nextPhase(GameImpl game, GameState state, Player player) {
				return null;
			}}, REINFORCE {

				@Override
				public Flux<GameEvent> execute(GameImpl game, GameState state, Player player,
						GamePlayer gamePlayer) {
					return gamePlayer.reinforce(state, player, state.getMostRecentAttack().territory(), state.getMostRecentAttack().to).map(Events::move);
				}

				@Override
				public GameStep nextPhase(GameImpl game, GameState state, Player player) {
					return new GameStep(ATTACK, player);
				}};
	
	private static final Logger LOG = Logger.getLogger(Phase.class.getName());
	
}