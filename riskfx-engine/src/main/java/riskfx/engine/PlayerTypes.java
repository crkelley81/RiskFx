package riskfx.engine;

import java.util.Collection;
import java.util.List;
import java.util.ServiceLoader;
import java.util.ServiceLoader.Provider;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PlayerTypes {

	public static PlayerTypes create() {
		Stream<PlayerType> stream = Stream.of(StandardPlayerTypes.HUMAN, StandardPlayerTypes.DOES_NOTHING_AI, StandardPlayerTypes.NONE);
		Stream<PlayerType> stream2 = ServiceLoader.load(PlayerType.class).stream().map(Provider::get);
		
		return new PlayerTypes(Stream.concat(stream, stream2));
		
	}

	private final List<PlayerType> playerTypes;
	private final List<PlayerType> visibleTypes;
	
	/* package for testing */ PlayerTypes(final Stream<PlayerType> types) {
		this.playerTypes = types.collect(Collectors.toUnmodifiableList());
		this.visibleTypes = this.playerTypes.stream().filter(PlayerType::isVisible).collect(Collectors.toUnmodifiableList());
	}
	
	public Collection<PlayerType> visibleTypes() {
		return this.visibleTypes;
	}
	
	public Collection<PlayerType> allTypes() {
		return this.playerTypes;
	}
}
