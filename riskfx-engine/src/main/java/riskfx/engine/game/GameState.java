package riskfx.engine.game;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import reactor.core.publisher.Flux;
import riskfx.engine.display.Display;
import riskfx.engine.games.GameUtil;
import riskfx.engine.model.Map;
import riskfx.engine.model.Moves.Attack;
import riskfx.engine.model.Player;
import riskfx.engine.model.Territory;

public final class GameState implements Serializable {

	public static interface Lookup {
		default public Territory territory() {
			return territoryOpt().orElseThrow();
		}
		
		public Optional<Territory> territoryOpt();

		public Optional<Player> playerOpt();
		
		default public Player player() {
			return playerOpt().orElseThrow();
		}
	}
	
	/**
	 * An event that updates the game state.
	 * 
	 * @author christopher
	 *
	 */
	public static abstract class Event implements Serializable {

		protected abstract Flux<Event> apply(final GameState state, final Display notifier);

		public abstract String describe();
	}

	/**
	 * A move modifies the game state. All changes to the game state must be wrapped
	 * in a {@code Move} class so that the engine can serialize changes to any
	 * remove clients.
	 * 
	 * Moves can be <b>eager</b> or not. Clients may apply eager moves to a local
	 * game immediately while also sending to the master game. Eager moves must be
	 * deterministic and make the same update on all games. Any move that requires
	 * random logic MAY NOT be eager.
	 * 
	 * @author christopher
	 */
	public static abstract class Move implements Serializable {
		protected final Player player;

		protected Move(final Player player) {
			this.player = Objects.requireNonNull(player);
		}

		/**
		 * Player making the move.
		 * 
		 * @return player making the move.
		 */
		public final Player player() {
			return this.player;
		}

		protected abstract void apply(final GameState state, final Display notifier);

		/**
		 * Return a friendly description for the move.
		 * 
		 * @return
		 */
		public abstract String describe();

		/**
		 * Return whether move can be executed eagerly in networked play.
		 * 
		 * @return true iff this move can be applied eagerly
		 */
		public boolean isEager() {
			return false;
		}

		/**
		 * Return a move that does the opposite of this move.
		 * 
		 * @return
		 */
		public abstract Move invert();

		/**
		 * Optionally merge this move with an {@code other} move.
		 * 
		 * @param other
		 * @return merged move, or {@code Optional.empty()} if this move cannot be
		 *         merged with other.
		 */
		public Optional<Move> merge(final Move other) {
			return Optional.empty();
		}

		/**
		 * Replace this move with a series of other moves.
		 * 
		 * Only executed on the master game.
		 * 
		 * Default implementation just returns a Flux containing {@code this}.
		 * 
		 * @return
		 */
		public Flux<Move> moveReplace(final Game game, final GameState state) {
			return Flux.just(this);
		}
	}

	/**
	 * Represents a move that happens on one or more territories.
	 * 
	 * @author christopher
	 *
	 */
	public static abstract class MoveWithTerritory extends Move implements Serializable {
		protected final Territory territory;
		protected final Collection<Territory> territories;

		protected MoveWithTerritory(final Player player, final Territory territory, final Territory... others) {
			super(Objects.requireNonNull(player));
			this.territory = Objects.requireNonNull(territory);
			this.territories = Stream.concat(Stream.of(territory), Stream.of(others))
					.collect(Collectors.toUnmodifiableList());
		}

		public final Territory territory() {
			return this.territory;
		}

		public final Collection<Territory> territories() {
			return this.territories;
		}
	}

	@FunctionalInterface
	public interface ReinforcementCalculator extends Serializable {
		public long calculateReinforcements(final GameState state, final Player player);
	}

	private static final Logger LOG = Logger.getLogger(GameState.class.getName());

	public static PlayerStep create() {
		return new Builder();
	}

//	private final GameConfig.Type mode;
	
	private final boolean useCapitals = false;
	private final boolean useMissions = false;
	
	private final Set<Player> allPlayers;
	private final List<Player> turnOrder;
	private final Map map;

	private final ReinforcementCalculator reinforcementCalculator;

	long turnNumber = 0;
	private int currentPlayerIdx = -1;
	private Player currentPlayer = null;

	private long version = 0;
	private Attack mostRecentAttack;

	/* package */ GameState(final Builder builder) {
		this.allPlayers = Objects.requireNonNull(builder.allPlayers);
		this.turnOrder = Objects.requireNonNull(builder.turnOrder);
		this.map = Objects.requireNonNull(builder.map);

		turnOrder.forEach(p -> p.setTroopsToDeploy(builder.troopsToDeploy));
		this.reinforcementCalculator = Objects.requireNonNull(builder.reinforcementCalculator);

	}

	/**
	 * 
	 * @param move
	 */
	/* package */ void update(final Move move, final Display notifier) {
		move.apply(this, notifier);
	}

	public final Map map() {
		return this.map;
	}

	public Territory lookupTerritory(final String id) {
		return Objects.requireNonNull(map.lookupTerritory(Objects.requireNonNull(id)));
	}

	/* package */ static void autoAssignAll(final Map map, final List<Player> turnOrder) {
		final List<Territory> territories = new ArrayList<>(map.territories());
		Collections.shuffle(territories);

		for (int i = 0; i < territories.size(); i++) {
			final Player p = turnOrder.get(i % turnOrder.size());
			territories.get(i).setOwner(p);
		}

	}

	private static void autoPlaceAll(Map map, List<Player> turnOrder, long troopsToDeploy) {
		for (Player p : turnOrder) {
			List<Territory> terrs = map.territories().stream().filter(t2 -> t2.isOwnedBy(p))
					.collect(Collectors.toList());
//			LOG.info("Deploying %s troops for %s on %s territories".formatted(troopsToDeploy, p.getId(), terrs.size()));
			terrs.forEach(t -> t.setArmies(1));

			long remaining = Math.max(0, troopsToDeploy - terrs.size());
			new Random().ints(0, terrs.size()).limit(remaining).mapToObj(terrs::get)
					.forEach(terr -> terr.setArmies(terr.getArmies() + 1));
		}
	}

	public final int numUnclaimedTerritories() {
		return (int) unclaimedTerritories().count();
	}

	public final Stream<Territory> unclaimedTerritories() {
		return map.territories().stream().filter(t -> t.isOwnedBy(Player.none()));
	}

	public final Set<Player> allPlayers() {
		return this.allPlayers;
	}

	public final List<Player> turnOrder() {
		return this.turnOrder;
	}

	public final Stream<Territory> territoriesOwnedBy(Player player) {
		return this.map().territories().stream().filter(t -> t.isOwnedBy(player));
	}

	public static interface PlayerStep {
		public TurnOrderStep allPlayers(final Set<Player> allPlayers);
	}

	public static interface TurnOrderStep {
		public MapStep turnOrder(final Stream<Player> players);

		public MapStep turnOrder(final List<Player> players);

		public MapStep turnOrder(final Player... players);
	}

	public static interface MapStep {
		public InitialTroopsStep map(final Map map);
	}

	public static interface InitialTroopsStep {
		public OptionalStep initialTroopsToDeploy(final long troopsToDeploy);
	}

	public static interface OptionalStep {
		public OptionalStep reinforcementCalculator(final ReinforcementCalculator calculator);

		default public OptionalStep autoAssign() {
			return autoAssign(true);
		}

		public OptionalStep autoAssign(final boolean autoAssign);

		public OptionalStep autoPlace(boolean autoPlace);

		public GameState build();

	}

	private static class Builder implements PlayerStep, TurnOrderStep, MapStep, InitialTroopsStep, OptionalStep {
		private Set<Player> allPlayers;
		private List<Player> turnOrder;
		private Map map;

		private long troopsToDeploy = -1;

		private boolean autoAssign = false;
		private boolean autoPlace = false;

		private ReinforcementCalculator reinforcementCalculator = GameUtil::classicRiskReinforcements;

		@Override
		public OptionalStep autoAssign(boolean autoAssign) {
			this.autoAssign = autoAssign;
			return this;
		}

		@Override
		public OptionalStep autoPlace(boolean autoPlace) {
			this.autoPlace = autoPlace;
			return this;
		}

		@Override
		public GameState build() {
			GameState result = new GameState(this);

			if (autoAssign)
				autoAssignAll(result.map, result.turnOrder);
			if (autoPlace) {
				autoPlaceAll(result.map, result.turnOrder, troopsToDeploy);
				turnOrder.forEach(p -> p.setTroopsToDeploy(0));
			}

			return result;
		}

		@Override
		public OptionalStep initialTroopsToDeploy(long troopsToDeploy) {
			this.troopsToDeploy = troopsToDeploy;
			return this;
		}

		@Override
		public InitialTroopsStep map(Map map) {
			this.map = map;
			return this;
		}

		@Override
		public MapStep turnOrder(final Stream<Player> players) {
			this.turnOrder = players.collect(Collectors.toUnmodifiableList());
			return this;
		}

		@Override
		public MapStep turnOrder(List<Player> players) {
			this.turnOrder = Collections.unmodifiableList(new ArrayList<>(players));
			return this;
		}

		@Override
		public MapStep turnOrder(Player... players) {
			this.turnOrder = Arrays.asList(players);
			return this;
		}

		@Override
		public TurnOrderStep allPlayers(Set<Player> allPlayers) {
			this.allPlayers = Collections.unmodifiableSet(new HashSet<>(allPlayers));
			return this;
		}

		@Override
		public OptionalStep reinforcementCalculator(ReinforcementCalculator calculator) {
			this.reinforcementCalculator = Objects.requireNonNull(calculator);
			return this;
		}

	}

	public Player nextPlayer(Player player) {
		final int index = turnOrder.indexOf(player);
		return turnOrder.get( (index + 1) % turnOrder.size());
	}

	public final boolean useCapitals() {
		return this.useCapitals;
	}

	public final boolean useMissions() {
		return this.useMissions;
	}
	
	private final class LookupImpl implements Lookup {

		private final String id;

		public LookupImpl(String id) {
			super();
			this.id = id;
		}

		@Override
		public Optional<Territory> territoryOpt() {
			return Optional.ofNullable(map().lookupTerritory(id));
		}

		@Override
		public Optional<Player> playerOpt() {
			return allPlayers().stream().filter(p -> Objects.equals(id, p.getId())).findAny();
		}

	}

	public final Lookup lookup(String id) {
		return new LookupImpl(id);
	}

	public void setMostRecentAttack(Attack attack) {
		this.mostRecentAttack = attack;
	}

	public Attack getMostRecentAttack() {
		return this.mostRecentAttack;
	}
}
