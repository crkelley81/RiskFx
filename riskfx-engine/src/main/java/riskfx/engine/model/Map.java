package riskfx.engine.model;

import java.io.Serializable;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Map implements Serializable {
	public static ConnectionStep of(final String id, final String displayName, final Continent... continents) {
		return of(id, displayName, Stream.of(continents));
	}
	
	public static ConnectionStep of(final String id, String displayName, Stream<Continent> continents) {
		final Map map = new Map(id, displayName, continents);
		return new ConnectionStep(map);
	}

	public static interface Notifier extends Continent.Notifier {
		
	}
	
	private final String id;
	private final String displayName;
	
	private final java.util.Map<String, Continent> continents;
	private final java.util.Map<String, Territory> territories;
	
	private transient Notifier notifier; 
	
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
	
	public final Territory lookupTerritory(String id) {
		return this.territories.get(id);
	}
	
	public final void setNotifier(final Notifier notifier) {
		continents.values().stream().forEach(c -> c.initNotifier(notifier));
	}
	
	public static class ConnectionStep {

		private final Map map;
		private final java.util.Map<String, Territory> territoryMap;
		
		private ConnectionStep(final Map map) {
			this.map = Objects.requireNonNull(map);
			this.territoryMap = map.territories()
					.stream()
					.collect(Collectors.toUnmodifiableMap(Territory::getId, Function.identity()));
		}

		public ConnectionStep neighbors(String id, String... neighbors) {
			final Stream<Territory> n = Stream.of(neighbors).map(territoryMap::get);
			territoryMap.get(id).initNeighbors( n );
			return this;
		}

		public ConnectionStep neighbors(Territory key, Set<Territory> value) {
			key.initNeighbors(value.stream());
			return this;
		}
		
		
		public Map build() {
			validateAllTerritoriesHaveNeighbors();
			return map;
		}

		private void validateAllTerritoriesHaveNeighbors() {
//			boolean allTerritoriesHaveNeighbors = map.territories().stream()
//					.allMatch(t -> t.)
		}


	}



}
