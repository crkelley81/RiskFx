package riskfx.engine.game;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import riskfx.engine.game.Events.StepComplete;
import riskfx.engine.model.Player;

public final class GameStep {
		private final GamePhase phase;
		private final Player player;
		private final CompletableFuture<Boolean> next = new CompletableFuture<>();
		
		/* package */ GameStep(GamePhase phase, Player player) {
			this.phase = Objects.requireNonNull(phase);
			this.player = Objects.requireNonNull(player);
		}
		
		public final GamePhase phase() {
			return phase; 
		}

		public final Player player() {
			return player;
		}

		public Mono<GameStep> runStep(final GameImpl impl, final GameState state) {
			final Mono<GameStep> next = Mono.fromFuture(this.next)
					.map(notused -> {
						final GameStep step = phase.nextPhase(impl, state, player);
						return step;
					})
					/*.doOnSubscribe(s -> {
						System.err.println("Waiting for events to be be processed");
					})
					.doAfterTerminate(() -> {
						System.err.println("Events processed");
					})*/;
			return wrap(phase.execute(impl, state, player, impl.gamePlayer(player)))
					.doOnNext(evt -> impl.pushEvent(evt))
//					.doAfterTerminate(() -> System.err.println("Game events complete"))
//					.doAfterTerminate(() -> impl.events.tryEmitNext(Events.stepComplete(phase, player, next)))
					.then(next);
		}
		
		private Flux<GameEvent> wrap(final Flux<GameEvent> flux) {
			final Flux<StepComplete> mono = Flux.just(Events.stepComplete(phase, player, next));
			return flux
					/*.doOnSubscribe(s -> {
						System.err.println("STEP %s, %s".formatted(phase, player.getId()));
					})
					.doOnNext(move -> {
						System.err.println("A user move " + move.describe());
					})
					.doOnError(ex -> {
						System.err.println("An error: " + ex.getMessage());
					})
					.doAfterTerminate(() -> {
						System.err.println("STEP MOVES Complete");
					})
//					.map(Events::move)*/
					.concatWith(mono);
		}

		@Override public final String toString() {
			return "GameStep[phase=%s, player=%s]".formatted(phase, player);
		}
		
	}