package riskfx.engine.games;

import java.util.stream.Stream;

import riskfx.engine.GameConfig;
import riskfx.engine.model.Player;

public class BigEuropeGameConfig extends GameConfig {

	public BigEuropeGameConfig() {
		super("bigeurope", "Big Europe", "bigeurope_pic.jpg", Stream.of(
				Player.of("red", "Red"),
				Player.of("blue", "Blue"),
				Player.of("yellow", "Yellow")));
	}
}
