package riskfx.ui;

import java.io.PrintWriter;

import javafx.scene.control.TreeItem;
import riskfx.engine.game.Events.BeginTurn;
import riskfx.engine.game.GameEvent;

public class History {

	
	private final TreeItem<GameEvent> root = new TreeItem<>();

	private TreeItem<GameEvent> currentTurn = null;
	
	public void update(final GameEvent move) {
		if (move instanceof BeginTurn b || (currentTurn == null)) {
			currentTurn = new TreeItem<>(move);
			currentTurn.setExpanded(true);
			root.getChildren().add(currentTurn);
		}
		else {
			currentTurn.getChildren().add(new TreeItem<>(move));
		}
	}
	
	public final TreeItem<GameEvent> asTreeItem() {
		return root;
	}
	
	public final void print(final PrintWriter writer) {
		root.getChildren().forEach(ti -> print(writer, ti, 0));
		writer.flush();
	}

	private void print(PrintWriter writer, TreeItem<GameEvent> ti, int indent) {
		String indentText = (indent > 0) ? "  ".repeat(indent) : "";
		writer.println(indentText + ti.getValue().describe());
		ti.getChildren().forEach(child -> print(writer, child, indent + 1 ));
	}
}
