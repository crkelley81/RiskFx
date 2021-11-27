package riskfx.mapeditor;

import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;
import riskfx.ui.TerritorySkin;

public class TerritorySkinCellFactory extends ListCell<TerritorySkin<?>> {

	public static final Callback<ListView<TerritorySkin<?>>, ListCell<TerritorySkin<?>>> forListView() {
		return lv -> new TerritorySkinCellFactory();
	}

	@Override
	protected void updateItem(TerritorySkin<?> item, boolean empty) {
		super.updateItem(item, empty);
		
		this.textProperty().unbind();
		
		if (empty || (item == null)) {
			setGraphic(null);
			setText(null);
		}
		else {
			this.textProperty().bind(item.idProperty());
		}
	}
	
	
}
