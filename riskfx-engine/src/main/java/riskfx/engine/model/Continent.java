package riskfx.engine.model;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Continent {
	public static interface Notifier extends Territory.Notifier {
		
	}

	public static Continent of(final String id, final String displayName, final long bonusArmies, final Territory... territories) {
		return new Continent(id, displayName, bonusArmies, Stream.of(territories));
	}
	
	private final String id;
	private final String displayName;
	private final long bonusArmies;
	private final Set<Territory> territories;
	
	private transient Notifier notifier;
	
	private Continent(final String id, final String displayName, final long bonusArmies, final Stream<Territory> territories) {
		this.id = Objects.requireNonNull(id);
		this.displayName = Objects.requireNonNull(displayName);
		this.bonusArmies = bonusArmies;
		this.territories = territories.collect(Collectors.toUnmodifiableSet());
		this.territories.forEach(t -> t.initContinent(this));
	}
	
	public final String getId() {
		return this.id;
	}
	
	public final String getDisplayName() {
		return this.displayName;
	}
	
	public final long getBonusArmies() {
		return this.bonusArmies;
	}
	
	public final Set<Territory> territories() {
		return this.territories;
	}
	
	/* package */ void initNotifier(final Notifier notifier) {
		this.notifier = notifier;
		territories.forEach(t -> t.initNotifier(notifier));
	}
}
