package riskfx.engine.game;

import riskfx.engine.display.Display;
import riskfx.engine.game.GameState;
import riskfx.engine.game.GameState.Move;
import riskfx.engine.model.Player;
import riskfx.engine.model.Territory;

public class EngineTestUtil {

	public static void update(GameState state, Display notifier, final Move move) {
		state.update(move, notifier);
	}

	public static void randomlyAssignAll(GameState state) {
		GameState.autoAssignAll(state.map(), state.turnOrder());
	}

	public static void territory(GameState state, String territoryId, Player player, int armies) {
		final Territory t = state.lookupTerritory(territoryId);
		t.setOwner(player);
		t.setArmies(armies);
	}

}
