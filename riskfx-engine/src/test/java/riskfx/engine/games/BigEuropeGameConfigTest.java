package riskfx.engine.games;


import org.junit.jupiter.api.Assertions;

import riskfx.engine.GamConfigTestCase;
import riskfx.engine.game.Game;

class BigEuropeGameConfigTest extends GamConfigTestCase<BigEuropeGameConfig>{
	
	

	@Override
	protected void validateGame(BigEuropeGameConfig config, Game game) {
		Assertions.assertNotNull(game);
		
		
	}

	@Override
	protected BigEuropeGameConfig createGameConfig() {
		return new BigEuropeGameConfig();
	}

}
