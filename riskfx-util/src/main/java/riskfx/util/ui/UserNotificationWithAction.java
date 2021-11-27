package riskfx.util.ui;

public record UserNotificationWithAction(String message, String actionText, Runnable action) {

}
