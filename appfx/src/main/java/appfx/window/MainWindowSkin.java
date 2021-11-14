package appfx.window;

import java.util.logging.Logger;

import com.jfoenix.controls.JFXSnackbar;
import com.jfoenix.controls.JFXSnackbar.SnackbarEvent;
import com.jfoenix.controls.JFXSnackbarLayout;

import appfx.window.MainWindow.LockViewEvent;
import appfx.window.MainWindow.NotifyEvent;
import appfx.window.MainWindow.UnlockViewEvent;
import javafx.scene.Node;
import javafx.scene.control.Skin;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

public class MainWindowSkin extends SkinBase<MainWindow> implements Skin<MainWindow> {
	private static final Logger LOG = Logger.getLogger(MainWindowSkin.class.getName());
	
	private final StackPane rootPane = new StackPane();
	private final GlassPane glassPane = new GlassPane();
//	private final NotificationPane notificationPane = new NotificationPane();
	private final StackPane contentPane = new StackPane();
	private final StackPane snackbarPane = new StackPane(contentPane);
	private final JFXSnackbar notificationPane = new JFXSnackbar(snackbarPane);
	
	
	private final AnimatedFlowContainer flow = new AnimatedFlowContainer(contentPane);
	
	protected MainWindowSkin(final MainWindow control) {
		super(control);
		
		contentPane.setId("main-window-container");
		
		final BorderPane borderPane = new BorderPane();
		
//		borderPane.setTop(control.getAppBar());
		borderPane.setCenter(snackbarPane);
		
		rootPane.getChildren().add(borderPane);
		rootPane.getChildren().add(glassPane);
		getChildren().add(rootPane);

		control.contentProperty().addListener((o, ov, nv) -> flow.updateContent(nv));
		final Node n = control.contentProperty().get();
		if (n != null) {
			flow.updateContent(n);
		}
		control.addEventHandler(LockViewEvent.ANY, evt -> lockView(evt.message));
		control.addEventHandler(UnlockViewEvent.ANY, evt-> unlockView());
		control.addEventHandler(NotifyEvent.ANY, evt -> {
			LOG.info("Enque a message: %s".formatted(evt.notification));
			try {
				notificationPane.enqueue(new SnackbarEvent(new JFXSnackbarLayout(evt.notification.message())));
			}
			catch (RuntimeException e) {
				System.err.println("Unexpected exception: " + e);
			}
		});
	}

	private void lockView(final String message) {
		glassPane.setMessage(message);
		glassPane.show();
	}

	private void unlockView() {
		glassPane.hide();
	}
}
