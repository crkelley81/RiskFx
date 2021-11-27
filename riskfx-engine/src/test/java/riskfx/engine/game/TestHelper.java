package riskfx.engine.game;

import riskfx.engine.game.GameState;
import riskfx.engine.games.ClassicRiskGameConfig;
import riskfx.engine.model.Continent;
import riskfx.engine.model.Map;
import riskfx.engine.model.Player;
import riskfx.engine.model.Territory;

public class TestHelper {

	public static GameState classicRiskState(final Player... players) {
		return new ClassicRiskGameConfig().createInitialState();
	}

	private static Map classicRiskMap() {
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

}
