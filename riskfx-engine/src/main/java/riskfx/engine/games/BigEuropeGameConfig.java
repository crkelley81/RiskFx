package riskfx.engine.games;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import riskfx.engine.MutableGameConfig;
import riskfx.engine.game.GameState;
import riskfx.engine.model.Map;
import riskfx.engine.model.Player;

public class BigEuropeGameConfig extends MutableGameConfig {
	private static final Player[] PLAYERS = {
			Player.of("red", "Red"),
			Player.of("blue", "Blue"),
			Player.of("yellow", "Yellow"),
			Player.of("green", "Green"),
			Player.of("black", "Black"),
			Player.of("purple", "Purple")
	};
	public BigEuropeGameConfig() {
		super("bigeurope", "Big Europe", "bigeurope_pic.jpg", Stream.of(PLAYERS));
	}
	@Override
	protected Map createMap() {
		try {
			final URI uri = getClass().getResource("bigeurope.map").toURI();
			final Path path = Paths.get(uri);
			return DominationMapReader.of(this.getId(), this.getName(), path).read();
		} catch (IOException | URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}
	
	private long initialTroopsToDeploy(long playerCount) {
		return (long) ((180 / playerCount) * 2.5);
	}
	
	@Override
	protected GameState createInitialState(Set<Player> allPlayers, List<Player> turnOrder, Map map) {
		return GameState.create()
				.allPlayers(allPlayers)
				.turnOrder(turnOrder)
				.map(map)
				.initialTroopsToDeploy( initialTroopsToDeploy(turnOrder.size()))
				.reinforcementCalculator(GameUtil::classicRiskReinforcements)
				.autoAssign(this.isAutoAssign())
				.autoPlace(this.isAutoPlace())
				.build();
	}
}
