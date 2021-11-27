package riskfx.ui;

import java.io.PrintWriter;
import java.util.List;
import java.util.stream.Collectors;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javafx.scene.control.TreeItem;
import riskfx.engine.MutableGameConfig;
import riskfx.engine.game.GameEvent;
import riskfx.engine.game.Events;
import riskfx.engine.game.Game;
import riskfx.engine.games.BigEuropeGameConfig;
import riskfx.engine.model.Moves;
import riskfx.engine.model.Player;
import riskfx.engine.model.Territory;

class HistoryTest {

	private Game game;
	private Player playerBlack;
	private List<Territory> blackTerritories;
	
	@BeforeEach public void setup() {
		final MutableGameConfig config = new BigEuropeGameConfig();
		config.setAutoAssign(true);
		config.setAutoPlace(true);
		
		game = Game.from(config);
		
		playerBlack = game.lookupPlayer("black");
		
		blackTerritories = game.state().map().territories().stream().filter(t -> t.isOwnedBy(playerBlack)).collect(Collectors.toUnmodifiableList());
	}
	
	@Test
	void test() {
		// GIVEN 
		final History history = new History();
		
		// WHEN 
		history.update(Events.beginTurn(playerBlack, 1));
		
		history.update(Events.move(Moves.place(blackTerritories.get(0), playerBlack, 3)));
		history.update(Events.move(Moves.place(blackTerritories.get(1), playerBlack, 4)));
		history.update(Events.move(Moves.place(blackTerritories.get(2), playerBlack, 5)));
		
		history.update(Events.endTurn(playerBlack, 1));
		
		// THEN 
		final TreeItem<GameEvent> turn = history.asTreeItem().getChildren().get(0);
		Assertions.assertThat(turn.getChildren()).hasSize(4);
		history.print(new PrintWriter(System.err));
	}

}
