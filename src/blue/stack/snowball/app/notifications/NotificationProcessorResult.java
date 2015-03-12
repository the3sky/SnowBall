package blue.stack.snowball.app.notifications;

import blue.stack.snowball.app.inbox.RawMessage;

public class NotificationProcessorResult {
    RawMessage message;
    boolean shouldRemoveNotification;

    public NotificationProcessorResult(RawMessage message, boolean shouldRemoveNotification) {
        this.message = message;
        this.shouldRemoveNotification = shouldRemoveNotification;
    }

    public RawMessage getMessage() {
        return this.message;
    }

    public boolean shouldRemoveNotification() {
        return this.shouldRemoveNotification;
    }

    public void setShouldRemoveNotification(boolean shouldRemoveNotification) {
        this.shouldRemoveNotification = shouldRemoveNotification;
    }
}
