package appfx.window;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

public class AppBar extends Control {
	private static final String DEFAULT_STYLE_CLASS = "appbar";
	
	private final StringProperty titleTextProperty = new SimpleStringProperty(this, "titleText");
	public final StringProperty titleTextProperty()	{	return this.titleTextProperty; }
	
	private final ObjectProperty<Node> titleProperty = new SimpleObjectProperty<>(this, "title");
	public final ObjectProperty<Node> titleProperty() 		{ return this.titleProperty; }
	public final Node getTitle()							{	return this.titleProperty.get(); }
	
	private final ObjectProperty<Node> navIconProperty = new SimpleObjectProperty<>(this, "navIcon");
	public final ObjectProperty<Node> navIconProperty()		{	return this.navIconProperty; }
	public final Node getNavIcon()							{	return this.navIconProperty.get(); }
	
	/* package */ AppBar() {
		this.getStyleClass().add(DEFAULT_STYLE_CLASS);
	}
	
	@Override
	public final String getUserAgentStylesheet() {
		return getClass().getResource("appbar.css").toExternalForm();
	}

	@Override
	protected Skin<?> createDefaultSkin() {
		return new AppBarSkin(this);
	}
	
	
	
}
