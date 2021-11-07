package appfx.ui;

import appfx.util.UserNotification;

public interface LockableView {
	public void lockView(final UserNotification n);
	public void unlockView();
}
