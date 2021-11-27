package riskfx.engine;

import riskfx.engine.game.GamePlayer;

/**
 * 
 * @author christopher
 *
 */
public interface PlayerType {
	
	public static enum Type {
		HUMAN, AI, REMOTE;
	}
	
	public String getDisplayName();
	
	public boolean isHuman();
	public boolean isComputer();
	public boolean isNone();
	public boolean isVisible();
	
	public GamePlayer newGamePlayerWithName(final String name);
}