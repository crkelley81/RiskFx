package riskfx.engine.game;

import java.util.Objects;

import riskfx.engine.MutableGameConfig;
import riskfx.engine.games.BigEuropeGameConfig;
import riskfx.engine.model.Card;
import riskfx.engine.model.Player;
import riskfx.engine.model.Territory;

public class BigEuropeGameFixture {

	public final MutableGameConfig gameConfig;
	public final Game game;

	public final Player playerBlack;
	public final Player playerBlue;
	public final Player playerRed;

	public final Card wildcard1;
	public final Card wildcard2;
	public final Card infantrySi1;
	public final Card infantrySi2;
	public final Card artilleryS1;
	public final Card cavalrySi3;

	public final Territory territorySi1;
	public final Territory territorySi2;
	public final Territory territorySi3;
	public final Territory territoryS1;

	public BigEuropeGameFixture(int i, boolean autoAssign, boolean autoPlace) {
		gameConfig = new BigEuropeGameConfig();

		gameConfig.setAutoAssign(autoAssign);
		gameConfig.setAutoPlace(autoPlace);

		game = Game.from(gameConfig);

		playerBlack = game.lookup("black").player();
		playerBlue = game.lookup("blue").player();
		playerRed = game.lookup("red").player();

		territorySi1 = game.lookup("si1").territory();
		territorySi2 = game.lookup("si2").territory();
		territorySi3 = game.lookup("si3").territory();
		territoryS1 = game.lookup("s1").territory();

		wildcard1 = Card.wildcard(1);
		wildcard2 = Card.wildcard(2);

		infantrySi1 = Card.territoryCard(territorySi1, Card.Type.INFANTRY);
		infantrySi2 = Card.territoryCard(territorySi2, Card.Type.INFANTRY);
		artilleryS1 = Card.territoryCard(territoryS1, Card.Type.ARTILLERY);
		cavalrySi3 = Card.territoryCard(territoryS1, Card.Type.CAVALRY);
	}

	public BigEuropeGameFixture(int i, boolean autoAssign, boolean autoPlace, BattleCalculator calculator) {
		
		gameConfig = new BigEuropeGameConfig();
		gameConfig.setBattleCalculator(Objects.requireNonNull(calculator));
		gameConfig.setAutoAssign(autoAssign);
		gameConfig.setAutoPlace(autoPlace);

		game = Game.from(gameConfig);

		playerBlack = game.lookup("black").player();
		playerBlue = game.lookup("blue").player();
		playerRed = game.lookup("red").player();

		territorySi1 = game.lookup("si1").territory();
		territorySi2 = game.lookup("si2").territory();
		territorySi3 = game.lookup("si3").territory();
		territoryS1 = game.lookup("s1").territory();

		wildcard1 = Card.wildcard(1);
		wildcard2 = Card.wildcard(2);

		infantrySi1 = Card.territoryCard(territorySi1, Card.Type.INFANTRY);
		infantrySi2 = Card.territoryCard(territorySi2, Card.Type.INFANTRY);
		artilleryS1 = Card.territoryCard(territoryS1, Card.Type.ARTILLERY);
		cavalrySi3 = Card.territoryCard(territoryS1, Card.Type.CAVALRY);
	}

	public void autoAssignAll() {
		EngineTestUtil.randomlyAssignAll(game.state());
	}

	public void autoPlaceAll() {
		GameState.autoAssignAll(game.state().map(), game.state().turnOrder());
	}
}
