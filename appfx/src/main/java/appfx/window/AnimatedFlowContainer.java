package appfx.window;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

public class AnimatedFlowContainer {

	private static final Duration DEFAULT_DURATION = Duration.millis(320);
	
	private final Duration duration;
	private final StackPane root;
	private final ImageView placeholder;
	
	private Timeline animation;
	
	public AnimatedFlowContainer(final StackPane contentPane) {
		this(DEFAULT_DURATION, contentPane);
	}

	public AnimatedFlowContainer(final Duration duration, final StackPane contentPane) {
		this.duration = Objects.requireNonNull(duration);
		this.root = Objects.requireNonNull(contentPane);
		this.placeholder = new ImageView();
		placeholder.setPreserveRatio(true);
        placeholder.setSmooth(true);
	}
	
	public void updateContent(final Node content) {
		updatePlaceholder(content);

        if (animation != null) {
            animation.stop();
        }

        animation = new Timeline();
        animation.getKeyFrames().addAll(fade(this));
        animation.getKeyFrames().add(new KeyFrame(duration, (e) -> clearPlaceholder()));

        animation.play();
	}

	static List<KeyFrame> fade(final AnimatedFlowContainer c) {
		return new ArrayList<>(Arrays.asList(new KeyFrame(Duration.ZERO, new KeyValue(c.placeholder.opacityProperty(), 1.0, Interpolator.EASE_BOTH)),
                new KeyFrame(c.duration, new KeyValue(c.placeholder.opacityProperty(), 0.0, Interpolator.EASE_BOTH))));
	}
	
	private void clearPlaceholder() {
        placeholder.setImage(null);
        placeholder.setVisible(false);
    }

    private void updatePlaceholder(Node newView) {
        if (root.getWidth() > 0 && root.getHeight() > 0) {
            Image placeholderImage = root.snapshot(null, new WritableImage((int) root.getWidth(), (int) root.getHeight()));
            placeholder.setImage(placeholderImage);
            placeholder.setFitWidth(placeholderImage.getWidth());
            placeholder.setFitHeight(placeholderImage.getHeight());
        } else {
            placeholder.setImage(null);
        }
        placeholder.setVisible(true);
        placeholder.setOpacity(1.0);
        root.getChildren().setAll(placeholder);
        root.getChildren().add(newView);
        placeholder.toFront();

    }
}
