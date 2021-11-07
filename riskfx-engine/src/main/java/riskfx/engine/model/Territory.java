package riskfx.engine.model;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 
 * @author christopher
 *
 */
public class Territory {

	public static interface Notifier {
		public void territoryOwnerChanged(final Territory territory, final Player oldOwner, final Player newOwner);
		public void territoryArmiesChanged(final Territory territory, final long old, final long newArmies);
	}
	
	private final String id;
	private final String displayName;
	
	private Continent continent;
	private Set<Territory> neighbors; 
	
	private Player owner = null;
	private long armies = 0;
	
	private Notifier notifier = null;
	
	private Territory(final String id, final String displayName) {
		this.id = Objects.requireNonNull(id);
		this.displayName = Objects.requireNonNull(displayName);
	}
	
	public final String getId() {
		return this.id;
	}
	
	public final String getDisplayName() {
		return this.displayName;
	}
	
	public final Continent getContinent() {
		return this.continent;
	}
	
	public final Player getOwner() {
		return this.owner;
	}
	
	public final long getArmies() {
		return this.armies;
	}
	
	/* package */ void setArmies(final long armies) {
		final long oldArmies = this.armies;
		this.armies = armies;
		this.notifier.territoryArmiesChanged(this, oldArmies, oldArmies);
	}
	
	/* package */ void setOwner(final Player owner) {
		final Player ov = this.owner;
		this.owner = owner;
		notifier.territoryOwnerChanged(this, ov, owner);
	}

	/* package */ void initContinent(final Continent continent) {
		this.continent = continent;
	}
	
	/* package */ void initNotifier(final Notifier notifier) {
		this.notifier = notifier;
	}
	
	/* package */ void initNeighbors(final Stream<Territory> territories) {
		this.neighbors = territories.collect(Collectors.toUnmodifiableSet());
	}
}
