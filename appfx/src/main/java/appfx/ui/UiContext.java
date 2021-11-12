package appfx.ui;

import java.io.File;
import java.util.function.Consumer;

import com.airhacks.afterburner.views.FXMLView;

import appfx.util.UserNotification;
import appfx.util.UserNotificationWithAction;
import javafx.stage.FileChooser;
import reactor.core.publisher.Mono;

public interface UiContext extends LockableView {
	public void notify(final UserNotification n);
	public void notify(final UserNotificationWithAction n);
	public void requestExit();
	public void switchView(final FXMLView view);
	public void notifyError(UserNotification of, Throwable ex);
	public Mono<File> showOpenFileDialog(final Consumer<FileChooser> action);
}
