package riskfx.app.util.role;

import javafx.beans.property.ReadOnlyStringProperty;

public interface ObservableDisplayable extends Displayable {

	public ReadOnlyStringProperty displayNameProperty();
	
	default public String getDisplayName() {
		return this.displayNameProperty().getValue();
	}
}
