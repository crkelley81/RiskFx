package riskfx.engine.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

import reactor.core.publisher.Flux;
import riskfx.engine.display.Display;
import riskfx.engine.game.BattleCalculator;
import riskfx.engine.game.Game;
import riskfx.engine.game.GameImpl;
import riskfx.engine.game.GameState;
import riskfx.engine.game.GameState.Move;
import riskfx.engine.game.GameState.MoveWithTerritory;
import riskfx.engine.model.Player.PlayerImpl;
import riskfx.util.Try;

public class Moves {

	/**
	 * Create a move to claim an territory.
	 * 
	 * @param territory
	 * @param player
	 * @return
	 */
	public static Claim claim(final Territory territory, final Player player) {
		Objects.requireNonNull(territory);
		Objects.requireNonNull(player);
		if (!Objects.equals(Player.none(), territory.getOwner()))
			throw new IllegalStateException(
					"Cannot claim a territory already owned by %s".formatted(territory.getOwner()));
		return new Claim(territory, player);
	}

	public static final class Claim extends MoveWithTerritory {
		private Claim(final Territory territory, final Player player) {
			super(player, territory);
		}

		@Override
		public final String describe() {
			return "%s claims %s".formatted(player.getDisplayName(), territory.getDisplayName());
		}

		@Override
		protected void apply(GameState state, final Display notifier) {
			territory().setOwner(player());
		}

		@Override
		public boolean isEager() {
			return true;
		}

		@Override
		public Move invert() {
			return new Claim(territory, Player.none());
		}

	}

	/**
	 * Place {@code numArmies} on the {@code territory} owned by {@code player}.
	 * 
	 * @param territory
	 * @param player
	 * @param numArmies
	 * @return
	 */
	public static Place place(final Territory territory, final Player player, final int numArmies) {
		Objects.requireNonNull(territory);
		Objects.requireNonNull(player);
		if (!Objects.equals(territory.getOwner(), player))
			throw new IllegalArgumentException("");
		if (numArmies < 0)
			throw new IllegalArgumentException();

		return new Place(territory, player, numArmies);
	}

	public static final class Place extends MoveWithTerritory {
		private final int numArmies;

		public Place(Territory territory, Player player, int numArmies) {
			super(Objects.requireNonNull(player), Objects.requireNonNull(territory));
			this.numArmies = numArmies;
		}

		@Override
		protected void apply(final GameState state, final Display notifier) {
			territory.setArmies(territory.getArmies() + numArmies);
		}

		@Override
		public String describe() {
			return "%s places %s armies on %s".formatted(player.getId(), numArmies, territory.getId());
		}

		@Override
		public boolean isEager() {
			return true;
		}

		@Override
		public Move invert() {
			return new Place(territory, player, -numArmies);
		}
	}

	/** 
	 * Select {@code territory} as the capital for {@code player}.
	 * 
	 * @param territory the territory
	 * @param player the player
	 * @return a move to select the territory as capital
	 * @throws IllegalStateException if the player already has a capital or does not own the territory
	 */
	public static SelectCapital selectCapital(final Territory territory, final Player player) {
		Objects.requireNonNull(territory);
		Objects.requireNonNull(player);
		if (!territory.isOwnedBy(player))
			throw new IllegalArgumentException("");
		return new SelectCapital(territory, player);
	}

	public static class SelectCapital extends MoveWithTerritory {

		public SelectCapital(Territory territory, Player player) {
			super(player, territory);
		}

		@Override
		protected void apply(final GameState state, final Display notifier) {
			territory.setCapitalFor(player);
			((PlayerImpl) player).setCapital(territory);
		}

		@Override
		public String describe() {
			return "%s selects %s as capital".formatted(player.getDisplayName(), territory.getDisplayName());
		}

		@Override
		public Move invert() {
			throw new UnsupportedOperationException();
		}

	}

	/**
	 * Create a move to attack {@code to} from {@code from}.
	 * 
	 * @param player
	 * @param from
	 * @param to
	 * @return an attack
	 */
	public static Attack attack(final Player player, final Territory from, final Territory to) {
		Objects.requireNonNull(player);
		Objects.requireNonNull(from);
		Objects.requireNonNull(to);
		if (!from.isOwnedBy(player))
			throw new IllegalArgumentException("");
		if (to.isOwnedBy(player))
			throw new IllegalArgumentException("");
		if (!to.isNeighborOf(from))
			throw new IllegalArgumentException("");

		return new Attack(player, from, to);
	}

	public static class Attack extends MoveWithTerritory {

		public final Territory to;

		public Attack(Player player, Territory from, Territory to) {
			super(player, from, to);
			this.to = Objects.requireNonNull(to);
		}

		@Override
		public boolean isEager() {
			return false;
		}

		@Override
		public Move invert() {
			throw new UnsupportedOperationException();
		}

		@Override
		public Flux<Move> moveReplace(final Game game, final GameState state) {
			System.err.println("Attack.moveReplace");
			
			final BattleCalculator calculator = game.as(BattleCalculator.class).orElseThrow(() -> new NoSuchElementException("No BattleCalculator found in game"));
			final riskfx.engine.model.BattleResult result = calculator.battle(state, this);


			((GameImpl) game).playerDidConquer = result.conquer();
			
			return result.asEvents();
		}

		@Override
		protected void apply(final GameState state, final Display notifier) {
			state.setMostRecentAttack(this);
		}

		@Override
		public String describe() {
			return "%s attacks %s from %s".formatted(player.getDisplayName(), to.getDisplayName(),
					territory.getDisplayName());
		}

	}

	/**
	 * 
	 * @param player
	 * @param from
	 * @param to
	 * @param numArmies
	 * @return
	 */
	public static Fortify fortify(final Player player, final Territory from, final Territory to, final int numArmies) {
		Objects.requireNonNull(player);
		Objects.requireNonNull(from);
		Objects.requireNonNull(to);
		if (!from.isOwnedBy(player))
			throw new IllegalArgumentException();
		if (!to.isOwnedBy(player))
			throw new IllegalArgumentException();
		if (!from.isNeighborOf(to))
			throw new IllegalArgumentException();

		return new Fortify(player, from, to, numArmies);
	}

	public static class Fortify extends MoveWithTerritory {
		private final Territory to;
		private final int numArmies;

		public Fortify(Player player, Territory from, Territory to, int numArmies) {
			super(player, from, to);
			this.to = Objects.requireNonNull(to);
			this.numArmies = numArmies;
		}

		@Override
		protected void apply(final GameState state, final Display notifier) {
			territory.setArmies(territory.getArmies() - numArmies);
			to.setArmies(to.getArmies() + numArmies);
		}

		@Override
		public final String describe() {
			return "%s fortifies %s from %s with %s armies".formatted(player.getId(), territory.getId(), to.getId(),
					numArmies);
		}

		@Override
		public Move invert() {
			return new Fortify(player, to, territory, numArmies);
		}

	}

	/**
	 * 
	 * @param player
	 * @param from
	 * @param to
	 * @param numArmies
	 * @return
	 */
	public static Try<Reinforce> reinforce(Player player, Territory from, Territory to, long numArmies) {
		return Try.tryGet(() -> new Reinforce(player, from, to, numArmies));
	}

	public static class Reinforce extends MoveWithTerritory {
		private final Territory to;
		private final long numArmies;

		public Reinforce(Player player, Territory from, Territory to, long numArmies) {
			super(player, from, to);

			if (!from.isOwnedBy(player))
				throw new IllegalArgumentException();
			if (!to.isOwnedBy(player))
				throw new IllegalArgumentException();
			if (from.getArmies() <= 1)
				throw new IllegalArgumentException();
			
			
			this.to = Objects.requireNonNull(to);
			this.numArmies = numArmies;
		}

		@Override
		protected void apply(final GameState state, final Display notifier) {
			territory.setArmies(territory.getArmies() - numArmies);
			to.setArmies(to.getArmies() + numArmies);
		}

		@Override
		public String describe() {
			return "%s moves %s armies from %s to %s".formatted(player.getId(), numArmies, territory.getId(),
					to.getId());
		}

		@Override
		public Move invert() {
			return new Reinforce(player, to, territory, numArmies);
		}

	}

	
	/* package */ static Battle battle(Attack attack, List<Long> attackerResults,
			List<Long> defenderResults) {
		return new Battle(attack.player(), attack.territory(), attack.to, attackerResults, defenderResults);
	}

	public static class Battle extends MoveWithTerritory {

		private final Territory to;
		private final List<Long> attackerResults;
		private final List<Long> defenderResults;
//		private final long attackLosses;
//		private final long defenderLosses;

		public Battle(Player player, Territory territory, Territory to, List<Long> attackerResults,
				List<Long> defenderResults) {
			super(player, territory, to);
			this.to = Objects.requireNonNull(to);
			this.attackerResults = attackerResults;
			this.defenderResults = defenderResults;
		}

		@Override
		public String describe() {
			return "%s battles %s in %s".formatted(player.getDisplayName(), to.getOwner().getDisplayName(),
					to.getDisplayName());
		}

		@Override
		protected void apply(GameState state, final Display notifier) {
// Do nothing
		}

		@Override
		public Move invert() {
			throw new UnsupportedOperationException();
		}

	}

	public static BattleResult battleResult(Attack attack, long attackLosses,
			long defenderLosses) {
		return new BattleResult(attack.player(), attack.territory(), attack.to, attackLosses, defenderLosses);
	}

	public static class BattleResult extends MoveWithTerritory {

		private final Territory to;
		private final Player defender;
		private final long attackerLosses;
		private final long defenderLosses;

		public BattleResult(Player player, Territory territory, Territory to, long attackLosses, long defenderLosses) {
			super(player, territory, to);
			this.to = Objects.requireNonNull(to);
			this.defender = to.getOwner();
			this.attackerLosses = attackLosses;
			this.defenderLosses = defenderLosses;
		}

		public Flux<Move> asEvents() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String describe() {
			return "%s losses %s armies, %s losses %s armies".formatted(player.getDisplayName(), attackerLosses,
					defender.getDisplayName(), defenderLosses);
		}

		@Override
		protected void apply(GameState state, Display notifier) {
			territory.setArmies(territory.getArmies() - attackerLosses);
			to.setArmies(to.getArmies() - defenderLosses);
		}

		@Override
		public Move invert() {
			throw new UnsupportedOperationException();
		}

	}

	public static Conquer conquer(Player player, Territory territory, Territory to) {
		return new Conquer(player, territory, to);
	}

	public static class Conquer extends MoveWithTerritory {

		private final Player defender;
		private final Territory to;

		public Conquer(Player player, Territory territory, Territory to) {
			super(player, territory, to);
			this.to = to;
			this.defender = to.getOwner();
		}

		@Override
		public String describe() {
			return "%s conquers %s from %s".formatted(player.getDisplayName(), to.getDisplayName(),
					defender.getDisplayName());
		}

		@Override
		protected void apply(GameState state, Display notifier) {
			to.setOwner(player);
			to.setArmies(1);
			territory.setArmies(territory.getArmies() - 1);
		}

		@Override
		public Move invert() {
			throw new UnsupportedOperationException();
		}

	}
	
	public static TurnInCards turnInCards(final Player player, final Collection<Card> cards) {
		Objects.requireNonNull(player);
		Objects.requireNonNull(cards);
		if (cards.size() != 3) throw new IllegalArgumentException();
		
		return new TurnInCards(player, cards);
	}
	
	public static final class TurnInCards extends Move {

		private final Collection<Card> cards;

		public TurnInCards(Player player, Collection<Card> cards) {
			super(Objects.requireNonNull(player));
			this.cards = Collections.unmodifiableCollection(new ArrayList<>(cards));
		}

		@Override
		protected void apply(GameState state, Display notifier) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public String describe() {
			return "%s turns in cards".formatted(player.getDisplayName());
		}

		@Override
		public Move invert() {
			throw new UnsupportedOperationException();
		}
		
	}

	public static DealCard dealCard(Player player, Card card) {
		Objects.requireNonNull(player);
		Objects.requireNonNull(card);
		return new DealCard(player, card);
	}
	
	public static final class DealCard extends Move {

		private final Card card;

		public DealCard(Player player, Card card) {
			super(player);
			this.card = card;
		}

		@Override
		protected void apply(GameState state, Display notifier) {
			player.receiveCard(card);
		}

		@Override
		public String describe() {
			return "% receives a card".formatted(player.getDisplayName());
		}

		@Override
		public Move invert() {
			// TODO Auto-generated method stub
			return null;
		}
		
	}
	
	
}
