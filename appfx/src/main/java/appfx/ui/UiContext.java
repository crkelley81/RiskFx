package appfx.ui;

import appfx.util.UserNotification;
import appfx.util.UserNotificationWithAction;

public interface UiContext extends LockableView {
	public void notify(final UserNotification n);
	public void notify(final UserNotificationWithAction n);
}
