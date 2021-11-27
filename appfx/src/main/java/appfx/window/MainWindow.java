package appfx.window;

import java.util.Objects;

import appfx.util.FxmlView;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.Event;
import javafx.event.EventType;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import riskfx.util.ui.LockableView;
import riskfx.util.ui.UserNotification;
import riskfx.util.ui.UserNotificationWithAction;

/**
 * @author christopher
 *
 */
public class MainWindow extends Control implements LockableView {

	@Override
	public final String getUserAgentStylesheet() {
		return getClass().getResource("mainwindow.css").toExternalForm();
	}

	public static class LockViewEvent extends Event {
		public static final EventType<LockViewEvent> ANY = new EventType<>("LockView");
		public final String message;
		public LockViewEvent(final String message) {
			super(ANY);
			this.message = Objects.requireNonNull(message);
		}
		
	}
	
	public static class UnlockViewEvent extends Event {
		public static final EventType<UnlockViewEvent> ANY = new EventType<>("UnlockView");
		public UnlockViewEvent() {
			super(ANY);
		}
		
	}
	
	public static class NotifyEvent extends Event {
		public static final EventType<NotifyEvent> ANY = new EventType<>("Notify");
		
		public final UserNotification notification;
		
		public NotifyEvent(final UserNotification notification) {
			super(ANY);
			this.notification = Objects.requireNonNull(notification);
		}
	}

	private final AppBar appbar = new AppBar();
	
	private final ObjectProperty<Node> contentProperty = new SimpleObjectProperty<>(this, "content") {

		@Override
		protected void invalidated() {
			final Node nv = get();
			if (nv != null) {
				if (nv instanceof FxmlView nx) {
					nx.updateAppBar(getAppBar());
				}
			}
		}
		
	};
	public final ObjectProperty<Node> contentProperty()		{	return this.contentProperty; }
	public final Node getContent()							{	return this.contentProperty.getValue(); }
	public final void setContent(final Node node)			{ 	this.contentProperty.set(node); }
	
	
	public MainWindow() {
		getStyleClass().add("mainwindow");
	}
	@Override
	protected Skin<?> createDefaultSkin() {
		return new MainWindowSkin(this);
	}
	
	@Override
	public final void lockView(final UserNotification message) {
		this.fireEvent(new LockViewEvent(message.message()));
	}

	@Override
	public final void unlockView() {
		this.fireEvent(new UnlockViewEvent());
	}

	public final AppBar getAppBar() {
		return this.appbar;
	}
	public void notify(final UserNotification message) {
		fireEvent(new NotifyEvent(message));
	}
	public void notify(UserNotificationWithAction n) {
//		fireEvent(new NotifyEvent(n));
	}
	public void notifyError(final UserNotification of, final Throwable ex) {
		final UserNotificationWithAction n = UserNotification.of(of.message())
				.withAction("DETAIL", () -> {});
		notify(n);
	}
	
	
}
