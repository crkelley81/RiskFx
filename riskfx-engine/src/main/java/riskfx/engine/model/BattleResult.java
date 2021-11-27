package riskfx.engine.model;

import java.util.List;

import reactor.core.publisher.Flux;
import riskfx.engine.game.GameState.Move;
import riskfx.engine.model.Moves.Attack;

public record BattleResult(Attack attack, boolean conquer, List<Long> attackerResults, List<Long> defenderResults, long attackerLosses, long defenderLosses) {

	public Flux<Move> asEvents() {
		Flux<Move> flux =  Flux.just(
				attack(),
				Moves.battle(attack, attackerResults, defenderResults),
				Moves.battleResult(attack(), attackerLosses, defenderLosses)
				);
		if (conquer) {
			flux = flux.concatWithValues(Moves.conquer(attack.player(), attack.territory(), attack.to));
		}
		return flux;
	}
}
