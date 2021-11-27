package riskfx.engine.network;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import riskfx.engine.MutableGameConfig;
import riskfx.engine.PlayerAssociation;
import riskfx.engine.game.GameState;
import riskfx.engine.games.ClassicRiskGameConfig;
import riskfx.engine.model.Moves;
import riskfx.engine.model.Moves.Claim;
import riskfx.engine.model.Player;
import riskfx.engine.model.Territory;

class GameDataTest {

	@Test
	void test() {
		final Territory t =  Territory.of("id", "Id");
		Assertions.assertTrue( GameData.canSerialize(t));
	}

	@Test void full() throws IOException, ClassNotFoundException {
		// GIVEN 
		final MutableGameConfig config = new ClassicRiskGameConfig();
		final List<Player> turnOrder = config.playerAssociations().stream()
				.map(PlayerAssociation::getPlayer)
				.collect(Collectors.toUnmodifiableList());
		final GameState state1 = new ClassicRiskGameConfig().createInitialState();
		final GameState state2 = serializeCopy(state1);
		
		final Territory alaska2 = state2.lookupTerritory("alaska");
		Assertions.assertNotSame(state1.lookupTerritory("alaska"), alaska2);
		
		// WHEN 
		final Claim claim = Moves.claim(state1.lookupTerritory("alaska"), turnOrder.get(0));
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		final GameOutputStream out = new GameOutputStream(baos, state1);
		out.writeObject(claim);
		out.flush();
		out.close();
		
		final ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		final GameInputStream in = new GameInputStream(bais, state2);
		Claim claim2 = (Claim) in.readObject();
		in.close();
		
		// THEN 
		Assertions.assertNotSame(claim, claim2);
		Assertions.assertEquals(alaska2, claim2.territory());
		
	}

	private GameState serializeCopy(GameState state) throws IOException, ClassNotFoundException {
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		final ObjectOutputStream out = new ObjectOutputStream(baos);
		out.writeObject(state);
		out.flush();
		out.close();
		
		try (final ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		final ObjectInputStream in = new ObjectInputStream(bais)) {
		return (GameState) in.readObject();
		} 
	}
}
