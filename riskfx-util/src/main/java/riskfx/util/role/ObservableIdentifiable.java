package riskfx.util.role;

import javafx.beans.property.ReadOnlyStringProperty;

public interface ObservableIdentifiable extends Identifiable {

	public ReadOnlyStringProperty idProperty();
	
	default public String getId() {
		return this.idProperty().getValue();
	}
}
