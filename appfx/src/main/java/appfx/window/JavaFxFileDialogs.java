package appfx.window;

import java.io.File;
import java.util.Optional;
import java.util.function.Consumer;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import reactor.core.publisher.Mono;
import riskfx.util.ui.FileDialogs;

public class JavaFxFileDialogs implements FileDialogs<Node, FileChooser> {

	@Override
	public Mono<File> showOpenFileDialog(Node node, Consumer<FileChooser> action) {
		return Mono.fromCallable(() -> {
			final FileChooser chooser = new FileChooser();
			action.accept(chooser);
			final File file = chooser.showOpenDialog(getWindow(node));
			return file;
		});
	}

	private Window getWindow(Node node) {
		return Optional.ofNullable(node).map(Node::getScene).map(Scene::getWindow).orElse(null);
	}

}
