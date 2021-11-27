package riskfx.engine.game;

import java.io.Serializable;

import riskfx.engine.display.Display;

public abstract class GameEvent implements Serializable {
	protected abstract void apply(final Game game, final Display notifier);
	
	public abstract String describe();
}