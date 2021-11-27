package riskfx.engine;

import java.util.Objects;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import riskfx.engine.game.Game;
import riskfx.engine.model.Player;

public abstract class GamConfigTestCase<T extends MutableGameConfig> {

	private T gameConfig = null;

	protected abstract T createGameConfig();

	@Test
	public final void hasPreviewImage() {
		// GIVEN
		final T gameConfig = this.createGameConfig();
		assert gameConfig != null;

		// WHEN

		// THEN
		Assertions.assertNotNull(gameConfig.getPreviewImageUrl());
	}

	@Test
	public final void isNotReadyWithoutAtLeast2Players() {
		// GIVEN
		final T gameConfig = this.createGameConfig();
		assert gameConfig != null;

		// WHEN
		gameConfig.playerAssociations().forEach(pa -> {
			pa.typeProperty().set(StandardPlayerTypes.NONE);
		});

		// THEN
		Assertions.assertFalse(gameConfig.isReady());

		// WHEN
		gameConfig.playerAssociations().get(0).typeProperty().set(StandardPlayerTypes.HUMAN);

		// THEN
		Assertions.assertFalse(gameConfig.isReady());
	}

	@Test
	public final void isReadyWithAtLeast2Players() {
		// GIVEN
		final T gameConfig = this.createGameConfig();
		assert gameConfig != null;

		// WHEN
		gameConfig.playerAssociations().forEach(pa -> {
			pa.typeProperty().set(StandardPlayerTypes.NONE);
		});

		gameConfig.playerAssociations().stream().limit(2).forEach(pa -> pa.typeProperty().set(StandardPlayerTypes.HUMAN));

		// THEN
		Assertions.assertTrue(gameConfig.isReady());
	}

	@Test
	public final void createGame() {
		// GIVEN
		final T gameConfig = this.createGameConfig();
		assert gameConfig != null;

		gameConfig.playerAssociations().get(0).typeProperty().setValue(StandardPlayerTypes.NONE);

		validateCreateGame(gameConfig);
	}

	@Test
	public final void createGameWithAutoAssign() {
		// GIVEN
		final T gameConfig = this.createGameConfig();
		assert gameConfig != null;

		gameConfig.playerAssociations().get(0).typeProperty().setValue(StandardPlayerTypes.NONE);
		gameConfig.setAutoAssign(true);
		validateCreateGame(gameConfig);
	}

	private void validateCreateGame(final T gameConfig) {
		Objects.requireNonNull(gameConfig);
		// WHEN
		final Game game = Game.from(gameConfig);

		// THEN
		Assertions.assertNotNull(game);
		Assertions.assertEquals(gameConfig.getId(), game.getId());
		Assertions.assertEquals(gameConfig.getName(), game.getDisplayName());

		for (PlayerAssociation pa : gameConfig.playerAssociations()) {
			Assertions.assertEquals(!pa.typeProperty().get().isNone(), game.allPlayers().contains(pa.getPlayer()));
		}
		
		Assertions.assertNotNull(game.map());

		if (gameConfig.isAutoAssign()) {
			boolean allAssigned = game.map()
				.territories()
				.stream()
				.allMatch(t -> (t.getOwner() != null) && ! Objects.equals(Player.none(), t.getOwner()));
			Assertions.assertTrue(allAssigned);
		}

		validateGame(gameConfig, game);
	}

	protected void validateGame(T config, Game game) {
		// TODO Auto-generated method stub

	}
}
