package appfx.util;

public record UserNotificationWithAction(String message, String actionText, Runnable action) {

}
