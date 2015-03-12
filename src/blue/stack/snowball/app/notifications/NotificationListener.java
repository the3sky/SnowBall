package blue.stack.snowball.app.notifications;

public interface NotificationListener {
    void onNotificationPosted(Notification notification);

    void onNotificationServiceRunning();
}
