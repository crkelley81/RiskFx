package riskfx.util.ui;

import java.io.File;
import java.util.function.Consumer;

import reactor.core.publisher.Mono;

public interface FileDialogs<C,FC> {

	public Mono<File> showOpenFileDialog(final C node, Consumer<FC> action);
}
