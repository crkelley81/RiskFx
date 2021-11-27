package riskfx.engine.network;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Objects;

import riskfx.engine.game.GameState;

class GameOutputStream extends ObjectOutputStream {

	private GameState state;

	public GameOutputStream(OutputStream out, GameState state) throws IOException {
		super(out);
		this.state = Objects.requireNonNull(state);
		this.enableReplaceObject(true);
	}

	@Override
	protected Object replaceObject(final Object obj) throws IOException {
		if (GameData.canSerialize(obj)) {
			return new GameData(obj);
		}
		return super.replaceObject(obj);
	}

	
}
