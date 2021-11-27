package riskfx.app.view;

import javafx.scene.control.TreeCell;
import riskfx.engine.game.GameEvent;

public class HistoryTreeCell extends TreeCell<GameEvent> {

	@Override
	protected void updateItem(final GameEvent item, boolean empty) {
		super.updateItem(item, empty);
		
		if (empty || (item == null)) {
			setText(null);
			setGraphic(null);
		}
		else {
			setText(item.describe());
		}
	}

}
