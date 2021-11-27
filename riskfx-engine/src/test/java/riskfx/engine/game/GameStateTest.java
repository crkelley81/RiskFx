package riskfx.engine.game;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import riskfx.engine.display.Display;
import riskfx.engine.game.GameState;
import riskfx.engine.model.Moves;
import riskfx.engine.model.Player;
import riskfx.engine.model.Territory;
import riskfx.engine.model.Moves.Claim;

class GameStateTest {

	private static final Player PLAYER_RED = Player.of("red", "Red");
	private static final Player PLAYER_BLUE = Player.of("blue", "Blue");
	private static final Player PLAYER_GREEN = Player.of("green", "Green");
	
	private Display notifier =new Display() {};
	
	private GameState gameState = TestHelper.classicRiskState(PLAYER_RED, PLAYER_BLUE, PLAYER_GREEN);
	
	private Territory territoryEus = gameState.lookupTerritory("eastern-united-states");
	private Territory quebec = gameState.lookupTerritory("quebec");
	private Territory territoryAlberta = gameState.lookupTerritory("alberta");
	
	@Test void claimJson() throws JsonProcessingException {
		Claim move = Moves.claim(territoryAlberta, PLAYER_BLUE);
		ObjectMapper mapper = new ObjectMapper();
		System.err.println( mapper.writeValueAsString(move));
	}
	
	@Test
	void claim() {
		// GIVEN 
		// WHEN 
		gameState.update(Moves.claim(territoryEus, PLAYER_BLUE), notifier);
		
		// THEN 
		Assertions.assertEquals(PLAYER_BLUE, territoryEus.getOwner());
	}

	@Test
	void place() {
		// GIVEN 
		gameState.update(Moves.claim(territoryAlberta, PLAYER_BLUE), notifier);
		
		// WHEN 
		gameState.update(Moves.place(territoryAlberta, PLAYER_BLUE, 1), notifier);
		
		// THEN
		Assertions.assertEquals(1, territoryAlberta.getArmies());
	}
}
