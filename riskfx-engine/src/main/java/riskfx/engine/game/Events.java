package riskfx.engine.game;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import riskfx.engine.display.Display;
import riskfx.engine.game.GameState.Move;
import riskfx.engine.game.GameState.ReinforcementCalculator;
import riskfx.engine.model.Player;

public class Events {

	public static GameEvent move(Move move) {
		return new MoveEvent(Objects.requireNonNull(move));
	}

	public static final class MoveEvent extends GameEvent {

		private final Move move;

		private MoveEvent(final Move move) {
			this.move = Objects.requireNonNull(move);
		}

		@Override
		protected void apply(Game game, Display notifier) {
//			System.err.println("Submitting a move: " + move.describe());
			((GameImpl) game).processMove(move);
		}

		@Override
		public String describe() {
			return move.describe();
		}

		@Override
		public String toString() {
			return "MoveEvent[%s]".formatted(move.describe());
		}

	}

	public static BeginTurn beginTurn(Player playerBlack, long turn) {
		return new BeginTurn(playerBlack, turn);
	}

	public static class BeginTurn extends GameEvent {
		private final Player player;
		private final long turnNumber;

		public BeginTurn(Player player, long turn) {
			this.player = Objects.requireNonNull(player);
			this.turnNumber = turn;
		}

		@Override
		protected void apply(final Game game, final Display notifier) {
			try {
				final ReinforcementCalculator calc = game.as(ReinforcementCalculator.class).orElseThrow();
				long numArmies = calc.calculateReinforcements(game.state(), player);
				player.setTroopsToDeploy(numArmies);

				System.err.println("WTF? Begin turn for %s with %s armies".formatted(player.getId(), numArmies));

				Optional.ofNullable(notifier).ifPresent(n -> n.onBeginTurn(player, 0, turnNumber));

			} catch (Throwable ex) {
				ex.printStackTrace();
			}
		}

		@Override
		public String describe() {
			return "%s begins turn %s".formatted(player.getId(), turnNumber);
		}

		@Override
		public final String toString() {
			return "BeginTurn[%s, %s]".formatted(player, turnNumber);
		}
	}

	public static EndTurn endTurn(Player playerBlack, long turnNumber) {
		return new EndTurn(playerBlack, turnNumber);
	}

	public static class EndTurn extends GameEvent {

		private final Player player;
		private final long turnNumber;

		private EndTurn(Player player, long turn) {
			this.player = Objects.requireNonNull(player);
			this.turnNumber = turn;
		}

		@Override
		protected void apply(final Game state, final Display notifier) {
			notifier.onEndTurn(player, -1, turnNumber);
		}

		@Override
		public String describe() {
			return "%s ends turn %s".formatted(player.getId(), turnNumber);
		}

	}

	public static StepComplete stepComplete(GamePhase phase, Player player, CompletableFuture<Boolean> next) {
		return new StepComplete(phase, player, next);
	}

	public static class StepComplete extends GameEvent {

		private GamePhase phase;
		private Player player;
		private CompletableFuture<Boolean> future;

		public StepComplete(GamePhase phase, Player player, CompletableFuture<Boolean> next) {
			this.phase = phase;
			this.player = player;
			this.future = next;
		}

		@Override
		protected void apply(Game game, Display notifier) {
			future.complete(true);
		}

		@Override
		public String describe() {
			return "%s completes %s".formatted(player.getId(), phase);
		}
		
		@Override public final String toString() {
			return "StepComplete[%s, %s]".formatted(phase, player.getId());
		}

	}

}
