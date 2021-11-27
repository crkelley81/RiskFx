package riskfx.engine.model;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyLongProperty;
import javafx.beans.property.ReadOnlyLongWrapper;
import javafx.beans.property.SimpleObjectProperty;
import riskfx.util.role.Displayable;
import riskfx.util.role.Identifiable;

/**
 * 
 * @author christopher
 *
 */
public final class Territory implements Identifiable, Displayable, Serializable {

	public static interface Notifier {
		public void territoryOwnerChanged(final Territory territory, final Player oldOwner, final Player newOwner);
		public void territoryArmiesChanged(final Territory territory, final long old, final long newArmies);
		public void territoryCapitalChanged(final Territory territory, final Player oldPlayer, final Player newPlayer);
	}
	
	public static Territory of(final String name, final Object...lookups) {
		return of(Util.toId(name), name, lookups);
	}
	
	public static Territory of(final String id, final String name, final Object... lookups) {
		return new Territory(id, name /*, lookups*/);
	}
	
	private final String id;
	private final String displayName;
	
	private Continent continent;
	private Set<Territory> neighbors; 
	
	private Player owner = Player.none();
	private transient ObjectProperty<Player> ownerProperty = null;
	
	private long armies = 0;
	private transient ReadOnlyLongWrapper armiesProperty = null;
	
	private Player capitalFor = null;
	
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
	
	public final boolean isCapitalFor(final Player player) {
		return Objects.equals(capitalFor, player);
	}
	
	/* package */ public void setArmies(final long armies) {
		final long oldArmies = this.armies;
		this.armies = armies;
		Optional.ofNullable(armiesProperty).ifPresent(p -> p.setValue(this.armies));
		notify(n -> n.territoryArmiesChanged(this, oldArmies, oldArmies));
	}
	
	/* package */ public void setOwner(final Player owner) {
		final Player ov = this.owner;
		this.owner = owner;
		Optional.ofNullable(ownerProperty).ifPresent(p -> p.setValue(owner));
		notify(n -> n.territoryOwnerChanged(this, ov, owner));
	}
	
	public final ObjectProperty<Player> ownerProperty() {
		if (ownerProperty == null) {
			this.ownerProperty = new SimpleObjectProperty<>(this, "owner", owner);
		}
		return this.ownerProperty;
	}
	
	/* package */ void setCapitalFor(final Player player) {
		final Player oldVal = this.capitalFor;
		this.capitalFor = player;
		notify(n -> n.territoryCapitalChanged(this, oldVal, capitalFor));
	}

	private void notify(final Consumer<Notifier> action) {
		Optional.ofNullable(notifier).ifPresent(action);
	}
	
	/* package */ void initContinent(final Continent continent) {
		Objects.requireNonNull(continent);
		if (this.continent != null) throw new IllegalStateException("Continent already set to %s for territory %s".formatted(this.continent.getId(), getId()));
		this.continent = continent;
	}
	
	/* package */ void initNotifier(final Notifier notifier) {
		this.notifier = notifier;
	}
	
	/* package */ void initNeighbors(final Stream<Territory> territories) {
		this.neighbors = territories.collect(Collectors.toUnmodifiableSet());
	}

	public final boolean isOwnedBy(final Player player) {
		return Objects.equals(getOwner(), player);
	}
	
	public final Optional<Player> getCapitalFor() {
		return Optional.ofNullable(capitalFor);
	}

	public final boolean isNeighborOf(Territory from) {
		return this.neighbors.contains(from);
	}

	public final Stream<Territory> neighborsNotOwnedBy(Player p) {
		return this.neighbors.stream().filter(t -> ! t.isOwnedBy(p));
	}

	public final Stream<Territory>neighborsOwnedBy(Player player) {
		return this.neighbors.stream().filter(t -> t.isOwnedBy(player));
	}

	public final ReadOnlyLongProperty armiesProperty() {
		if (this.armiesProperty == null) {
			this.armiesProperty = new ReadOnlyLongWrapper(this, "armies", this.armies);
		}
		return this.armiesProperty.getReadOnlyProperty();
	}
}
