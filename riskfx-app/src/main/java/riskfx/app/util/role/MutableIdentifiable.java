package riskfx.app.util.role;

import javafx.beans.property.StringProperty;

public interface MutableIdentifiable extends ObservableIdentifiable {

	public StringProperty idProperty();
	
	default public void setId(final String id) {
		this.idProperty().setValue(id);
	}
}
