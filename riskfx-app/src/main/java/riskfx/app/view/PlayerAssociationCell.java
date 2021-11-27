package riskfx.app.view;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.util.Callback;
import riskfx.engine.PlayerAssociation;
import riskfx.engine.PlayerType;

public class PlayerAssociationCell extends ListCell<PlayerAssociation> {

	public static Callback<ListView<PlayerAssociation>, ListCell<PlayerAssociation>> factory(final Stream<PlayerType> types) {
		final Collection<PlayerType> t = types.collect(Collectors.toUnmodifiableList());
		return lv -> new PlayerAssociationCell(t);
	}

	private final Region colorRegion = new Region();
	private final Label nameField = new Label();
	private final ComboBox<PlayerType> typeCombo = new ComboBox<>();
	
	private final HBox hbox = new HBox(colorRegion, nameField, typeCombo);
	
	private Runnable clear = () -> {};
	
	private PlayerAssociationCell(final Collection<PlayerType> t) {
		super();
		
		hbox.setSpacing(8);
		hbox.setAlignment(Pos.CENTER_LEFT);
		colorRegion.setPrefSize(20, 20);
		
		nameField.setPrefWidth(150);
		typeCombo.setPrefWidth(100);
		
		typeCombo.getItems().addAll(t);
	}
	
	@Override
	protected void updateItem(PlayerAssociation pa, boolean empty) {
		super.updateItem(pa, empty);
		
		clear.run();
		
		if (!empty && (pa != null)) {
			setGraphic(hbox);
			
			nameField.setText(pa.getDisplayName());
			typeCombo.valueProperty().bindBidirectional(pa.typeProperty());
			
			clear = () -> {
				nameField.setText("");
				typeCombo.valueProperty().unbindBidirectional(pa.typeProperty());
				
				setGraphic(null);
				setText(null);
			};
		}
	}
	
	
}
