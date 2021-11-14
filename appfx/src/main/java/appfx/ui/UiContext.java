package appfx.ui;

import java.io.File;
import java.util.Deque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.function.Consumer;

import appfx.util.UserNotification;
import appfx.util.UserNotificationWithAction;
import appfx.window.MainWindow;
import javafx.scene.Node;
import javafx.stage.FileChooser;
import reactor.core.publisher.Mono;

public interface UiContext extends LockableView {
	
	public static UiContext of(final MainWindow mainWindow, final FileDialogs dialogs, final ExitController requestExit) {
		return new UiContext() {
			final Deque<Node> stack = new LinkedBlockingDeque<>();
			
			@Override
			public void lockView(UserNotification n) {
				mainWindow.lockView(n);
			}

			@Override
			public void unlockView() {
				mainWindow.unlockView();
			}

			@Override
			public void notify(UserNotification n) {
				mainWindow.notify(n);
			}

			@Override
			public void notify(UserNotificationWithAction n) {
				mainWindow.notify(n);
			}

			@Override
			public void requestExit() {
				requestExit.requestExit();
			}

			@Override
			public void notifyError(UserNotification of, Throwable ex) {
				mainWindow.notifyError(of, ex);
			}

			@Override
			public Mono<File> showOpenFileDialog(final Consumer<FileChooser> action) {
				return dialogs.showOpenFileDialog(mainWindow, action);
			}

			@Override
			public void switchView(Node node) {
				stack.addFirst(node);
				mainWindow.setContent(node);
			}

			@Override
			public void goBack() {
				final Node n = stack.removeFirst();
				mainWindow.setContent(n);
			}};
	}
	
	public void notify(final UserNotification n);
	public void notify(final UserNotificationWithAction n);
	public void requestExit();
	public void switchView(final Node node);
	public void notifyError(UserNotification of, Throwable ex);
	
	public Mono<File> showOpenFileDialog(final Consumer<FileChooser> action);

	public void goBack();
}
