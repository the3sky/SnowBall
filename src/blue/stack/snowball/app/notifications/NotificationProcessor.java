package blue.stack.snowball.app.notifications;

public interface NotificationProcessor {
    NotificationProcessorResult processNotification(Notification notification);

    boolean shouldHandleNotification(Notification notification);
}
