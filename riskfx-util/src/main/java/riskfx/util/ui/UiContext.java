package riskfx.util.ui;

public interface UiContext<C> extends LockableView {
	
	public void notify(final UserNotification n);
	public void notify(final UserNotificationWithAction n);
	public void requestExit();
	public void switchView(final C node);
	public void notifyError(UserNotification of, Throwable ex);
	
//	public Mono<File> showOpenFileDialog(final Consumer<FileChooser> action);

	public void goBack();
}
