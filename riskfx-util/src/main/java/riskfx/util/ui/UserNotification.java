package riskfx.util.ui;

public record UserNotification(String message) {

	public static UserNotification of(final String format, final Object... args) {
		return new UserNotification(format.formatted(args));
	}
	
	public UserNotificationWithAction withAction(final String actionText, final Runnable action) {
		return new UserNotificationWithAction(message(), actionText, action);
	}
}
