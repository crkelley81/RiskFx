package riskfx.engine;

public interface GameConfig {
	public static enum Type { 
		DOMINATION, CAPITAL, MISSION;
	}
	
	public static enum CardStyle {
		FIXED, INCREASING, ITALIAN;
	}
	
	String getId();

	String getName();

	Type getGameType();

	CardStyle getCardStyle();

	boolean isAutoAssign();

	boolean isAutoPlace();
	
	boolean useCapitals();
	boolean useMissions();
	
	GameConfig immutableCopy();

}