package riskfx.app.util.role;

import javafx.beans.property.ReadOnlyBooleanProperty;

public interface Selectable {
	
	public ReadOnlyBooleanProperty selectedProperty();
	default public boolean isSelected() {
		return selectedProperty().getValue();
	}
	
	public void select();
	public void deselect();
	
}
