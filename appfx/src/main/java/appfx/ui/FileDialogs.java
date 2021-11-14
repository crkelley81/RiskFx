package appfx.ui;

import java.io.File;
import java.util.Optional;
import java.util.function.Consumer;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import reactor.core.publisher.Mono;

public interface FileDialogs {

	default public Mono<File> showOpenFileDialog(final Node node, Consumer<FileChooser> action) {
		return Mono.fromCallable(() -> {
			final FileChooser chooser = new FileChooser();
			action.accept(chooser);
			return chooser.showOpenDialog(getWindow(node));
		});
	}

	static Window getWindow(final Node node) {
		return Optional.ofNullable(node).map(Node::getScene).map(Scene::getWindow).orElse(null);
	}

}
