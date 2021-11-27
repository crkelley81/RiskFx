package riskfx.engine.games;

import riskfx.engine.game.GameState;
import riskfx.engine.model.Continent;
import riskfx.engine.model.Player;

public class GameUtil {

	public static long classicRiskReinforcements(final GameState state, final Player player) {
		long troopsToDeploy = state.territoriesOwnedBy(player).count() / 3;
		for (Continent c : state.map().continents()) {
			if (c.isOwnedBy(player)) {
				troopsToDeploy += c.getBonusArmies();
			}
		}
		return troopsToDeploy;
	}
}
