package riskfx.engine.game;

import riskfx.engine.model.BattleResult;
import riskfx.engine.model.Moves.Attack;

public interface BattleCalculator {

	BattleResult battle(GameState state, Attack attack);

}
