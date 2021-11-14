package appfx.window;

import java.util.Optional;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Skin;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

public class AppBarSkin extends SkinBase<AppBar> implements Skin<AppBar> {

	private final Label titleLabel = new Label();
	
	protected AppBarSkin(final AppBar control) {
		super(control);
	
		titleLabel.textProperty().bind(control.titleTextProperty());
		titleLabel.getStyleClass().add("appbar-title");
		
		control.navIconProperty().addListener(o -> update());
		control.titleProperty().addListener(o -> update());
	
		update();
	}
	
	private void update() {
		getChildren().clear();
		
		addWithStyle(getSkinnable().getNavIcon(), "appbar-navicon");
		
		final Node node = Optional.ofNullable(getSkinnable().getTitle())
			.orElse(titleLabel);
		HBox.setHgrow(node, Priority.ALWAYS);
		getChildren().add(node);
	
	}

	private void addWithStyle(final Node node, String styleClass) {
		if (node == null) return;
		
		if (!node.getStyleClass().contains(styleClass)) {
			node.getStyleClass().add(styleClass);
		}
		
		getChildren().add(node);
	}
}
