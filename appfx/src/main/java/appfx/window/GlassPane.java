package appfx.window;

import javafx.animation.FadeTransition;
import javafx.css.StyleableProperty;
import javafx.css.StyleablePropertyFactory;
import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.input.InputEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

public class GlassPane extends BorderPane {

	private static final String DEFAULT_STYLE_CLASS = "glasspane";
	
	private static final StyleablePropertyFactory<GlassPane> FACTORY = new StyleablePropertyFactory<>(StackPane.getClassCssMetaData());
	
	private final StyleableProperty<Duration> durationProperty = 
			FACTORY.createStyleableDurationProperty(this, "duration", "-duration", s -> s.durationProperty, Duration.millis(320));
	public final StyleableProperty<Duration> durationProperty()		{	return this.durationProperty; }
	public final Duration getDuration()								{	return this.durationProperty.getValue(); }
	public final void setDuration(final Duration duration)			{	this.durationProperty.setValue(duration); }
	
	private final EventHandler<InputEvent> inputFilter = evt -> evt.consume();
	
	private FadeTransition animation;
	
	public GlassPane() {
		super();
			
		this.getStyleClass().add(DEFAULT_STYLE_CLASS);
		this.message.getStyleClass().add("glasspane-message");
		this.indicator.getStyleClass().add("glasspane-indicator");
		
		final HBox hbox = new HBox(indicator, message);
		setCenter(hbox);
		
		indicator.setProgress(0.0);
		
		setOpacity(0);
		setVisible(false);
		setManaged(false);
		
		//setStyle("-fx-background-color: black;");
	}
	
	public void show() {
		this.addEventFilter(InputEvent.ANY, inputFilter);
		
		if (animation != null) {
			animation.stop();
		}

		setVisible(true);
		setManaged(true);
		indicator.setProgress(-1.0);
		
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
			
			message.setText(null);
			indicator.setProgress(0.0);
			
			setVisible(false);
			setManaged(false);

			indicator.setProgress(0.0);
		});
		animation.play();
	}

	public void setMessage(String message) {
		this.message.setText(message);	
	}
	
	@Override
	public final String getUserAgentStylesheet() {
		return getClass().getResource("glasspane.css").toExternalForm();
	}

	private final Label message = new Label();
	private final ProgressIndicator indicator = new ProgressIndicator();
	
}
