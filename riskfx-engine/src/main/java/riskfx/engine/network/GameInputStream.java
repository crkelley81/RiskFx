package riskfx.engine.network;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.Objects;

import riskfx.engine.game.GameState;

class GameInputStream extends ObjectInputStream {

	private final GameState state;

	public GameInputStream(InputStream in, final GameState state) throws IOException {
		super(in);
		this.state = Objects.requireNonNull(state);
		this.enableResolveObject(true);
	}

	@Override
	protected Object resolveObject(Object obj) throws IOException {
		if (obj instanceof GameData gd) {
			return gd.resolveIn(state);
		}
		return super.resolveObject(obj);
	}

}
