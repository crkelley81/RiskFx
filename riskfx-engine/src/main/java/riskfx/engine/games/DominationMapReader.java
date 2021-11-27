package riskfx.engine.games;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import riskfx.engine.model.Continent;
import riskfx.engine.model.Map;
import riskfx.engine.model.Territory;

public class DominationMapReader {

	
	public static DominationMapReader of(final String id, final String displayName, final Path path) {
		Objects.requireNonNull(path);
		return new DominationMapReader(id, displayName, path);
	}

	private final String id;
	private final String displayName;
	private final Path path;
	
	private DominationMapReader(final String id, final String displayName, final Path path) {
		this.id = Objects.requireNonNull(id);
		this.displayName = Objects.requireNonNull(displayName);
		this.path = Objects.requireNonNull(path);
	}

	public final Map read() throws IOException {
		final State state = Files.lines(path, Charset.forName("UTF-8"))
			.reduce(new State(), State::handle, (a, b) -> a);
		return state.toMap();
	}
	
	private class State { 
		final List<String> continents = new ArrayList<>();
		final List<Territory> territories = new ArrayList<>();
		final java.util.Map<String, Set<Territory>> territoryMap = new HashMap<>();
		final java.util.Map<Territory, Set<Territory>> neighborMap = new HashMap<>();
		
		boolean readingContinents = false;
		boolean readingTerritories = false;
		boolean readingBorders = false;
		
		public State handle(final String line) {
			if (line.startsWith("[continents]")) {
				readingContinents = true;
			} else if (readingContinents && !line.isBlank()) {
				final String name = line.substring(0, line.indexOf(" ") - 1).trim();
				continents.add(name);
				territoryMap.put(name, new HashSet<>());
			} else if (readingContinents && line.isBlank()) {
				readingContinents = false;
			} else if (line.startsWith("[countries]")) {
				readingTerritories = true;
			} else if (readingTerritories && !line.isBlank()) {
				final String[] parts = line.split(" ");
				final Territory defaultTerritory = Territory.of(parts[1], parts[1]);
				territories.add(defaultTerritory);
				territoryMap.get(continents.get(Integer.parseInt(parts[2]) - 1)).add(defaultTerritory);
			} else if (readingTerritories && line.isBlank()) {
				readingTerritories = false;
			} else if (line.startsWith("[borders]")) {
				readingBorders = true;
			} else if (readingBorders && !line.isBlank()) {
				final String[] parts = line.split(" ");
				final int index = Integer.parseInt(parts[0]) - 1;
				final Territory t = territories.get(index);
				neighborMap.put(t, new HashSet<>());
				for (int i = 1; i < parts.length; i++) {
					final Territory b = territories.get(Integer.parseInt(parts[i]) - 1);
					neighborMap.get(t).add(b);
				}
			}	
			return this;
		}

		public Map toMap() {
			final Stream<Continent> continents = this.continents.stream()
					.map(id -> Continent.of(id, id, 0, territoryMap.get(id).stream()));
			
			Map.ConnectionStep builder = Map.of(id, displayName, continents);

			for (java.util.Map.Entry<Territory, Set<Territory>> entry : this.neighborMap.entrySet()) {
				builder = builder.neighbors(entry.getKey(), entry.getValue());
			}
			
			return builder.build();
		}
		
		
	}
}
