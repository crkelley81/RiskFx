package riskfx.engine.games;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import riskfx.engine.MutableGameConfig;
import riskfx.engine.game.GameState;
import riskfx.engine.model.Continent;
import riskfx.engine.model.Map;
import riskfx.engine.model.Player;
import riskfx.engine.model.Territory;

public class ClassicRiskGameConfig extends MutableGameConfig {
	private static final Player[] PLAYERS = {
			Player.of("red", "Red"),
			Player.of("blue", "Blue"),
			Player.of("yellow", "Yellow"),
			Player.of("green", "Green"),
			Player.of("black", "Black"),
			Player.of("purple", "Purple")
	};
	
	public ClassicRiskGameConfig() {
		super("classic-risk", "Classic Risk", "bigeurope_pic.jpg", Stream.of(PLAYERS));
	}

	@Override
	public Map createMap() {
		return classicRiskMap();
	}
	
	public static Map classicRiskMap() {
		return Map.of("classic-risk", "Classic Risk",
				Continent.of("North America", 7,
						Territory.of("Eastern United States"),
						Territory.of("Alaska"),
						Territory.of("alberta", "Alberta (Western Canada)"),
						Territory.of("Central America"),
						Territory.of("Greenland"),
						Territory.of("Northwest Territory"),
						Territory.of("ontario", "Ontario (Central Canada)"),
						Territory.of("quebec", "Quebec (Eastern Canada)"),
						Territory.of("Western United States")),
				Continent.of("South America", 2, 
						Territory.of("Argentina"),
						Territory.of("Brazil"),
						Territory.of("Peru"),
						Territory.of("Venezuela")),
				Continent.of("Asia", 7, Territory.of("Kamchatka"))
				)
				.neighbors("eastern-united-states", "western-united-states", "central-america", "ontario", "quebec")
				.neighbors("alaska", "kamchatka", "northwest-territory", "alberta")
				.build();
	}


	protected int initialTroopsToDeploy(final int playerCount) {
		return 50 - 5 * playerCount;
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
