package riskfx.app.util.role;

import javafx.beans.property.StringProperty;

public interface MutableDisplayable extends ObservableDisplayable {

	public StringProperty displayNameProperty();
	
	default public void setDisplayName(final String displayName) {
		this.displayNameProperty().setValue(displayName);
	}
}
