package appfx.window;

import org.controlsfx.control.NotificationPane;

import appfx.window.MainWindow.LockViewEvent;
import appfx.window.MainWindow.NotifyEvent;
import javafx.scene.Node;
import javafx.scene.control.Skin;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;

public class MainWindowSkin extends SkinBase<MainWindow> implements Skin<MainWindow> {

	private final StackPane rootPane = new StackPane();
	private final GlassPane glassPane = new GlassPane();
	private final NotificationPane notificationPane = new NotificationPane();
	private final StackPane contentPane = new StackPane();
	
	private final AnimatedFlowContainer flow = new AnimatedFlowContainer(contentPane);
	
	protected MainWindowSkin(final MainWindow control) {
		super(control);
		
		final BorderPane borderPane = new BorderPane();
		
		borderPane.setTop(control.getAppBar());
		borderPane.setCenter(notificationPane);
		notificationPane.setContent(contentPane);
		
		rootPane.getChildren().add(borderPane);
		rootPane.getChildren().add(glassPane);
		getChildren().add(rootPane);

		control.contentProperty().addListener((o, ov, nv) -> flow.updateContent(nv));
		final Node n = control.contentProperty().get();
		if (n != null) {
			flow.updateContent(n);
		}
		control.addEventHandler(LockViewEvent.ANY, evt -> lockView(evt.message));
		control.addEventHandler(NotifyEvent.ANY, evt -> notificationPane.show(evt.notification.message()));
	}

	private void lockView(final String message) {
		glassPane.setMessage(message);
		glassPane.show();
	}

	private void unlockView() {
		glassPane.hide();
	}
}
