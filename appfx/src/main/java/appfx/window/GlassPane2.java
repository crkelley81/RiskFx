package appfx.window;

import javafx.animation.FadeTransition;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.css.StyleableProperty;
import javafx.css.StyleablePropertyFactory;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.input.InputEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.util.Duration;

public class GlassPane2 extends Region {

	private static final String DEFAULT_STYLE_CLASS = "glasspane";

	private static final StyleablePropertyFactory<GlassPane2> FACTORY = new StyleablePropertyFactory<>(Region.getClassCssMetaData());
	
	private final StyleableProperty<Duration> durationProperty = 
			FACTORY.createStyleableDurationProperty(this, "duration", "-duration", s -> s.durationProperty, Duration.millis(320));
	public final StyleableProperty<Duration> durationProperty()		{	return this.durationProperty; }
	public final Duration getDuration()								{	return this.durationProperty.getValue(); }
	public final void setDuration(final Duration duration)			{	this.durationProperty.setValue(duration); }
	
	private final ObjectProperty<Node> content = new SimpleObjectProperty<>(this, "content") {

		@Override
		protected void invalidated() {
			getChildren().setAll(scrim);
			final Node node = get();
			if (node != null) {
				getChildren().add(node);
			}
			
		}
		
	};
	public final void setContent(final Node node)					{	this.content.setValue(node); }
	private final EventHandler<InputEvent> inputFilter = evt -> evt.consume();
	
	private FadeTransition animation;
	
	private final Label message = new Label();
	private final ProgressIndicator indicator = new ProgressIndicator();
	private final Region scrim = new Region();
	
	public GlassPane2() {
		this.getStyleClass().add(DEFAULT_STYLE_CLASS);
		
		this.message.getStyleClass().add("glasspane-message");
		this.indicator.getStyleClass().add("glasspane-indicator");
		indicator.setProgress(0.0);
		
		final HBox hbox = new HBox(indicator, message);
		setContent(hbox);
		
		scrim.setStyle("-fx-background-color: rgba(0,0,0, 0.7);");
//		scrim.setPrefSize(200, 200);
		setOpacity(0);
		setVisible(false);
		setManaged(false);
	}
	
	@Override
	protected void layoutChildren() {
		super.layoutChildren();
		
		this.layoutInArea(scrim, 0, 0, this.getWidth(), this.getHeight(),0, getInsets(), HPos.CENTER, VPos.CENTER);
		
		final Node node = content.get();
		if (node != null) {
			double prefWidth = node.prefWidth(-1);
			double prefHeight = node.prefHeight(-1);
			
			double x = (this.getWidth() - prefWidth) / 2;
			double y = (this.getHeight() - prefHeight) / 2;
			this.layoutInArea(node, x, y, prefWidth, prefHeight,0, getInsets(), HPos.CENTER, VPos.CENTER);
		}
		
	}
	public void show() {
		scrim.addEventFilter(InputEvent.ANY, inputFilter);
		
		if (animation != null) {
			animation.stop();
		}

		setVisible(true);
		setManaged(true);
//		indicator.setProgress(-1.0);
		
		animation = new FadeTransition(getDuration(), this);
		animation.setFromValue(getOpacity());
		animation.setToValue(1.0);
		animation.setOnFinished(evt -> {
			animation = null;
		});
		
		animation.play();
	}

	public void hide() {
		if (animation != null) {
			animation.pause();
		}
		
		animation = new FadeTransition(getDuration(), this);
		animation.setFromValue(getOpacity());
		animation.setToValue(0.0);
		animation.setOnFinished(evt -> {
			animation = null;
			
//			message.setText(null);
//			indicator.setProgress(0.0);
			
			setVisible(false);
			setManaged(false);

//			indicator.setProgress(0.0);
		});
		animation.play();
	}
	public void setMessage(String string) {
		this.message.setText(string);
	}

}
