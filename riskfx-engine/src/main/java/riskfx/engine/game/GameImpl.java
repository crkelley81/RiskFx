package riskfx.engine.game;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.logging.Logger;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import riskfx.engine.GameConfig;
import riskfx.engine.MutableGameConfig;
import riskfx.engine.display.Display;
import riskfx.engine.game.GameState.Move;
import riskfx.engine.game.GameState.ReinforcementCalculator;
import riskfx.engine.model.Continent;
import riskfx.engine.model.Player;
import riskfx.util.role.As;

/**
 * 
 * @author christopher
 *
 */
public final class GameImpl implements Game {
	private static final Logger LOG = Logger.getLogger(GameImpl.class.getName());

	public static Game from(final MutableGameConfig config) {
		return new GameImpl(config);
	}

	private final GameConfig config;
	private final Collection<Object> lookups;
	private GameState state;

	private transient java.util.Map<Player, GamePlayer> gamePlayers = new java.util.HashMap<>();

	private final ReinforcementCalculator reinforceCalculater;

	private transient Sinks.Many<GameStep> pipeline = Sinks.many().unicast().onBackpressureBuffer();
	private transient Sinks.Many<GameEvent> events = Sinks.many().multicast().onBackpressureBuffer();
	
		
	private transient Executor executor;
	private transient Display notifier;
	
	protected boolean playerDidAttack = false;
	public boolean playerDidConquer = false;
	
	private GameImpl(final MutableGameConfig config) {
		this.config = Objects.requireNonNull(config).immutableCopy();
		this.state = config.createInitialState();
		this.reinforceCalculater = (gs, p) -> {
			long troopsToDeploy = gs.map().territories().stream().filter(t -> t.isOwnedBy(p)).count() / 3;
			for (Continent c : gs.map().continents()) {
				if (c.isOwnedBy(p)) {
					troopsToDeploy += c.getBonusArmies();
				}
			}
			return troopsToDeploy;
		};
		this.lookups = Arrays.asList(reinforceCalculater, Objects.requireNonNull(config.getBattleCalculator()));
		events.asFlux().subscribe(this::processEvent);
	}
	
	@Override
	public final String getId() {
		return this.config.getId();
	}

	@Override
	public final String getDisplayName() {
		return this.config.getName();
	}

	@Override
	public final GameState state() {
		return state;
	}

	@Override
	public Flux<GameEvent> events() {
		return events.asFlux();
	}

	@Override
	public final void assignGamePlayer(Player player, GamePlayer gamePlayer) {
		this.gamePlayers.put(player, gamePlayer);
	}
	
	public final GamePlayer gamePlayer(Player player) {
		return this.gamePlayers.get(player);
	}

	@Override
	public final <T> Optional<T> as(Class<T> clazz) {
		return As.as(clazz, lookups);
	}

	@Override
	public final void setNotifier(Display notifier, Executor executor) {
		this.notifier = Display.inExecutor(notifier, executor);
	}

	/* package */ void pushEvent(final GameEvent evt) {
		events.tryEmitNext(evt);
	}
	
	private void processEvent(final GameEvent evt) {
		LOG.info("Processing an event: %s".formatted(evt));
		try {
			evt.apply(this, notifier);
		}
		catch (Throwable ex) {
			System.err.println("Exception processing evt" + ex);
		}
//		events.tryEmitNext(evt);
	}


	/* package*/ void processMove(final Move move) {
		LOG.info("Move: %s".formatted(move.describe()));
		move.moveReplace(this, state)
			.doOnNext(m -> state.update(m, notifier))
//			.doOnNext(m -> events.tryEmitNext(Events.move(m)))
			.subscribe();

	}

	
	

	@Override
	public void start(final Duration stepDelay) {
		pipeline.asFlux()
			.doOnNext(step -> {
				runStep(step)
					.subscribe(next -> pipeline.tryEmitNext(next));
			})
			.subscribe();

		GameStep step = null;
		if (this.state().numUnclaimedTerritories() > 0) {
			step = new GameStep(Phase.CLAIM, state.turnOrder().get(0));
			pipeline.tryEmitNext(step);
		} else {
			pipeline.tryEmitNext(new GameStep(Phase.BEGIN_TURN, state.turnOrder().get(0)));
		}

	}
	
	/* package */ void queueStep(final GameStep step) {
		pipeline.tryEmitNext(step);
	}

	/* package */ Mono<GameStep> runStep(final GameStep step) {
		
		return step.runStep(this, state)
				/* .doOnSubscribe(s -> {
					LOG.info("Running a step: %s on %s".formatted(step, Thread.currentThread()));
				})
				.doOnNext(ge -> {
					System.err.println("Game step: " + ge);
				})*/
				;
	}

	

//	private void notify(final Consumer<Display> action) {
//		if (notifier != null) {
//			executor.execute(() -> {
//				action.accept(notifier);
//			});
//		}
//	}
//
//	public void stepComplete(Phase phase, Player player) {
////		final Step nextStep = phase.nextPhase(this, state, player);
////		pipeline.tryEmitNext(nextStep);
//	}
//
//	private <T extends GameState.Move> Flux<T> withPlayerFlux(final Player player,
//			final BiFunction<GamePlayer, GameState, Flux<T>> action) {
//		return action.apply(gamePlayers.get(player), state).doOnNext(move -> processMove(move));
//	}
//
//	private <T extends GameState.Move> Mono<T> withPlayerMono(final Player player,
//			final BiFunction<GamePlayer, GameState, Mono<T>> action) {
//		return action.apply(gamePlayers.get(player), state).doOnNext(move -> processMove(move));
//	}
//
//	private Mono<Void> doClaimTerritories(final Player player, int numTerritories) {
//		LOG.info("Request %s to claim %s territories at %s".formatted(player.getId(), numTerritories, Instant.now()));
//
//		return withPlayerFlux(player, (gp, s) -> gp.claim(s, player, numTerritories))
//				.doOnError(ex -> {
//			})
//		.doAfterTerminate(() -> {
//			/*int count = state.numUnclaimedTerritories();
//			if (count > 0) {
//				int index = (turnOrder().indexOf(player) + 1) % turnOrder().size();
//				int num = Math.min(count, numTerritories);
//				pipeline.tryEmitNext(() -> {
//					return doClaimTerritories(turnOrder().get(index), num);
//				});
//			} else {
//				turnOrder().forEach(p -> p.setTroopsToDeploy(35));
//
//				pipeline.tryEmitNext(() -> {
//					final Player p = turnOrder().get(0);
//					return doPlaceArmies(p, p.getTroopsToDeploy());
//				});
//			}*/
//		}).then();
//	}
//
//	private Mono<Void> doPlaceArmies(Player player, long l) {
//		LOG.info("Request %s to deploy %s armies".formatted(player.getId(), l));
//		return withPlayerFlux(player, (gp, s) -> gp.place(s, player, (int) l)).doAfterTerminate(() -> {
//			final int nextIdx = (turnOrder().indexOf(player) + 1) % turnOrder().size();
//			final Player next = turnOrder().get(nextIdx);
//			if (next.getTroopsToDeploy() > 0) {
//				/*pipeline.tryEmitNext(() -> {
//					return doPlaceArmies(next, next.getTroopsToDeploy());
//				});*/
//			} else {
//				doTurn(turnOrder().get(0));
//			}
//
//		}).then();
//	}
//
//	private Mono<Void> doDeployArmies(final Player player, final long troopsToDeploy) {
//		LOG.info("Request %s to deploy %s armies".formatted(player.getId(), troopsToDeploy));
//		return withPlayerFlux(player, (gp, s) -> gp.place(s, player, (int)troopsToDeploy)).then();
//	}
//
//	private void doTurn(final Player player) {
//		/*pipeline.tryEmitNext(() -> doBeginTurn(player));
////		pipeline.tryEmitNext(() -> doTurnInCards(player));
//		pipeline.tryEmitNext(() -> doDeployArmies(player, this.reinforceCalculater.calculateReinforcements(state, player)));
//		pipeline.tryEmitNext(() -> doAttack(player));*/
//
//	}
//
//	private Mono<Void> doFinishTurn(Player player) {
//		return Mono.empty();
//	}
//
//	private Mono<Void> doBeginTurn(final Player player) {
//		final GamePlayer gamePlayer = gamePlayers.get(player);
//		this.processEvent(Events.beginTurn(player, 0));
//		return gamePlayer.beginTurn(state, player, 0);
//	}
//	private Mono<Void> nextPlayer(Player player) {
//		final int currentIdx = turnOrder().indexOf(player);
//		final int nextIdx = (currentIdx + 1) % turnOrder().size();
//		final Player nextPlayer = turnOrder().get(nextIdx);
//		this.doTurn(nextPlayer);
//		return Mono.empty();
//	}
//	private Mono<Void> doFortify(Player player) {
//		return withPlayerFlux(player, (gp, s) -> gp.fortify(s, player)).then();
//	}
//	private Mono<Void> doAttack(Player player) {
//		return withPlayerMono(player, (gp, s) -> gp.attack(s, player))
//				.map(a -> true)
//				.switchIfEmpty(Mono.just(false))
//				.doOnNext(attacked -> {
//				/*	
//					if (attacked) {
//						pipeline.tryEmitNext(() -> doAttack(player));
//					} else {
//						LOG.info("Player %s did not attack".formatted(player.getId()));
//						pipeline.tryEmitNext(() -> {
//							return doFortify(player);
//						});
//						pipeline.tryEmitNext(() -> {
//							return doFinishTurn(player);
//						});
//						pipeline.tryEmitNext(() -> {
//							return nextPlayer(player);
//						});
//					}*/
//				}).then();
//	}
//
//


}
