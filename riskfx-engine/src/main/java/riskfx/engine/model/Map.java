package riskfx.engine.model;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Map {

	private final String id;
	private final String displayName;
	
	private final java.util.Map<String, Continent> continents;
	private final java.util.Map<String, Territory> territories;
	
	private Map(final String id, final String displayName, final Stream<Continent> continents) {
		this.id = Objects.requireNonNull(id);
		this.displayName = Objects.requireNonNull(displayName);
	
		this.continents = continents.collect(Collectors.toUnmodifiableMap(Continent::getId, Function.identity()));
		this.territories = this.continents.values()
			.stream()
			.flatMap(c -> c.territories().stream())
			.collect(Collectors.toUnmodifiableMap(Territory::getId, Function.identity()));
	}
		
	public final Collection<Continent> continents() {
		return this.continents.values();
	}
	
	public final Collection<Territory> territories() {
		return this.territories.values();
	}
}
